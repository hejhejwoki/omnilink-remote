package com.omnilink.remote

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")

class RemoteViewModel(application: Application) : AndroidViewModel(application) {
    private val _ipAddress = MutableStateFlow("192.168.1.100")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val IP_KEY = stringPreferencesKey("tv_ip")

    init {
        viewModelScope.launch {
            val prefs = getApplication<Application>().applicationContext.dataStore.data.first()
            prefs[IP_KEY]?.let { _ipAddress.value = it }
            try {
                AdbManager.connect(getApplication<Application>().applicationContext, _ipAddress.value)
                _connectionStatus.value = "Connected to ${_ipAddress.value}"
            } catch (e: Exception) {
                _connectionStatus.value = "Failed: ${e.message} 💀"
            }
        }
    }

    fun updateIp(newIp: String) {
        viewModelScope.launch {
            _ipAddress.value = newIp
            getApplication<Application>().applicationContext.dataStore.edit { prefs ->
                prefs[IP_KEY] = newIp
            }
            AdbManager.disconnect()
            _connectionStatus.value = "Reconnecting..."
            try {
                AdbManager.connect(getApplication<Application>().applicationContext, newIp)
                _connectionStatus.value = "Connected to $newIp"
            } catch (e: Exception) {
                _connectionStatus.value = "Failed: ${e.message} 💀"
            }
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch {
            val result = AdbManager.executeShell(getApplication<Application>().applicationContext, command)
            if (result.isFailure) {
                _connectionStatus.value = "Error: ${result.exceptionOrNull()?.message} 💔"
            }
        }
    }
}