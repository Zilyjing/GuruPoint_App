package com.example.gurupoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gurupoint.data.model.*
import com.example.gurupoint.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SelectionUiState {
    object Idle : SelectionUiState()
    object Loading : SelectionUiState()
    data class Success(val selection: Selection) : SelectionUiState()
    data class Error(val message: String) : SelectionUiState()
}

sealed class AdminActionResult {
    object Idle : AdminActionResult()
    object Loading : AdminActionResult()
    data class Success(val message: String) : AdminActionResult()
    data class Error(val message: String) : AdminActionResult()
}

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games.asStateFlow()

    private val _transactions = MutableStateFlow<List<CoinTransaction>>(emptyList())
    val transactions: StateFlow<List<CoinTransaction>> = _transactions.asStateFlow()

    private val _selections = MutableStateFlow<List<Selection>>(emptyList())
    val selections: StateFlow<List<Selection>> = _selections.asStateFlow()

    private val _results = MutableStateFlow<List<ResultItem>>(emptyList())
    val results: StateFlow<List<ResultItem>> = _results.asStateFlow()

    private val _adminLogs = MutableStateFlow<List<AdminLog>>(emptyList())
    val adminLogs: StateFlow<List<AdminLog>> = _adminLogs.asStateFlow()

    private val _selectionUiState = MutableStateFlow<SelectionUiState>(SelectionUiState.Idle)
    val selectionUiState: StateFlow<SelectionUiState> = _selectionUiState.asStateFlow()

    private val _adminActionResult = MutableStateFlow<AdminActionResult>(AdminActionResult.Idle)
    val adminActionResult: StateFlow<AdminActionResult> = _adminActionResult.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            userRepository.syncUserAccountWithServer()
            _games.value = userRepository.getGames()
            _transactions.value = userRepository.getCoinTransactions()
            _selections.value = userRepository.getUserSelections()
            _results.value = userRepository.getResults()
            _adminLogs.value = userRepository.getAdminLogs()
        }
    }

    fun submitSelection(
        gameId: String,
        gameName: String,
        mode: SelectionMode,
        number: String,
        coinAmount: Long
    ) {
        submitMultipleSelections(gameId, gameName, mode, listOf(number), coinAmount)
    }

    fun submitMultipleSelections(
        gameId: String,
        gameName: String,
        mode: SelectionMode,
        numbers: List<String>,
        coinsPerNumber: Long
    ) {
        viewModelScope.launch {
            _selectionUiState.value = SelectionUiState.Loading
            val result = userRepository.submitMultipleSelections(gameId, gameName, mode, numbers, coinsPerNumber)
            result.onSuccess { selections ->
                if (selections.isNotEmpty()) {
                    _selectionUiState.value = SelectionUiState.Success(selections.first())
                }
                loadData()
            }.onFailure { exception ->
                _selectionUiState.value = SelectionUiState.Error(exception.message ?: "Selection failed")
            }
        }
    }

    fun declareResult(gameId: String, declaredNumber: String, sourceName: String) {
        viewModelScope.launch {
            _adminActionResult.value = AdminActionResult.Loading
            val res = userRepository.declareResultAndCalculateRewards(gameId, declaredNumber, sourceName)
            res.onSuccess { msg ->
                _adminActionResult.value = AdminActionResult.Success(msg)
                loadData()
            }.onFailure { err ->
                _adminActionResult.value = AdminActionResult.Error(err.message ?: "Failed to declare result")
            }
        }
    }

    fun toggleGameStatus(gameId: String, isOpen: Boolean) {
        viewModelScope.launch {
            userRepository.toggleGameOpenStatus(gameId, isOpen)
            loadData()
        }
    }

    fun saveGame(game: Game) {
        viewModelScope.launch {
            _adminActionResult.value = AdminActionResult.Loading
            userRepository.saveGame(game)
            _adminActionResult.value = AdminActionResult.Success("Game saved successfully")
            loadData()
        }
    }

    fun adminAdjustCoins(targetUserId: String, targetUserMobile: String, amount: Long, isAdd: Boolean, reason: String) {
        viewModelScope.launch {
            _adminActionResult.value = AdminActionResult.Loading
            val res = userRepository.adminAdjustCoins(targetUserId, targetUserMobile, amount, isAdd, reason)
            res.onSuccess {
                _adminActionResult.value = AdminActionResult.Success("Coins adjusted successfully")
                loadData()
            }.onFailure { err ->
                _adminActionResult.value = AdminActionResult.Error(err.message ?: "Coin adjustment failed")
            }
        }
    }

    fun resetSelectionState() {
        _selectionUiState.value = SelectionUiState.Idle
    }

    fun resetAdminActionResult() {
        _adminActionResult.value = AdminActionResult.Idle
    }
}
