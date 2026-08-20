package com.example.animetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animetracker.data.NovelRepository
import com.example.animetracker.data.local.NovelEntry
import com.example.animetracker.data.local.NovelProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NovelFormState(
    val title: String = "",
    val author: String = "",
    val totalChapters: String = "",
    val currentChapter: String = "",
    val status: String = "reading",
    val note: String = ""
)

class NovelViewModel(
    private val repository: NovelRepository
) : ViewModel() {

    val allNovels = repository.getAllNovels().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(NovelFormState())
    val formState: StateFlow<NovelFormState> = _formState

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _selectedNovelId = MutableStateFlow<Long?>(null)
    val selectedNovelId: StateFlow<Long?> = _selectedNovelId

    fun updateTitle(value: String) { _formState.update { it.copy(title = value) } }
    fun updateAuthor(value: String) { _formState.update { it.copy(author = value) } }
    fun updateTotalChapters(value: String) { _formState.update { it.copy(totalChapters = value) } }
    fun updateCurrentChapter(value: String) { _formState.update { it.copy(currentChapter = value) } }
    fun updateStatus(value: String) { _formState.update { it.copy(status = value) } }
    fun updateNote(value: String) { _formState.update { it.copy(note = value) } }

    fun saveNovel() {
        val form = _formState.value
        if (form.title.isBlank()) return

        viewModelScope.launch {
            val novelId = repository.saveNovel(
                NovelEntry(
                    title = form.title.trim(),
                    author = form.author.trim().ifBlank { null },
                    totalChapters = form.totalChapters.toIntOrNull(),
                    status = form.status
                )
            )
            val currentCh = form.currentChapter.toIntOrNull()
            if (currentCh != null && currentCh > 0) {
                repository.addProgress(
                    NovelProgress(
                        novelId = novelId,
                        chapter = currentCh,
                        page = null,
                        readDate = System.currentTimeMillis(),
                        note = form.note.trim().ifBlank { null }
                    )
                )
            }
            _formState.value = NovelFormState()
            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }

    fun selectNovel(id: Long) { _selectedNovelId.value = id }
    fun clearSelection() { _selectedNovelId.value = null }

    fun getNovelFlow(id: Long): Flow<NovelEntry?> = flow {
        emit(repository.getNovelById(id))
    }

    fun getProgressFlow(novelId: Long): Flow<List<NovelProgress>> =
        repository.getProgressForNovel(novelId)

    fun addProgress(novelId: Long, chapter: Int?, page: Int?, note: String?) {
        viewModelScope.launch {
            repository.addProgress(
                NovelProgress(
                    novelId = novelId,
                    chapter = chapter,
                    page = page,
                    readDate = System.currentTimeMillis(),
                    note = note?.trim()?.ifBlank { null }
                )
            )
        }
    }

    suspend fun getLatestProgress(novelId: Long) = repository.getLatestProgress(novelId)

    fun updateNovelStatus(novel: NovelEntry, newStatus: String) {
        viewModelScope.launch {
            repository.updateNovel(novel.copy(status = newStatus))
        }
    }

    fun deleteNovel(novel: NovelEntry) {
        viewModelScope.launch {
            repository.deleteNovel(novel)
            _selectedNovelId.value = null
        }
    }
}
