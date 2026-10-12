package com.example.gurupoint.data.repository

import com.example.gurupoint.data.local.SessionManager
import com.example.gurupoint.data.model.*
import kotlinx.coroutines.delay

class AgentRepository(private val sessionManager: SessionManager) {

    private val mockAgents = mutableListOf(
        Agent(
            id = "ag_1",
            name = "Royal Agent",
            mobileNumber = "8888888888",
            passwordHash = "1234",
            agentCode = "AG1001",
            status = AgentStatus.ACTIVE,
            coinBalance = 50000L,
            totalCustomers = 12,
            todayEarnings = 450.0,
            totalEarnings = 3200.0,
            createdAt = "2025-02-18"
        ),
        Agent(
            id = "ag_2",
            name = "Golden Star Agent",
            mobileNumber = "7777777777",
            passwordHash = "1234",
            agentCode = "AG1002",
            status = AgentStatus.ACTIVE,
            coinBalance = 25000L,
            totalCustomers = 8,
            todayEarnings = 220.0,
            totalEarnings = 1800.0,
            createdAt = "2025-02-18"
        )
    )

    private val mockAgentTransactions = mutableListOf<AgentCoinTransaction>()
    private val mockEarnings = mutableListOf<AgentEarningRecord>()

    // --- AGENT AUTH ---
    suspend fun agentLogin(mobile: String, pass: String): Result<Agent> {
        delay(600)
        val agent = mockAgents.find { it.mobileNumber == mobile }
            ?: return Result.failure(Exception("Agent account not found! Please contact Master Admin."))

        if (agent.passwordHash != pass) {
            return Result.failure(Exception("Incorrect Agent password!"))
        }

        if (agent.status == AgentStatus.BLOCKED || agent.status == AgentStatus.SUSPENDED) {
            return Result.failure(Exception("Your Agent account is BLOCKED/SUSPENDED by Master Admin!"))
        }

        return Result.success(agent)
    }

    // --- AGENT CODE VERIFICATION ---
    fun verifyAgentCode(agentCode: String): Agent? {
        val trimmed = agentCode.trim()
        val agent = mockAgents.find { it.agentCode.equals(trimmed, ignoreCase = true) }
        return if (agent != null && agent.status == AgentStatus.ACTIVE) agent else null
    }

    // --- MASTER ADMIN AGENT CONTROLS ---
    suspend fun createAgent(name: String, mobile: String, pass: String): Result<Agent> {
        delay(500)
        if (mockAgents.any { it.mobileNumber == mobile }) {
            return Result.failure(Exception("Agent with this mobile already exists!"))
        }

        val newCode = "AG${1001 + mockAgents.size}"
        val newAgent = Agent(
            id = "ag_${System.currentTimeMillis()}",
            name = name,
            mobileNumber = mobile,
            passwordHash = pass,
            agentCode = newCode,
            status = AgentStatus.ACTIVE,
            coinBalance = 10000L, // Initial allocation from Unlimited Master Admin source
            createdAt = "2025-02-18"
        )
        mockAgents.add(0, newAgent)

        return Result.success(newAgent)
    }

    suspend fun getAgents(): List<Agent> {
        delay(300)
        return mockAgents
    }

    suspend fun masterAdminTransferCoinsToAgent(agentId: String, coinAmount: Long): Result<Unit> {
        delay(400)
        val idx = mockAgents.indexOfFirst { it.id == agentId }
        if (idx == -1) return Result.failure(Exception("Agent not found"))

        val agent = mockAgents[idx]
        val before = agent.coinBalance
        val after = before + coinAmount

        mockAgents[idx] = agent.copy(coinBalance = after)

        mockAgentTransactions.add(0, AgentCoinTransaction(
            id = "ag_tx_${System.currentTimeMillis()}",
            agentId = agent.id,
            agentCode = agent.agentCode,
            amount = coinAmount,
            type = TransactionType.ADD,
            reason = "Allocated by Master Admin (Unlimited Source)",
            dateTime = "Just Now",
            balanceBefore = before,
            balanceAfter = after
        ))

        return Result.success(Unit)
    }

    // BLOCK AGENT & TRANSFER CUSTOMERS TO MASTER ADMIN
    suspend fun blockAgentAndTransferCustomers(agentId: String): Result<String> {
        delay(600)
        val idx = mockAgents.indexOfFirst { it.id == agentId }
        if (idx == -1) return Result.failure(Exception("Agent not found"))

        val agent = mockAgents[idx]
        val undistributedCoins = agent.coinBalance

        // 1. Block Agent and recover undistributed coins back to Master Admin
        mockAgents[idx] = agent.copy(
            status = AgentStatus.BLOCKED,
            coinBalance = 0L
        )

        // 2. Transfer assigned customers to Master Admin / Unassigned
        val allUsers = sessionManager.getRegisteredAccountsMap()
        var transferredCount = 0
        allUsers.values.forEach { acc ->
            if (acc.assignedAgentCode.equals(agent.agentCode, ignoreCase = true)) {
                transferredCount++
                val updated = acc.copy(assignedAgentCode = "MASTER_ADMIN")
                sessionManager.saveOrUpdateRegisteredAccount(updated)
            }
        }

        return Result.success("Agent ${agent.name} BLOCKED! $transferredCount Customers transferred to Master Admin. $undistributedCoins undistributed coins returned to Master Admin.")
    }

    // --- AGENT CUSTOMER COIN DISTRIBUTION ---
    suspend fun agentDistributeCoinsToCustomer(
        agentId: String,
        customerMobile: String,
        coinAmount: Long
    ): Result<Unit> {
        delay(500)
        val idx = mockAgents.indexOfFirst { it.id == agentId }
        if (idx == -1) return Result.failure(Exception("Agent account not found"))

        val agent = mockAgents[idx]
        if (agent.status != AgentStatus.ACTIVE) {
            return Result.failure(Exception("Your Agent account is inactive/blocked!"))
        }

        if (agent.coinBalance < coinAmount) {
            return Result.failure(Exception("Insufficient Agent Wallet Balance! Balance: ${agent.coinBalance} Coins"))
        }

        val customer = sessionManager.getRegisteredAccount(customerMobile)
            ?: return Result.failure(Exception("Customer with mobile $customerMobile not found!"))

        // Deduct from Agent Wallet
        val agentBefore = agent.coinBalance
        val agentAfter = agentBefore - coinAmount
        mockAgents[idx] = agent.copy(coinBalance = agentAfter)

        // Add to Customer Wallet
        val custBefore = customer.coinBalance
        val custAfter = custBefore + coinAmount
        val updatedCustomer = customer.copy(coinBalance = custAfter)
        sessionManager.saveOrUpdateRegisteredAccount(updatedCustomer)

        // Record Agent Transaction Ledger
        mockAgentTransactions.add(0, AgentCoinTransaction(
            id = "ag_dist_${System.currentTimeMillis()}",
            agentId = agent.id,
            agentCode = agent.agentCode,
            amount = coinAmount,
            type = TransactionType.DEDUCT,
            targetCustomerMobile = customerMobile,
            reason = "Coins Distributed to Customer $customerMobile",
            dateTime = "Just Now",
            balanceBefore = agentBefore,
            balanceAfter = agentAfter
        ))

        // Record Customer Transaction Ledger
        sessionManager.addTransactionForUser(updatedCustomer.id, CoinTransaction(
            id = "tx_ag_credit_${System.currentTimeMillis()}",
            userId = updatedCustomer.id,
            amount = coinAmount,
            type = TransactionType.ADD,
            reason = "Coins Received from Agent ${agent.name} (${agent.agentCode})",
            dateTime = "Just Now",
            balanceBefore = custBefore,
            balanceAfter = custAfter
        ))

        return Result.success(Unit)
    }

    suspend fun getAgentCustomers(agentCode: String): List<RegisteredAccount> {
        delay(300)
        val allUsers = sessionManager.getRegisteredAccountsMap()
        return allUsers.values.filter { it.assignedAgentCode.equals(agentCode, ignoreCase = true) }
    }

    suspend fun getAgentTransactions(agentId: String): List<AgentCoinTransaction> {
        delay(300)
        return mockAgentTransactions.filter { it.agentId == agentId }
    }
}
