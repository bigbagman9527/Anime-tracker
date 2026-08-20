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

    // 公开方法，供 UI 获取最新进度
    suspend fun getLatestProgressForAnime(animeId: Long) = repository.getLatestProgress(animeId)
}
