package com.example.gurupoint.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.provider.Settings
import android.util.Log
import com.example.gurupoint.data.model.CoinTransaction
import com.example.gurupoint.data.model.RegisteredAccount
import com.example.gurupoint.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    @Volatile
    private var isLoggingOut = false

    private val _userStateFlow = MutableStateFlow<User?>(getUserInternal())
    val userStateFlow: StateFlow<User?> = _userStateFlow.asStateFlow()

    companion object {
        private const val PREF_NAME = "gurupoint_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER = "user_data"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_DEVICE_ID = "unique_device_id"
        private const val KEY_REGISTERED_USERS = "registered_users_db"
        private const val KEY_LAST_MOBILE = "last_logged_mobile"
    }

    init {
        seedDefaultAdminAccount()
    }

    fun getDeviceId(): String {
        var devId = prefs.getString(KEY_DEVICE_ID, null)
        if (devId == null) {
            val androidId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                null
            }
            devId = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                androidId
            } else {
                UUID.randomUUID().toString()
            }
            prefs.edit().putString(KEY_DEVICE_ID, devId).apply()
        }
        return devId
    }

    private fun seedDefaultAdminAccount() {
        val users = getRegisteredAccountsMap()
        if (!users.containsKey("9999999999")) {
            val admin = RegisteredAccount(
                id = "usr_9999999999",
                name = "GuruPoint Admin",
                mobileNumber = "9999999999",
                passwordHash = "1234",
                coinBalance = 1000000L,
                referralCode = "GPADMIN",
                role = "ADMIN"
            )
            users["9999999999"] = admin
            saveRegisteredAccountsMap(users)
        }
    }

    fun getRegisteredAccountsMap(): MutableMap<String, RegisteredAccount> {
        val json = prefs.getString(KEY_REGISTERED_USERS, null) ?: return mutableMapOf()
        return try {
            val type = object : TypeToken<MutableMap<String, RegisteredAccount>>() {}.type
            gson.fromJson(json, type) ?: mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun exportRegisteredAccountsToDisk(json: String) {
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val file = File(downloadDir, "gurupoint_users_db.json")
            file.writeText(json)
            Log.d("SessionManager", "Exported users db to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SessionManager", "Disk export failed: ${e.message}")
        }
    }

    fun saveRegisteredAccountsMap(map: Map<String, RegisteredAccount>) {
        val json = gson.toJson(map)
        prefs.edit().putString(KEY_REGISTERED_USERS, json).apply()
        exportRegisteredAccountsToDisk(json)
    }

    fun getRegisteredAccount(mobile: String): RegisteredAccount? {
        return getRegisteredAccountsMap()[mobile]
    }

    fun saveOrUpdateRegisteredAccount(account: RegisteredAccount) {
        val users = getRegisteredAccountsMap()
        users[account.mobileNumber] = account
        saveRegisteredAccountsMap(users)
    }

    // --- USER TRANSACTION LEDGER STORAGE ---
    fun getUserTransactions(userId: String): MutableList<CoinTransaction> {
        val json = prefs.getString("tx_store_$userId", null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<CoinTransaction>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveUserTransactions(userId: String, list: List<CoinTransaction>) {
        val json = gson.toJson(list)
        prefs.edit().putString("tx_store_$userId", json).apply()
    }

    fun addTransactionForUser(userId: String, transaction: CoinTransaction) {
        val list = getUserTransactions(userId)
        list.add(0, transaction)
        saveUserTransactions(userId, list)
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUserSession(user: User, token: String? = null) {
        synchronized(this) {
            if (isLoggingOut) return
            val json = gson.toJson(user)
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER, json)
                .putString(KEY_LAST_MOBILE, user.mobileNumber)
                .commit()

            token?.let { saveAuthToken(it) }
            _userStateFlow.value = user

            // Sync coinBalance and real name back to registered account database
            val registered = getRegisteredAccount(user.mobileNumber)
            if (registered != null) {
                val updated = registered.copy(
                    name = if (user.name.isNotBlank() && !user.name.startsWith("Gamer ")) user.name else registered.name,
                    coinBalance = user.coinBalance
                )
                saveOrUpdateRegisteredAccount(updated)
            }
        }
    }

    private fun getUserInternal(): User? {
        if (isLoggingOut) return null
        val json = prefs.getString(KEY_USER, null)
        if (!json.isNullOrBlank()) {
            try {
                val user = gson.fromJson(json, User::class.java)
                if (user != null && user.mobileNumber.isNotBlank()) {
                    return user
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Fallback to last logged in mobile in registered account database
        val lastMobile = prefs.getString(KEY_LAST_MOBILE, null)
        if (!lastMobile.isNullOrBlank()) {
            val reg = getRegisteredAccount(lastMobile)
            if (reg != null) {
                return User(
                    id = reg.id,
                    name = reg.name,
                    mobileNumber = reg.mobileNumber,
                    coinBalance = reg.coinBalance,
                    referralCode = reg.referralCode,
                    referredBy = reg.referredBy,
                    assignedAgentCode = reg.assignedAgentCode,
                    role = reg.role
                )
            }
        }
        return null
    }

    fun getUser(): User? {
        if (isLoggingOut) return null
        val current = _userStateFlow.value
        if (current != null && current.mobileNumber.isNotBlank() && !current.name.startsWith("Gamer ")) {
            return current
        }
        val internalUser = getUserInternal()
        if (internalUser != null) {
            _userStateFlow.value = internalUser
        }
        return internalUser
    }

    fun isLoggedIn(): Boolean {
        if (isLoggingOut) return false
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) return false
        return getUserInternal() != null
    }

    fun logout() {
        synchronized(this) {
            if (isLoggingOut) return
            isLoggingOut = true
            try {
                val currentUser = getUserInternal()
                if (currentUser != null) {
                    val registered = getRegisteredAccount(currentUser.mobileNumber)
                    if (registered != null) {
                        val map = getRegisteredAccountsMap()
                        map[currentUser.mobileNumber] = registered.copy(activeDeviceId = null)
                        saveRegisteredAccountsMap(map)
                    }
                }

                // SYNCHRONOUS COMMIT TO DISK
                prefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, false)
                    .remove(KEY_USER)
                    .remove(KEY_AUTH_TOKEN)
                    .remove(KEY_LAST_MOBILE)
                    .commit()

                _userStateFlow.value = null
            } catch (e: Exception) {
                Log.e("SessionManager", "Error during logout: ${e.message}")
            } finally {
                isLoggingOut = false
            }
        }
    }
}
