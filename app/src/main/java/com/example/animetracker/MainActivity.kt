package com.example.animetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import com.example.animetracker.data.remote.SubjectItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as AnimeApplication
        setContent {
            MaterialTheme {
                val viewModel: SearchViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return SearchViewModel(application.repository) as T
                        }
                    }
                )
                SearchScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入番剧名称，如：刀剑神域") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { /* 已经通过 debounce 自动触发 */ })
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            uiState.results.isNotEmpty() -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.results) { item ->
                        SearchResultItem(item)
                    }
                }
            }
            else -> {
                Text("输入关键字开始搜索", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
fun SearchResultItem(item: SubjectItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 暂时不做跳转，后续添加详情 */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUrl = item.images?.common ?: item.images?.medium ?: item.images?.large
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = item.name_cn ?: item.name,
                modifier = Modifier.size(60.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = item.name_cn ?: item.name, style = MaterialTheme.typography.titleMedium)
            if (item.name_cn != null && item.name != item.name_cn) {
                Text(text = item.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
