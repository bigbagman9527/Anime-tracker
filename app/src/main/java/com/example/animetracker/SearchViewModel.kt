package com.example.animetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animetracker.data.AnimeRepository
import com.example.animetracker.data.remote.SubjectItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<SubjectItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { keyword ->
                    search(keyword)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, results = emptyList(), error = null) }
        queryFlow.value = newQuery
    }

    private suspend fun search(keyword: String) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val response = repository.searchAnime(keyword)
            val items = response.data ?: emptyList()
            _uiState.update { it.copy(isLoading = false, results = items) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "搜索失败：${e.message}") }
        }
    }
}
