package com.example.newsapp.news.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.newsapp.core.domain.AppLanguage
import com.example.newsapp.core.domain.Article
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.androidx.compose.koinViewModel

@Composable
fun NewsScreenCore(
    viewModel: NewsViewModel = koinViewModel(),
    onArticleClick: (String) -> Unit
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    NewsScreen(
        onAction = viewModel::onActions,
        state = state,
        onArticleClick = onArticleClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onAction: (NewsActions) -> Unit,
    state: State<NewsState>,
    onArticleClick: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = "The News", fontSize = 25.sp, fontFamily = FontFamily.Serif
                    )
                }, windowInsets = WindowInsets(top = 30.dp, bottom = 5.dp),
                actions = {
                    LanguageSwitcher(
                        enabled = state.value.isOnline,
                        onLanguageChange = { onAction(NewsActions.ChangeLanguage(it)) }
                    )
                }
            )
        }) { paddingValue ->
        Box(
            modifier = Modifier
                .padding(paddingValue)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {

            if (state.value.isLoading && state.value.articleList.isEmpty()) {
                CircularProgressIndicator()
            }
            if (state.value.isError && state.value.articleList.isEmpty()) {
                Text(
                    text = "Could not Load News",
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.value.articleList.isNotEmpty()) {
                val listState = rememberLazyListState()
                val shouldPaginate = remember {
                    derivedStateOf {
                        val totalItem = listState.layoutInfo.totalItemsCount
                        val lastVisibleIndex =
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleIndex == totalItem - 1 &&
                                !state.value.isLoading &&
                                state.value.isOnline &&
                                state.value.nextPage != null
                    }
                }

                LaunchedEffect(key1 = listState) {
                    snapshotFlow {
                        shouldPaginate.value
                    }.distinctUntilChanged().filter { it }.collect {
                        onAction(NewsActions.Pagination)
                    }
                }

                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    state = listState
                ) {
                    itemsIndexed(
                        items = state.value.articleList,
                        key = { _, article -> article.articleId }) { index, article ->

                        ArticleItem(
                            article = article,
                            onArticleClick = onArticleClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    article: Article,
    onArticleClick: (String) -> Unit
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .clickable { onArticleClick(article.articleId) }
        .padding(16.dp, 16.dp)
    ) {
        Text(
            text = article.sourceName.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            maxLines = 1,
            fontFamily = FontFamily.Monospace,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = article.title.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            maxLines = 2,
            fontFamily = FontFamily.Monospace,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(8.dp))

        SubcomposeAsyncImage(
            model = article.imageUrl,
            contentDescription = article.title,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
            loading = { if (article.imageUrl?.isNotBlank() == true) LinearProgressIndicator(Modifier.fillMaxWidth()) }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = article.description.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            maxLines = 2,
            fontFamily = FontFamily.Monospace,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = article.pubDate.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            maxLines = 1,
            fontFamily = FontFamily.Monospace,
            overflow = TextOverflow.Ellipsis,
        )
    }
    HorizontalDivider(thickness = 2.dp)
}


@Composable
fun LanguageSwitcher(
    enabled: Boolean,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }

    Box {
        IconButton(
            enabled = enabled,
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Change Language"
            )
        }

        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    onLanguageChange(AppLanguage.ENGLISH)
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("Persian") },
                onClick = {
                    onLanguageChange(AppLanguage.PERSIAN)
                    expanded = false
                }
            )
        }
    }
}


