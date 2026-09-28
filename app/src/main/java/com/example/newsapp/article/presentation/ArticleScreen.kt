package com.example.newsapp.article.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.newsapp.core.domain.Article
import org.koin.androidx.compose.koinViewModel


@Composable
fun ArticleScreenCore(
    viewModel: ArticleViewModel = koinViewModel(), articleId: String, onBackClick: () -> Unit
) {
    LaunchedEffect(true) {
        viewModel.onAction(ArticleActions.LoadArticle(articleId))
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
    ArticleScreen(state = state, onBackClick = onBackClick)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    state: State<ArticleState>, onBackClick: () -> Unit
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
                        state.value.article?.sourceName.toString(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 25.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                windowInsets = WindowInsets(top = 30.dp, bottom = 5.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back To News Screen"
                        )
                    }
                },
            )
        }) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (state.value.isLoading && state.value.article == null) CircularProgressIndicator()

            if (state.value.isError && state.value.article == null) {
                Text(
                    text = "Could not Load Article",
                    fontSize = 25.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.error
                )
            }
            state.value.article?.let { article ->
                ArticleDetails(article)
            }
        }
    }
}

@Composable
fun ArticleDetails(article: Article) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = article.pubDate.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = article.title.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(8.dp))
        SubcomposeAsyncImage(model = article.imageUrl,
            contentDescription = article.title,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
            loading = { if (article.imageUrl?.isNotBlank() == true)
                LinearProgressIndicator(Modifier.fillMaxWidth()) })
        Spacer(Modifier.height(8.dp))
        Text(
            text = article.description.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text(
            text = article.content.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
    }
}




