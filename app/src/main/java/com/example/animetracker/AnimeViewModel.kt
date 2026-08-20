package com.example.animetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animetracker.data.AnimeRepository
import com.example.animetracker.data.local.AnimeEntry
import com.example.animetracker.data.local.AnimeProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnimeFormState(
    val name: String = "",
    val nameCn: String = "",
    val totalEpisodes: String = "",
    val currentEpisode: String = "",
    val status: String = "watching",
    val note: String = ""
)

class AnimeViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    val allAnime = repository.getAllAnime().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(AnimeFormState())
    val formState: StateFlow<AnimeFormState> = _formState

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    // 当前选中的番剧 ID，用于详情页
    private val _selectedAnimeId = MutableStateFlow<Long?>(null)
    val selectedAnimeId: StateFlow<Long?> = _selectedAnimeId

    fun updateName(value: String) { _formState.update { it.copy(name = value) } }
    fun updateNameCn(value: String) { _formState.update { it.copy(nameCn = value) } }
    fun updateTotalEpisodes(value: String) { _formState.update { it.copy(totalEpisodes = value) } }
    fun updateCurrentEpisode(value: String) { _formState.update { it.copy(currentEpisode = value) } }
    fun updateStatus(value: String) { _formState.update { it.copy(status = value) } }
    fun updateNote(value: String) { _formState.update { it.copy(note = value) } }

    fun saveAnime() {
        val form = _formState.value
        if (form.name.isBlank()) return

        viewModelScope.launch {
            val animeId = repository.saveAnime(
                AnimeEntry(
                    name = form.name.trim(),
                    nameCn = form.nameCn.trim().ifBlank { null },
                    episodes = form.totalEpisodes.toIntOrNull(),
                    status = form.status
                )
            )
            val currentEp = form.currentEpisode.toIntOrNull()
            if (currentEp != null && currentEp > 0) {
                repository.addProgress(
                    AnimeProgress(
                        animeId = animeId,
                        episode = currentEp,
                        watchedDate = System.currentTimeMillis(),
                        note = form.note.trim().ifBlank { null }
                    )
                )
            }
            _formState.value = AnimeFormState()
            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }

    // 选择某个番剧，跳转详情
    fun selectAnime(id: Long) { _selectedAnimeId.value = id }
    fun clearSelection() { _selectedAnimeId.value = null }

    // 获取单个番剧的 Flow（用于详情页）
    fun getAnimeFlow(id: Long): Flow<AnimeEntry?> = flow {
        emit(repository.getAnimeById(id))
    }

    // 获取进度历史 Flow
    fun getProgressFlow(animeId: Long): Flow<List<AnimeProgress>> =
        repository.getProgressForAnime(animeId)

    // 添加进度记录
    fun addProgress(animeId: Long, episode: Int, note: String?) {
        viewModelScope.launch {
            repository.addProgress(
                AnimeProgress(
                    animeId = animeId,
                    episode = episode,
                    watchedDate = System.currentTimeMillis(),
                    note = note?.trim()?.ifBlank { null }
                )
            )
        }
    }

    suspend fun getLatestProgressForAnime(animeId: Long) = repository.getLatestProgress(animeId)
}
