@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.animetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animetracker.data.local.AnimeEntry
import com.example.animetracker.data.local.AnimeProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as AnimeApplication
        setContent {
            MaterialTheme {
                val viewModel: AnimeViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AnimeViewModel(application.repository) as T
                        }
                    }
                )
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AnimeViewModel) {
    val selectedAnimeId by viewModel.selectedAnimeId.collectAsState()

    if (selectedAnimeId == null) {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("添加番剧", "我的番剧")

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> AddAnimeTab(viewModel)
                1 -> AnimeListTab(viewModel)
            }
        }
    } else {
        AnimeDetailScreen(viewModel, selectedAnimeId)
    }
}

@Composable
fun AddAnimeTab(viewModel: AnimeViewModel) {
    val formState by viewModel.formState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("手动添加番剧", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = formState.name,
            onValueChange = viewModel::updateName,
            label = { Text("番剧名称（必填）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = formState.nameCn,
            onValueChange = viewModel::updateNameCn,
            label = { Text("中文名（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = formState.totalEpisodes,
            onValueChange = viewModel::updateTotalEpisodes,
            label = { Text("总集数（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = formState.currentEpisode,
            onValueChange = viewModel::updateCurrentEpisode,
            label = { Text("当前看到第几集（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = formState.note,
            onValueChange = viewModel::updateNote,
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        val statusOptions = listOf(
            "watching" to "在看",
            "completed" to "看完",
            "on_hold" to "搁置",
            "dropped" to "弃番",
            "plan_to_watch" to "想看"
        )
        var statusExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = it }
        ) {
            OutlinedTextField(
                value = statusOptions.find { it.first == formState.status }?.second ?: "在看",
                onValueChange = {},
                readOnly = true,
                label = { Text("状态") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                statusOptions.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            viewModel.updateStatus(value)
                            statusExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.saveAnime() },
            modifier = Modifier.fillMaxWidth(),
            enabled = formState.name.isNotBlank()
        ) {
            Text("保存")
        }

        if (saveSuccess) {
            Text("保存成功！", color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun AnimeListTab(viewModel: AnimeViewModel) {
    val animeList by viewModel.allAnime.collectAsState()

    if (animeList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("还没有添加任何番剧")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(animeList, key = { it.id }) { anime ->
                AnimeListItem(anime, viewModel)
            }
        }
    }
}

@Composable
fun AnimeListItem(anime: AnimeEntry, viewModel: AnimeViewModel) {
    var latestEp by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(anime.id) {
        latestEp = viewModel.getLatestProgressForAnime(anime.id)?.episode
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectAnime(anime.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(anime.nameCn ?: anime.name, style = MaterialTheme.typography.titleMedium)
            if (anime.nameCn != null && anime.name != anime.nameCn) {
                Text(anime.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = when (anime.status) {
                    "watching" -> "在看"
                    "completed" -> "看完"
                    "on_hold" -> "搁置"
                    "dropped" -> "弃番"
                    else -> "想看"
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (latestEp != null) {
                Text("看到第 $latestEp 集", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AnimeDetailScreen(viewModel: AnimeViewModel, animeId: Long) {
    val animeEntry by viewModel.getAnimeFlow(animeId).collectAsState(initial = null)
    val progressHistory by viewModel.getProgressFlow(animeId).collectAsState(initial = emptyList())

    val progressEpisode = remember { mutableStateOf("") }
    val progressNote = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = { viewModel.clearSelection() }) {
            Text("返回列表")
        }

        animeEntry?.let { entry ->
            Text(entry.nameCn ?: entry.name, style = MaterialTheme.typography.headlineMedium)
            if (entry.nameCn != null && entry.name != entry.nameCn) {
                Text(entry.name, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Text("状态：${when (entry.status) {
                "watching" -> "在看"
                "completed" -> "看完"
                "on_hold" -> "搁置"
                "dropped" -> "弃番"
                else -> "想看"
            }}")
            entry.episodes?.let { Text("总集数：$it") }
            entry.summary?.let { Text(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("历史进度", style = MaterialTheme.typography.titleMedium)
            if (progressHistory.isEmpty()) {
                Text("还没有进度记录")
            } else {
                progressHistory.forEach { progress ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("第 ${progress.episode} 集")
                        Text(
                            text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .format(Date(progress.watchedDate))
                        )
                        progress.note?.let { Text(it, color = Color.Gray) }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("更新进度", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = progressEpisode.value,
                onValueChange = { progressEpisode.value = it },
                label = { Text("看到第几集") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = progressNote.value,
                onValueChange = { progressNote.value = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    val ep = progressEpisode.value.toIntOrNull()
                    if (ep != null && ep > 0) {
                        viewModel.addProgress(animeId, ep, progressNote.value)
                        progressEpisode.value = ""
                        progressNote.value = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = progressEpisode.value.isNotBlank()
            ) {
                Text("保存进度")
            }
        }
    }
}
