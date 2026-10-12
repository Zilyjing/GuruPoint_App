package com.example.gurupoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gurupoint.data.model.Agent
import com.example.gurupoint.data.model.AgentCoinTransaction
import com.example.gurupoint.data.model.RegisteredAccount
import com.example.gurupoint.data.repository.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AgentUiState {
    object Idle : AgentUiState()
    object Loading : AgentUiState()
    data class Success(val message: String) : AgentUiState()
    data class Error(val message: String) : AgentUiState()
}

class AgentViewModel(private val agentRepository: AgentRepository) : ViewModel() {

    private val _currentAgent = MutableStateFlow<Agent?>(null)
    val currentAgent: StateFlow<Agent?> = _currentAgent.asStateFlow()

    private val _agentList = MutableStateFlow<List<Agent>>(emptyList())
    val agentList: StateFlow<List<Agent>> = _agentList.asStateFlow()

    private val _agentCustomers = MutableStateFlow<List<RegisteredAccount>>(emptyList())
    val agentCustomers: StateFlow<List<RegisteredAccount>> = _agentCustomers.asStateFlow()

    private val _agentTransactions = MutableStateFlow<List<AgentCoinTransaction>>(emptyList())
    val agentTransactions: StateFlow<List<AgentCoinTransaction>> = _agentTransactions.asStateFlow()

    private val _uiState = MutableStateFlow<AgentUiState>(AgentUiState.Idle)
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        loadAgents()
    }

    fun loadAgents() {
        viewModelScope.launch {
            _agentList.value = agentRepository.getAgents()
        }
    }

    fun agentLogin(mobile: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AgentUiState.Loading
            val res = agentRepository.agentLogin(mobile, pass)
            res.onSuccess { agent ->
                _currentAgent.value = agent
                _uiState.value = AgentUiState.Success("Agent Login Successful")
                loadAgentData(agent)
            }.onFailure { err ->
                _uiState.value = AgentUiState.Error(err.message ?: "Agent login failed")
            }
        }
    }

    fun loadAgentData(agent: Agent) {
        viewModelScope.launch {
            _agentCustomers.value = agentRepository.getAgentCustomers(agent.agentCode)
            _agentTransactions.value = agentRepository.getAgentTransactions(agent.id)
        }
    }

    fun createAgent(name: String, mobile: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AgentUiState.Loading
            val res = agentRepository.createAgent(name, mobile, pass)
            res.onSuccess { agent ->
                _uiState.value = AgentUiState.Success("Agent ${agent.name} created! Code: ${agent.agentCode}")
                loadAgents()
            }.onFailure { err ->
                _uiState.value = AgentUiState.Error(err.message ?: "Create agent failed")
            }
        }
    }

    fun masterAdminTransferCoins(agentId: String, amount: Long) {
        viewModelScope.launch {
            _uiState.value = AgentUiState.Loading
            val res = agentRepository.masterAdminTransferCoinsToAgent(agentId, amount)
            res.onSuccess {
                _uiState.value = AgentUiState.Success("Transferred $amount coins to Agent Wallet")
                loadAgents()
            }.onFailure { err ->
                _uiState.value = AgentUiState.Error(err.message ?: "Transfer failed")
            }
        }
    }

    fun blockAgentAndTransferCustomers(agentId: String) {
        viewModelScope.launch {
            _uiState.value = AgentUiState.Loading
            val res = agentRepository.blockAgentAndTransferCustomers(agentId)
            res.onSuccess { msg ->
                _uiState.value = AgentUiState.Success(msg)
                loadAgents()
            }.onFailure { err ->
                _uiState.value = AgentUiState.Error(err.message ?: "Block agent failed")
            }
        }
    }

    fun distributeCoinsToCustomer(customerMobile: String, amount: Long) {
        viewModelScope.launch {
            val agent = _currentAgent.value ?: return@launch
            _uiState.value = AgentUiState.Loading
            val res = agentRepository.agentDistributeCoinsToCustomer(agent.id, customerMobile, amount)
            res.onSuccess {
                _uiState.value = AgentUiState.Success("Distributed $amount coins to Customer $customerMobile")
                // Refresh agent balance
                val updatedList = agentRepository.getAgents()
                _agentList.value = updatedList
                val refreshed = updatedList.find { it.id == agent.id }
                if (refreshed != null) {
                    _currentAgent.value = refreshed
                    loadAgentData(refreshed)
                }
            }.onFailure { err ->
                _uiState.value = AgentUiState.Error(err.message ?: "Distribution failed")
            }
        }
    }

    fun logoutAgent() {
        _currentAgent.value = null
        _uiState.value = AgentUiState.Idle
    }

    fun resetUiState() {
        _uiState.value = AgentUiState.Idle
    }
}
