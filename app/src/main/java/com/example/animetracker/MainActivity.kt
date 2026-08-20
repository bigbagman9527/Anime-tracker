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
import com.example.animetracker.data.local.NovelEntry
import com.example.animetracker.data.local.NovelProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as AnimeApplication
        setContent {
            MaterialTheme {
                val animeViewModel: AnimeViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AnimeViewModel(application.animeRepository) as T
                        }
                    }
                )
                val novelViewModel: NovelViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return NovelViewModel(application.novelRepository) as T
                        }
                    }
                )
                MainScreen(animeViewModel, novelViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(animeViewModel: AnimeViewModel, novelViewModel: NovelViewModel) {
    var selectedMainTab by remember { mutableStateOf(0) }
    val mainTabs = listOf("番剧", "小说")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedMainTab) {
            mainTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedMainTab == index,
                    onClick = { selectedMainTab = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedMainTab) {
            0 -> AnimeSection(animeViewModel)
            1 -> NovelSection(novelViewModel)
        }
    }
}

@Composable
fun AnimeSection(viewModel: AnimeViewModel) {
    val selectedAnimeId by viewModel.selectedAnimeId.collectAsState()
    val animeId = selectedAnimeId
    if (animeId != null) {
        AnimeDetailScreen(viewModel, animeId)
        return
    }
    var subTab by remember { mutableStateOf(0) }
    val tabs = listOf("列表", "添加")
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = subTab == index, onClick = { subTab = index }, text = { Text(title) })
            }
        }
        when (subTab) {
            0 -> AnimeListTab(viewModel)
            1 -> AddAnimeTab(viewModel)
        }
    }
}

@Composable
fun NovelSection(viewModel: NovelViewModel) {
    val selectedNovelId by viewModel.selectedNovelId.collectAsState()
    val novelId = selectedNovelId
    if (novelId != null) {
        NovelDetailScreen(viewModel, novelId)
        return
    }
    var subTab by remember { mutableStateOf(0) }
    val tabs = listOf("列表", "添加")
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = subTab == index, onClick = { subTab = index }, text = { Text(title) })
            }
        }
        when (subTab) {
            0 -> NovelListTab(viewModel)
            1 -> AddNovelTab(viewModel)
        }
    }
}

// ==================== 番剧添加（含搜索） ====================
@Composable
fun AddAnimeTab(viewModel: AnimeViewModel) {
    val formState by viewModel.formState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) viewModel.resetSaveSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("添加番剧（可先搜索）", style = MaterialTheme.typography.headlineSmall)

        // 搜索区域
        OutlinedTextField(
            value = searchState.keyword,
            onValueChange = viewModel::updateSearchKeyword,
            label = { Text("输入番剧名称关键词") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = { viewModel.searchBili() },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchState.keyword.isNotBlank() && !searchState.isLoading
        ) {
            if (searchState.isLoading) Text("搜索中...") else Text("搜索匹配")
        }

        if (searchState.error != null) {
            Text(searchState.error!!, color = MaterialTheme.colorScheme.error)
        }

        if (searchState.results.isNotEmpty()) {
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(searchState.results) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.fillFormFromSearch(item) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.cover.isNullOrBlank()) {
                            AsyncImage(
                                model = item.cover,
                                contentDescription = item.title,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column {
                            Text(item.title, style = MaterialTheme.typography.bodyLarge)
                            item.styles?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 表单字段
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

        // 状态选择
        val statusOptions = listOf(
            "watching" to "在看",
            "completed" to "看完",
            "on_hold" to "搁置",
            "dropped" to "弃番",
            "plan_to_watch" to "想看"
        )
        var statusExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
            OutlinedTextField(
                value = statusOptions.find { it.first == formState.status }?.second ?: "在看",
                onValueChange = {},
                readOnly = true,
                label = { Text("状态") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                statusOptions.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = { viewModel.updateStatus(value); statusExpanded = false }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.saveAnime() },
            modifier = Modifier.fillMaxWidth(),
            enabled = formState.name.isNotBlank()
        ) { Text("保存番剧") }

        if (saveSuccess) Text("保存成功！", color = Color(0xFF4CAF50))
    }
}

// ==================== 番剧列表 ====================
@Composable
fun AnimeListTab(viewModel: AnimeViewModel) {
    val animeList by viewModel.allAnime.collectAsState()
    if (animeList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("还没有添加任何番剧，点击“添加”开始记录") }
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
    LaunchedEffect(anime.id) { latestEp = viewModel.getLatestProgressForAnime(anime.id)?.episode }
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
                when (anime.status) {
                    "watching" -> "在看"
                    "completed" -> "看完"
                    "on_hold" -> "搁置"
                    "dropped" -> "弃番"
                    else -> "想看"
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (latestEp != null) Text("看到第 $latestEp 集", style = MaterialTheme.typography.bodySmall)
            Text("点击进入详情 →", style = MaterialTheme.typography.bodySmall, color = Color.Blue)
        }
    }
}

// ==================== 番剧详情（保留原有功能） ====================
@Composable
fun AnimeDetailScreen(viewModel: AnimeViewModel, animeId: Long) {
    val animeEntry by viewModel.getAnimeFlow(animeId).collectAsState(initial = null)
    val progressHistory by viewModel.getProgressFlow(animeId).collectAsState(initial = emptyList())
    val progressEpisode = remember { mutableStateOf("") }
    val progressNote = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = { viewModel.clearSelection() }) { Text("返回列表") }
        animeEntry?.let { entry ->
            Text(entry.nameCn ?: entry.name, style = MaterialTheme.typography.headlineMedium)
            if (entry.nameCn != null && entry.name != entry.nameCn) Text(entry.name, color = Color.Gray)
            Text("状态：${when(entry.status) { "watching" -> "在看"; "completed" -> "看完"; "on_hold" -> "搁置"; "dropped" -> "弃番"; else -> "想看" }}")
            entry.episodes?.let { Text("总集数：$it") }
            entry.summary?.let { Text(it) }

            // 状态修改
            val statusOptions = listOf("watching" to "在看", "completed" to "看完", "on_hold" to "搁置", "dropped" to "弃番", "plan_to_watch" to "想看")
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                OutlinedTextField(
                    value = statusOptions.find { it.first == entry.status }?.second ?: "在看",
                    onValueChange = {}, readOnly = true, label = { Text("修改状态") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    statusOptions.forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.updateAnimeStatus(entry, value); statusExpanded = false })
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("历史进度", style = MaterialTheme.typography.titleMedium)
            if (progressHistory.isEmpty()) Text("还没有进度记录") else {
                progressHistory.forEach { progress ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("第 ${progress.episode} 集")
                        Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(progress.watchedDate)))
                        progress.note?.let { Text(it, color = Color.Gray) }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("更新进度", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = progressEpisode.value, onValueChange = { progressEpisode.value = it }, label = { Text("看到第几集") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = progressNote.value, onValueChange = { progressNote.value = it }, label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                val ep = progressEpisode.value.toIntOrNull()
                if (ep != null && ep > 0) {
                    viewModel.addProgress(animeId, ep, progressNote.value)
                    progressEpisode.value = ""; progressNote.value = ""
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = progressEpisode.value.isNotBlank()) { Text("保存进度") }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.deleteAnime(entry) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除此条目") }
        }
    }
}

// ==================== 小说部分（保持不变） ====================
@Composable
fun AddNovelTab(viewModel: NovelViewModel) {
    val formState by viewModel.formState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    LaunchedEffect(saveSuccess) { if (saveSuccess) viewModel.resetSaveSuccess() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("添加小说（可先搜索）", style = MaterialTheme.typography.headlineSmall)

        // 搜索区域
        OutlinedTextField(
            value = searchState.keyword,
            onValueChange = viewModel::updateSearchKeyword,
            label = { Text("输入书名关键词") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = { viewModel.searchDouban() },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchState.keyword.isNotBlank() && !searchState.isLoading
        ) {
            if (searchState.isLoading) Text("搜索中...") else Text("搜索匹配")
        }

        if (searchState.error != null) {
            Text(searchState.error!!, color = MaterialTheme.colorScheme.error)
        }

        if (searchState.results.isNotEmpty()) {
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(searchState.results) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.fillFormFromSearch(item) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.cover.isNullOrBlank()) {
                            AsyncImage(
                                model = item.cover,
                                contentDescription = item.title,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column {
                            Text(item.title, style = MaterialTheme.typography.bodyLarge)
                            item.author?.let { authors ->
                                Text("作者：${authors.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            item.publisher?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        OutlinedTextField(value = formState.title, onValueChange = viewModel::updateTitle, label = { Text("书名（必填）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = formState.author, onValueChange = viewModel::updateAuthor, label = { Text("作者（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = formState.totalChapters, onValueChange = viewModel::updateTotalChapters, label = { Text("总章节数（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = formState.currentChapter, onValueChange = viewModel::updateCurrentChapter, label = { Text("当前读到第几章（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = formState.note, onValueChange = viewModel::updateNote, label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        val statusOptions = listOf("reading" to "在读", "completed" to "读完", "on_hold" to "搁置", "dropped" to "弃书", "plan_to_read" to "想读")
        var statusExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
            OutlinedTextField(value = statusOptions.find { it.first == formState.status }?.second ?: "在读", onValueChange = {}, readOnly = true, label = { Text("状态") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }, modifier = Modifier.fillMaxWidth())
            ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                statusOptions.forEach { (value, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.updateStatus(value); statusExpanded = false })
                }
            }
        }

        Button(onClick = { viewModel.saveNovel() }, modifier = Modifier.fillMaxWidth(), enabled = formState.title.isNotBlank()) { Text("保存小说") }
        if (saveSuccess) Text("保存成功！", color = Color(0xFF4CAF50))
    }
}


@Composable
fun NovelListTab(viewModel: NovelViewModel) {
    val novelList by viewModel.allNovels.collectAsState()
    if (novelList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("还没有添加任何小说") }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(novelList, key = { it.id }) { novel -> NovelListItem(novel, viewModel) }
        }
    }
}

@Composable
fun NovelListItem(novel: NovelEntry, viewModel: NovelViewModel) {
    var latestChapter by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(novel.id) { latestChapter = viewModel.getLatestProgress(novel.id)?.chapter }
    Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.selectNovel(novel.id) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(novel.title, style = MaterialTheme.typography.titleMedium)
            novel.author?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Text(when(novel.status) { "reading" -> "在读"; "completed" -> "读完"; "on_hold" -> "搁置"; "dropped" -> "弃书"; else -> "想读" }, style = MaterialTheme.typography.bodySmall)
            if (latestChapter != null) Text("读到第 $latestChapter 章", style = MaterialTheme.typography.bodySmall)
            Text("点击进入详情 →", style = MaterialTheme.typography.bodySmall, color = Color.Blue)
        }
    }
}

@Composable
fun NovelDetailScreen(viewModel: NovelViewModel, novelId: Long) {
    val novelEntry by viewModel.getNovelFlow(novelId).collectAsState(initial = null)
    val progressHistory by viewModel.getProgressFlow(novelId).collectAsState(initial = emptyList())
    val progressChapter = remember { mutableStateOf("") }
    val progressNote = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = { viewModel.clearSelection() }) { Text("返回列表") }
        novelEntry?.let { entry ->
            Text(entry.title, style = MaterialTheme.typography.headlineMedium)
            entry.author?.let { Text(it, color = Color.Gray) }
            Text("状态：${when(entry.status) { "reading" -> "在读"; "completed" -> "读完"; "on_hold" -> "搁置"; "dropped" -> "弃书"; else -> "想读" }}")
            entry.totalChapters?.let { Text("总章节数：$it") }

            val statusOptions = listOf("reading" to "在读", "completed" to "读完", "on_hold" to "搁置", "dropped" to "弃书", "plan_to_read" to "想读")
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                OutlinedTextField(value = statusOptions.find { it.first == entry.status }?.second ?: "在读", onValueChange = {}, readOnly = true, label = { Text("修改状态") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    statusOptions.forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.updateNovelStatus(entry, value); statusExpanded = false })
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("历史进度", style = MaterialTheme.typography.titleMedium)
            if (progressHistory.isEmpty()) Text("还没有进度记录") else {
                progressHistory.forEach { progress ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("第 ${progress.chapter} 章")
                        Text(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(progress.readDate)))
                        progress.note?.let { Text(it, color = Color.Gray) }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("更新进度", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = progressChapter.value, onValueChange = { progressChapter.value = it }, label = { Text("读到第几章") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = progressNote.value, onValueChange = { progressNote.value = it }, label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                val ch = progressChapter.value.toIntOrNull()
                if (ch != null && ch > 0) {
                    viewModel.addProgress(novelId, ch, null, progressNote.value)
                    progressChapter.value = ""; progressNote.value = ""
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = progressChapter.value.isNotBlank()) { Text("保存进度") }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.deleteNovel(entry) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除此条目") }
        }
    }
}
