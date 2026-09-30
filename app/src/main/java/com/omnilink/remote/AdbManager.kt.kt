package com.omnilink.remote

import android.content.Context
import dadb.AdbKeyPair
import dadb.AdbShellResponse
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AdbManager {
    private var dadb: Dadb? = null
    private var currentIp: String? = null

    suspend fun connect(context: Context, ip: String) = withContext(Dispatchers.IO) {
        if (currentIp == ip && dadb != null) return@withContext
        dadb?.close()
        try {
            // Dadb requires an RSA key pair for Android 11+ Wireless Debugging.
            // We generate and store it in the app's internal sandboxed directory.
            val privateKeyFile = File(context.filesDir, "adbkey")
            val publicKeyFile = File(context.filesDir, "adbkey.pub")
            
            if (!privateKeyFile.exists()) {
                AdbKeyPair.generate(privateKeyFile, publicKeyFile)
            }
            
            val keyPair = AdbKeyPair.read(privateKeyFile, publicKeyFile)
            dadb = Dadb.create(ip, 5555, keyPair)
            currentIp = ip
        } catch (e: Exception) {
            dadb = null
            currentIp = null
            throw e
        }
    }

    suspend fun executeShell(context: Context, command: String): Result<AdbShellResponse> = withContext(Dispatchers.IO) {
        val ip = currentIp ?: return@withContext Result.failure(Exception("No IP configured 😭"))
        
        try {
            if (dadb == null) connect(context, ip)
            val response = dadb?.shell(command) ?: throw Exception("Dadb instance is null 💔")
            Result.success(response)
        } catch (e: Exception) {
            // If the socket dropped, attempt a single transparent reconnection cycle
            dadb?.close()
            dadb = null
            try {
                connect(context, ip)
                val response = dadb?.shell(command) ?: throw Exception("Reconnect failed 🥀")
                Result.success(response)
            } catch (reEx: Exception) {
                Result.failure(reEx)
            }
        }
    }
    
    fun disconnect() {
        dadb?.close()
        dadb = null
        currentIp = null
    }
}