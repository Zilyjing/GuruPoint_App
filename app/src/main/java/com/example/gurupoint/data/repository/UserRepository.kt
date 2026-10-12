package com.example.gurupoint.data.repository

import android.util.Log
import com.example.gurupoint.data.local.SessionManager
import com.example.gurupoint.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class UserRepository(private val sessionManager: SessionManager) {

    // Initial games matching Master List & Satta-Results.com
    private val mockGames = mutableListOf(
        Game("g1", "Delhi Bazar", "Delhi Bazar Live Draw", null, "10:00 AM", "02:30 PM", "03:00 PM", isOpen = true, latestResult = "91"),
        Game("g2", "Shree Ganesh", "Shree Ganesh Daily Draw", null, "11:00 AM", "04:15 PM", "04:30 PM", isOpen = true, latestResult = "47"),
        Game("g3", "Faridabad", "Faridabad Evening Draw", null, "12:00 PM", "05:45 PM", "06:15 PM", isOpen = true, latestResult = "36"),
        Game("g4", "Ghaziabad", "Ghaziabad Night Draw", null, "01:00 PM", "08:15 PM", "08:30 PM", isOpen = true, latestResult = "95"),
        Game("g5", "Gali", "Gali Midnight Draw", null, "02:00 PM", "11:00 PM", "11:30 PM", isOpen = true, latestResult = "68"),
        Game("g6", "Disawar", "Disawar Early Morning Draw", null, "03:00 PM", "03:00 AM", "05:00 AM", isOpen = false, latestResult = "15")
    )

    private val mockSelections = mutableListOf<Selection>()

    private val mockResults = mutableListOf(
        ResultItem("res_1", "g1", "Delhi Bazar", "2025-02-18", "03:00 PM", "91", isDeclared = true, sourceName = "https://satta-results.com/"),
        ResultItem("res_2", "g2", "Shree Ganesh", "2025-02-18", "04:30 PM", "47", isDeclared = true, sourceName = "https://satta-results.com/"),
        ResultItem("res_3", "g3", "Faridabad", "2025-02-18", "06:15 PM", "36", isDeclared = true, sourceName = "https://satta-results.com/"),
        ResultItem("res_4", "g4", "Ghaziabad", "2025-02-18", "08:30 PM", "95", isDeclared = true, sourceName = "https://satta-results.com/"),
        ResultItem("res_5", "g5", "Gali", "2025-02-18", "11:30 PM", "68", isDeclared = true, sourceName = "https://satta-results.com/"),
        ResultItem("res_6", "g6", "Disawar", "2025-02-18", "05:00 AM", "15", isDeclared = true, sourceName = "https://satta-results.com/")
    )

    private val mockAdminLogs = mutableListOf<AdminLog>()

    init {
        startContinuousServerBalancePolling()
    }

    private fun startContinuousServerBalancePolling() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    syncUserAccountWithServer()
                } catch (e: Exception) {
                    // Background poll
                }
                delay(2000) // Poll every 2 seconds for live balance updates from Master Admin Server
            }
        }
    }

    fun syncUserAccountWithServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!sessionManager.isLoggedIn()) return@launch

                val currentUser = sessionManager.getUser() ?: return@launch
                val mobile = currentUser.mobileNumber

                val endpoints = listOf(
                    "http://10.0.2.2:3000/api/users",
                    "http://localhost:3000/api/users"
                )

                for (endpoint in endpoints) {
                    try {
                        val url = URL(endpoint)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.connectTimeout = 3000
                        conn.readTimeout = 3000

                        if (conn.responseCode == 200) {
                            val json = conn.inputStream.bufferedReader().use { it.readText() }
                            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                            val serverUsers: List<Map<String, Any>> = Gson().fromJson(json, type) ?: emptyList()

                            val matched = serverUsers.find {
                                val mob = (it["mobile"] ?: it["mobileNumber"])?.toString()?.trim()
                                mob == mobile.trim()
                            }

                            if (matched != null) {
                                if (!sessionManager.isLoggedIn()) return@launch

                                val serverBalance = (matched["balance"] as? Number)?.toLong() ?: currentUser.coinBalance
                                val serverStatus = (matched["status"] as? String) ?: "ACTIVE"
                                val serverAgent = (matched["agentCode"] as? String) ?: currentUser.assignedAgentCode

                                val registeredAcc = sessionManager.getRegisteredAccount(mobile)
                                if (registeredAcc != null) {
                                    val updatedAcc = registeredAcc.copy(
                                        coinBalance = serverBalance,
                                        status = serverStatus,
                                        assignedAgentCode = serverAgent
                                    )
                                    sessionManager.saveOrUpdateRegisteredAccount(updatedAcc)
                                }

                                if (serverStatus == "BLOCKED") {
                                    sessionManager.logout()
                                    return@launch
                                }

                                if (currentUser.coinBalance != serverBalance && sessionManager.isLoggedIn()) {
                                    val updatedUser = currentUser.copy(
                                        coinBalance = serverBalance,
                                        assignedAgentCode = serverAgent
                                    )
                                    sessionManager.saveUserSession(updatedUser)
                                    Log.d("GuruPointSync", "USER_REPO LIVE SYNC: $mobile balance updated to $serverBalance Coins!")
                                }
                                break
                            }
                        }
                    } catch (e: Exception) {
                        // Try next endpoint
                    }
                }
            } catch (e: Exception) {
                Log.e("GuruPointSync", "Failed to sync user account with server: ${e.message}")
            }
        }
    }

    suspend fun getGames(): List<Game> {
        return withContext(Dispatchers.IO) {
            val endpoints = listOf("http://10.0.2.2:3000/api/games", "http://localhost:3000/api/games")
            for (ep in endpoints) {
                try {
                    val url = URL(ep)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 2500
                    conn.readTimeout = 2500
                    if (conn.responseCode == 200) {
                        val json = conn.inputStream.bufferedReader().use { it.readText() }
                        val type = object : TypeToken<List<Game>>() {}.type
                        val serverGames: List<Game> = Gson().fromJson(json, type) ?: emptyList()
                        if (serverGames.isNotEmpty()) {
                            return@withContext serverGames.filter { !it.isHidden }
                        }
                    }
                } catch (e: Exception) {
                    // Try next
                }
            }
            mockGames.filter { !it.isHidden }
        }
    }

    suspend fun getAllGamesForAdmin(): List<Game> {
        return getGames()
    }

    suspend fun submitSelection(
        gameId: String,
        gameName: String,
        mode: SelectionMode,
        number: String,
        coinAmount: Long
    ): Result<Selection> {
        return submitMultipleSelections(gameId, gameName, mode, listOf(number), coinAmount).map { it.first() }
    }

    suspend fun submitMultipleSelections(
        gameId: String,
        gameName: String,
        mode: SelectionMode,
        numbers: List<String>,
        coinsPerNumber: Long
    ): Result<List<Selection>> {
        delay(500)
        val currentUser = sessionManager.getUser()
            ?: return Result.failure(Exception("User not logged in"))

        if (numbers.isEmpty()) {
            return Result.failure(Exception("Please select at least one number!"))
        }

        val totalCoinsRequired = coinsPerNumber * numbers.size
        if (currentUser.coinBalance < totalCoinsRequired) {
            return Result.failure(Exception("Insufficient Coin Balance! Required: $totalCoinsRequired Coins, Available: ${currentUser.coinBalance} Coins"))
        }

        val gamesList = getGames()
        val game = gamesList.find { it.id == gameId }
        if (game != null && !game.isOpen) {
            return Result.failure(Exception("Time Out! Game is closed for selections."))
        }

        val balanceBefore = currentUser.coinBalance
        val balanceAfter = balanceBefore - totalCoinsRequired

        val updatedUser = currentUser.copy(coinBalance = balanceAfter)
        sessionManager.saveUserSession(updatedUser)

        val tx = CoinTransaction(
            id = "tx_${System.currentTimeMillis()}",
            userId = currentUser.id,
            amount = totalCoinsRequired,
            type = TransactionType.GAME_SELECTION,
            reason = "Selection on $gameName (${numbers.joinToString(", ")})",
            dateTime = "Just Now",
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter
        )
        sessionManager.addTransactionForUser(currentUser.id, tx)

        val createdSelections = numbers.mapIndexed { idx, num ->
            Selection(
                id = "sel_${System.currentTimeMillis()}_$idx",
                userId = currentUser.id,
                gameId = gameId,
                gameName = gameName,
                mode = mode,
                selectedNumber = num,
                coinQuantity = coinsPerNumber,
                potentialReward = coinsPerNumber * (if (mode == SelectionMode.MODE_A) 90 else 9),
                status = SelectionStatus.PENDING,
                createdAt = "Today"
            )
        }

        createdSelections.forEach { mockSelections.add(0, it) }

        return Result.success(createdSelections)
    }

    // --- REWARD & RESULT ENGINE ---
    suspend fun declareResultAndCalculateRewards(
        gameId: String,
        declaredNumber: String,
        sourceName: String = "https://satta-results.com/"
    ): Result<String> {
        delay(600)
        val gamesList = getGames()
        val game = gamesList.find { it.id == gameId }
            ?: return Result.failure(Exception("Game not found"))

        // Save Result Item
        val resultItem = ResultItem(
            id = "res_${System.currentTimeMillis()}",
            gameId = gameId,
            gameName = game.name,
            resultDate = "2025-02-18",
            resultTime = game.resultTime,
            declaredNumber = declaredNumber,
            isDeclared = true,
            sourceName = sourceName
        )
        mockResults.add(0, resultItem)

        // Reward Calculation Engine: Process all pending selections
        var winnersCount = 0
        var totalCoinsRewarded = 0L

        val updatedSelections = mockSelections.map { selection ->
            if (selection.gameId == gameId && selection.status == SelectionStatus.PENDING) {
                val isMatch = if (selection.mode == SelectionMode.MODE_A) {
                    selection.selectedNumber == declaredNumber
                } else {
                    selection.selectedNumber == declaredNumber.takeLast(1)
                }

                if (isMatch) {
                    winnersCount++
                    totalCoinsRewarded += selection.potentialReward

                    val currentUser = sessionManager.getUser()
                    if (currentUser != null && currentUser.id == selection.userId) {
                        val before = currentUser.coinBalance
                        val after = before + selection.potentialReward
                        val updated = currentUser.copy(coinBalance = after)
                        sessionManager.saveUserSession(updated)

                        sessionManager.addTransactionForUser(currentUser.id, CoinTransaction(
                            id = "tx_rw_${System.currentTimeMillis()}",
                            userId = currentUser.id,
                            amount = selection.potentialReward,
                            type = TransactionType.REWARD,
                            reason = "Winning Reward on ${game.name} (#${selection.selectedNumber})",
                            dateTime = "Just Now",
                            balanceBefore = before,
                            balanceAfter = after
                        ))
                    }

                    selection.copy(status = SelectionStatus.WON)
                } else {
                    selection.copy(status = SelectionStatus.LOST)
                }
            } else {
                selection
            }
        }

        mockSelections.clear()
        mockSelections.addAll(updatedSelections)

        mockAdminLogs.add(0, AdminLog(
            id = "log_${System.currentTimeMillis()}",
            adminMobile = sessionManager.getUser()?.mobileNumber ?: "Admin",
            action = "DECLARE_RESULT",
            targetType = "GAME",
            targetId = gameId,
            details = "Declared $declaredNumber for ${game.name}. Winners: $winnersCount, Coins Rewarded: $totalCoinsRewarded",
            timestamp = "Just Now"
        ))

        return Result.success("Result $declaredNumber declared for ${game.name}! Winners: $winnersCount, Total Rewarded: $totalCoinsRewarded Coins")
    }

    // --- ADMIN GAME MANAGEMENT ---
    suspend fun saveGame(game: Game): Result<Unit> {
        delay(400)
        return Result.success(Unit)
    }

    suspend fun toggleGameOpenStatus(gameId: String, isOpen: Boolean): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    // --- ADMIN COIN MANAGEMENT ---
    suspend fun adminAdjustCoins(
        targetUserId: String,
        targetUserMobile: String,
        amount: Long,
        isAdd: Boolean,
        reason: String
    ): Result<Unit> {
        delay(500)
        val currentUser = sessionManager.getUser()
        val currentBalance = currentUser?.coinBalance ?: 0L
        val balanceAfter = if (isAdd) currentBalance + amount else (currentBalance - amount).coerceAtLeast(0L)

        val updatedUser = currentUser?.copy(coinBalance = balanceAfter)
        if (updatedUser != null) {
            sessionManager.saveUserSession(updatedUser)
        }

        sessionManager.addTransactionForUser(targetUserId, CoinTransaction(
            id = "tx_adm_${System.currentTimeMillis()}",
            userId = targetUserId,
            amount = amount,
            type = if (isAdd) TransactionType.ADD else TransactionType.DEDUCT,
            reason = "Admin Manual Adjustment: $reason",
            dateTime = "Just Now",
            balanceBefore = currentBalance,
            balanceAfter = balanceAfter,
            performedByAdminId = currentUser?.id
        ))

        mockAdminLogs.add(0, AdminLog(
            id = "log_${System.currentTimeMillis()}",
            adminMobile = currentUser?.mobileNumber ?: "Admin",
            action = if (isAdd) "ADD_COINS" else "DEDUCT_COINS",
            targetType = "USER_COIN",
            targetId = targetUserId,
            details = "Adjusted $amount coins for $targetUserMobile. Reason: $reason",
            timestamp = "Just Now"
        ))

        return Result.success(Unit)
    }

    suspend fun getCoinTransactions(): List<CoinTransaction> {
        delay(300)
        val currentUser = sessionManager.getUser() ?: return emptyList()
        return sessionManager.getUserTransactions(currentUser.id)
    }

    suspend fun getUserSelections(): List<Selection> {
        delay(300)
        return mockSelections
    }

    suspend fun getResults(): List<ResultItem> {
        return withContext(Dispatchers.IO) {
            val endpoints = listOf("http://10.0.2.2:3000/api/results", "http://localhost:3000/api/results")
            for (ep in endpoints) {
                try {
                    val url = URL(ep)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 2500
                    conn.readTimeout = 2500
                    if (conn.responseCode == 200) {
                        val json = conn.inputStream.bufferedReader().use { it.readText() }
                        val type = object : TypeToken<List<ResultItem>>() {}.type
                        val serverResults: List<ResultItem> = Gson().fromJson(json, type) ?: emptyList()
                        if (serverResults.isNotEmpty()) {
                            return@withContext serverResults
                        }
                    }
                } catch (e: Exception) {
                    // Try next
                }
            }
            mockResults
        }
    }

    suspend fun getAdminLogs(): List<AdminLog> {
        delay(300)
        return mockAdminLogs
    }
}
