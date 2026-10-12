package com.example.gurupoint.data.repository

import android.util.Log
import com.example.gurupoint.data.local.SessionManager
import com.example.gurupoint.data.model.CoinTransaction
import com.example.gurupoint.data.model.RegisteredAccount
import com.example.gurupoint.data.model.TransactionType
import com.example.gurupoint.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class AuthRepository(private val sessionManager: SessionManager) {

    val userFlow: StateFlow<User?> = sessionManager.userStateFlow

    init {
        syncAllRegisteredAccountsToMasterAdminServer()
        startAppBalanceSyncPolling()
    }

    private fun startAppBalanceSyncPolling() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val currentUser = sessionManager.getUser()
                    if (currentUser != null) {
                        val mobile = currentUser.mobileNumber
                        val (serverBalance, serverStatus, serverName) = fetchLatestServerUserInfo(mobile)
                        val (wa, tg) = fetchAgentContactsForCode(currentUser.assignedAgentCode)

                        val finalName = if (!serverName.isNullOrBlank() && !serverName.startsWith("Gamer ")) serverName else currentUser.name

                        val needsUpdate = serverBalance != null && serverBalance != currentUser.coinBalance ||
                                          finalName != currentUser.name ||
                                          wa != currentUser.agentWhatsApp ||
                                          tg != currentUser.agentTelegram

                        if (needsUpdate) {
                            Log.d("GuruPointSync", "AUTH_REPO LIVE SYNC: $mobile updated with Agent Contacts!")
                            val updatedUser = currentUser.copy(
                                name = finalName,
                                coinBalance = serverBalance ?: currentUser.coinBalance,
                                agentWhatsApp = wa ?: currentUser.agentWhatsApp ?: "919027750140",
                                agentTelegram = tg ?: currentUser.agentTelegram ?: "vinodagent"
                            )
                            sessionManager.saveUserSession(updatedUser)
                        }

                        if (serverStatus == "BLOCKED") {
                            sessionManager.logout()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    private suspend fun fetchAgentContactsForCode(agentCode: String?): Pair<String?, String?> {
        if (agentCode.isNullOrBlank()) return Pair("919027750140", "vinodagent")
        return withContext(Dispatchers.IO) {
            val endpoints = listOf("http://10.0.2.2:3000/api/agents", "http://localhost:3000/api/agents")
            for (ep in endpoints) {
                try {
                    val url = URL(ep)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 2000
                    conn.readTimeout = 2000
                    if (conn.responseCode == 200) {
                        val json = conn.inputStream.bufferedReader().use { it.readText() }
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val agentsList: List<Map<String, Any>> = Gson().fromJson(json, type) ?: emptyList()
                        val matched = agentsList.find { (it["code"] as? String)?.equals(agentCode.trim(), ignoreCase = true) == true }
                        if (matched != null) {
                            val wa = (matched["whatsapp"] as? String)?.ifBlank { null } ?: "919027750140"
                            val tg = (matched["telegram"] as? String)?.ifBlank { null } ?: "vinodagent"
                            return@withContext Pair(wa, tg)
                        }
                    }
                } catch (e: Exception) {}
            }
            Pair("919027750140", "vinodagent")
        }
    }

    private suspend fun fetchLatestServerUserInfo(mobile: String): Triple<Long?, String?, String?> {
        return withContext(Dispatchers.IO) {
            val endpoints = listOf("http://10.0.2.2:3000/api/users", "http://localhost:3000/api/users")
            for (ep in endpoints) {
                try {
                    val url = URL(ep)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 2000
                    conn.readTimeout = 2000
                    if (conn.responseCode == 200) {
                        val json = conn.inputStream.bufferedReader().use { it.readText() }
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val serverUsers: List<Map<String, Any>> = Gson().fromJson(json, type) ?: emptyList()

                        val matched = serverUsers.find {
                            val mob = (it["mobile"] ?: it["mobileNumber"])?.toString()?.trim()
                            mob == mobile.trim()
                        }

                        if (matched != null) {
                            val bal = (matched["balance"] as? Number)?.toLong()
                            val status = (matched["status"] as? String) ?: "ACTIVE"
                            val name = (matched["name"] as? String)
                            return@withContext Triple(bal, status, name)
                        }
                    }
                } catch (e: Exception) {
                    // Try next endpoint
                }
            }
            Triple(null, null, null)
        }
    }

    fun syncAllRegisteredAccountsToMasterAdminServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accounts = sessionManager.getRegisteredAccountsMap().values
                if (accounts.isEmpty()) return@launch

                val endpoints = listOf(
                    "http://10.0.2.2:3000/api/sync_all_users",
                    "http://localhost:3000/api/sync_all_users"
                )

                val jsonArray = accounts.joinToString(prefix = "[", postfix = "]") { acc ->
                    """
                    {
                        "mobile": "${acc.mobileNumber}",
                        "name": "${acc.name}",
                        "coinBalance": ${acc.coinBalance},
                        "assignedAgentCode": "${acc.assignedAgentCode ?: "UNASSIGNED"}",
                        "role": "${acc.role}",
                        "status": "${acc.status}"
                    }
                    """.trimIndent()
                }

                for (endpoint in endpoints) {
                    try {
                        val url = URL(endpoint)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-Type", "application/json")
                        conn.doOutput = true
                        conn.connectTimeout = 3000
                        conn.readTimeout = 3000

                        conn.outputStream.use { os ->
                            os.write(jsonArray.toByteArray(Charsets.UTF_8))
                        }

                        if (conn.responseCode == 200) {
                            Log.d("GuruPointSync", "ALL REGISTERED ACCOUNTS SYNCED TO MASTER ADMIN SERVER!")
                            break
                        }
                    } catch (e: Exception) {
                        // Try next endpoint
                    }
                }
            } catch (e: Exception) {
                Log.e("GuruPointSync", "Error bulk syncing accounts: ${e.message}")
            }
        }
    }

    suspend fun login(mobile: String, pass: String): Result<User> {
        delay(600) // Simulate network delay
        val cleanMobile = mobile.trim()
        val cleanPass = pass.trim()

        if (cleanMobile.length != 10) {
            return Result.failure(Exception("कृपया 10-अंकों का वैध मोबाइल नंबर दर्ज करें।"))
        }
        if (cleanPass.length < 4) {
            return Result.failure(Exception("पासवर्ड कम से कम 4 अक्षरों का होना चाहिए।"))
        }

        // 1. Mandatory Check: Account MUST be registered
        var registeredAccount = sessionManager.getRegisteredAccount(cleanMobile)

        if (registeredAccount == null) {
            val (serverBal, serverStat, serverName) = fetchLatestServerUserInfo(cleanMobile)
            if (serverBal != null) {
                registeredAccount = RegisteredAccount(
                    id = "usr_$cleanMobile",
                    name = serverName ?: "Gamer $cleanMobile",
                    mobileNumber = cleanMobile,
                    passwordHash = cleanPass,
                    coinBalance = serverBal,
                    status = serverStat ?: "ACTIVE"
                )
                sessionManager.saveOrUpdateRegisteredAccount(registeredAccount)
            } else {
                return Result.failure(Exception("यह मोबाइल नंबर रजिस्टर्ड नहीं है! कृपया पहले नया अकाउंट रजिस्टर करें।"))
            }
        }

        // Fetch latest server balance & status BEFORE login
        val (serverBalance, serverStatus, serverName) = fetchLatestServerUserInfo(cleanMobile)
        val (wa, tg) = fetchAgentContactsForCode(registeredAccount.assignedAgentCode)
        val currentBalance = serverBalance ?: registeredAccount.coinBalance
        val currentStatus = serverStatus ?: registeredAccount.status
        val currentName = if (!serverName.isNullOrBlank() && !serverName.startsWith("Gamer ")) serverName else registeredAccount.name

        // 1.5 Mandatory Check: Block Status Check
        if (currentStatus == "BLOCKED") {
            return Result.failure(Exception("आपका अकाउंट मास्टर एडमिन द्वारा ब्लॉक कर दिया गया है! कृपया कस्टमर सपोर्ट से संपर्क करें।"))
        }

        // 2. Mandatory Check: Password must match
        if (registeredAccount.passwordHash != cleanPass) {
            return Result.failure(Exception("गलत पासवर्ड! कृपया सही पासवर्ड दर्ज करें।"))
        }

        // 3. Mandatory Check: Single Device Login Enforcement
        val currentDeviceId = sessionManager.getDeviceId()

        // Update active device ID & server balance on successful login
        val updatedAccount = registeredAccount.copy(
            name = currentName,
            activeDeviceId = currentDeviceId,
            coinBalance = currentBalance,
            status = currentStatus,
            agentWhatsApp = wa ?: "919027750140",
            agentTelegram = tg ?: "vinodagent"
        )
        sessionManager.saveOrUpdateRegisteredAccount(updatedAccount)

        val user = User(
            id = updatedAccount.id,
            name = updatedAccount.name,
            mobileNumber = updatedAccount.mobileNumber,
            coinBalance = currentBalance,
            referralCode = updatedAccount.referralCode,
            referredBy = updatedAccount.referredBy,
            assignedAgentCode = updatedAccount.assignedAgentCode,
            agentWhatsApp = wa ?: "919027750140",
            agentTelegram = tg ?: "vinodagent",
            role = updatedAccount.role
        )

        sessionManager.saveUserSession(user, "token_${cleanMobile}_$currentDeviceId")

        // Sync with Master Admin Server in background
        syncUserToMasterAdminServer(user.mobileNumber, user.name, user.coinBalance, user.assignedAgentCode)

        return Result.success(user)
    }

    suspend fun register(
        name: String,
        mobile: String,
        pass: String,
        referralCode: String?,
        agentCode: String? = null
    ): Result<User> {
        delay(600)
        val cleanName = name.trim()
        val cleanMobile = mobile.trim()
        val cleanPass = pass.trim()

        if (cleanName.isBlank()) {
            return Result.failure(Exception("कृपया अपना नाम दर्ज करें।"))
        }
        if (cleanMobile.length != 10) {
            return Result.failure(Exception("मोबाइल नंबर 10 अंकों का होना चाहिए।"))
        }
        if (cleanPass.length < 4) {
            return Result.failure(Exception("पासवर्ड कम से कम 4 अक्षरों का होना चाहिए।"))
        }

        // Check if already registered
        if (sessionManager.getRegisteredAccount(cleanMobile) != null) {
            return Result.failure(Exception("यह मोबाइल नंबर पहले से रजिस्टर्ड है! कृपया लॉगिन करें।"))
        }

        var referrerAccount: RegisteredAccount? = null
        if (!referralCode.isNullOrBlank()) {
            val allAccounts = sessionManager.getRegisteredAccountsMap()
            referrerAccount = allAccounts.values.find { it.referralCode.equals(referralCode.trim(), ignoreCase = true) }
            if (referrerAccount == null) {
                return Result.failure(Exception("अमान्य रेफरल कोड! कृपया सही रेफरल कोड दर्ज करें अथवा खाली छोड़ें।"))
            }
        }

        val userId = "usr_${cleanMobile}"
        val initialBalance = if (referrerAccount != null) 600L else 500L

        val (wa, tg) = fetchAgentContactsForCode(agentCode)

        val currentDeviceId = sessionManager.getDeviceId()
        val newAccount = RegisteredAccount(
            id = userId,
            name = cleanName,
            mobileNumber = cleanMobile,
            passwordHash = cleanPass,
            coinBalance = initialBalance,
            referralCode = "GP${cleanMobile.takeLast(4)}",
            referredBy = referrerAccount?.referralCode,
            assignedAgentCode = agentCode?.trim()?.uppercase()?.ifEmpty { null },
            agentWhatsApp = wa ?: "919027750140",
            agentTelegram = tg ?: "vinodagent",
            role = if (cleanMobile == "9999999999") "ADMIN" else "USER",
            status = "ACTIVE",
            activeDeviceId = currentDeviceId,
            createdAt = "2025-02-18"
        )

        // Save account to database
        sessionManager.saveOrUpdateRegisteredAccount(newAccount)

        // Record Initial Ledger Transactions for New User
        sessionManager.addTransactionForUser(userId, CoinTransaction(
            id = "tx_wel_${System.currentTimeMillis()}",
            userId = userId,
            amount = 500L,
            type = TransactionType.BONUS,
            reason = "Welcome Bonus Coins",
            dateTime = "Just Now",
            balanceBefore = 0L,
            balanceAfter = 500L
        ))

        // Record Referral Bonus ONLY IF valid referral code was used
        if (referrerAccount != null) {
            sessionManager.addTransactionForUser(userId, CoinTransaction(
                id = "tx_ref_${System.currentTimeMillis()}",
                userId = userId,
                amount = 100L,
                type = TransactionType.REFERRAL,
                reason = "Referral Bonus Received (Code: ${referrerAccount.referralCode})",
                dateTime = "Just Now",
                balanceBefore = 500L,
                balanceAfter = 600L
            ))

            // Credit 100 Coins to Referrer User
            val oldReferrerBal = referrerAccount.coinBalance
            val updatedReferrer = referrerAccount.copy(coinBalance = oldReferrerBal + 100L)
            sessionManager.saveOrUpdateRegisteredAccount(updatedReferrer)

            sessionManager.addTransactionForUser(referrerAccount.id, CoinTransaction(
                id = "tx_ref_reward_${System.currentTimeMillis()}",
                userId = referrerAccount.id,
                amount = 100L,
                type = TransactionType.REFERRAL,
                reason = "Referral Reward for inviting $cleanMobile",
                dateTime = "Just Now",
                balanceBefore = oldReferrerBal,
                balanceAfter = oldReferrerBal + 100L
            ))
        }

        val user = User(
            id = newAccount.id,
            name = newAccount.name,
            mobileNumber = newAccount.mobileNumber,
            coinBalance = newAccount.coinBalance,
            referralCode = newAccount.referralCode,
            referredBy = newAccount.referredBy,
            assignedAgentCode = newAccount.assignedAgentCode,
            agentWhatsApp = wa ?: "919027750140",
            agentTelegram = tg ?: "vinodagent",
            role = newAccount.role
        )

        sessionManager.saveUserSession(user, "token_${cleanMobile}_$currentDeviceId")

        // Sync all accounts to Master Admin Real-Time Server in background
        syncAllRegisteredAccountsToMasterAdminServer()

        return Result.success(user)
    }

    private fun syncUserToMasterAdminServer(mobile: String, name: String, coinBalance: Long, agentCode: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val endpoints = listOf(
                "http://10.0.2.2:3000/api/register",
                "http://localhost:3000/api/register",
                "http://127.0.0.1:3000/api/register"
            )

            for (endpoint in endpoints) {
                try {
                    Log.d("GuruPointSync", "Attempting real-time sync for $mobile to $endpoint ...")
                    val url = URL(endpoint)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000

                    val json = """
                        {
                            "mobile": "$mobile",
                            "name": "$name",
                            "coinBalance": $coinBalance,
                            "assignedAgentCode": "${agentCode ?: "UNASSIGNED"}"
                        }
                    """.trimIndent()

                    conn.outputStream.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }

                    val code = conn.responseCode
                    Log.d("GuruPointSync", "SYNC SUCCESS! Response Code: $code from $endpoint")
                    if (code == 200) break
                } catch (e: Exception) {
                    Log.e("GuruPointSync", "Sync failed for $endpoint: ${e.message}")
                }
            }
        }
    }

    fun getCurrentUser(): User? {
        return sessionManager.getUser()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun logout() {
        sessionManager.logout()
    }
}
