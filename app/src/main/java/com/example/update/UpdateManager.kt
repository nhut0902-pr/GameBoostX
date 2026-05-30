package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val version: String, val changelog: String, val downloadUrl: String) : UpdateState()
    object UpToDate : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateManager(private val context: Context) {

    private val client = OkHttpClient()
    private val prefs = context.getSharedPreferences("gameboost_update_prefs", Context.MODE_PRIVATE)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    // Persistence settings
    fun getGitHubOwner(): String = "nhut0902-pr"
    fun getGitHubRepo(): String = "GameBoostX"
    fun isAutoCheckEnabled(): Boolean = prefs.getBoolean("github_auto_check", true)

    fun saveGitHubSettings(owner: String, repo: String, autoCheck: Boolean) {
        prefs.edit()
            .putBoolean("github_auto_check", autoCheck)
            .apply()
    }

    // Helper to compare semantic versions safely (e.g. "1.1.0" or "v2.0" vs "1.0")
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.lowercase().removePrefix("v").replace(Regex("[^0-9.]"), "")
        val cleanLatest = latest.lowercase().removePrefix("v").replace(Regex("[^0-9.]"), "")

        if (cleanCurrent == cleanLatest) return false

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val currVal = currentParts.getOrElse(i) { 0 }
            val latVal = latestParts.getOrElse(i) { 0 }
            if (latVal > currVal) return true
            if (currVal > latVal) return false
        }
        return false
    }

    suspend fun checkForUpdates() {
        _updateState.value = UpdateState.Checking
        withContext(Dispatchers.IO) {
            val owner = getGitHubOwner()
            val repo = getGitHubRepo()
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"

            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _updateState.value = UpdateState.Error("Repo not found or API rate limit exceeded")
                        return@withContext
                    }

                    val jsonStr = response.body?.string() ?: ""
                    if (jsonStr.isEmpty()) {
                        _updateState.value = UpdateState.Error("Empty response from update server")
                        return@withContext
                    }

                    val json = JSONObject(jsonStr)
                    val latestVersion = json.getString("tag_name")
                    val changelog = json.optString("body", "No release details provided.")
                    
                    // Look for an APK asset
                    val assets = json.optJSONArray("assets")
                    var downloadUrl = ""
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.getString("name")
                            if (name.endsWith(".apk")) {
                                downloadUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                    }

                    if (downloadUrl.isEmpty()) {
                        _updateState.value = UpdateState.Error("No APK asset found in the latest Release")
                        return@withContext
                    }

                    // Compare with current version
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val currentVersion = packageInfo.versionName ?: "1.0"

                    if (isNewerVersion(currentVersion, latestVersion)) {
                        _updateState.value = UpdateState.UpdateAvailable(
                            version = latestVersion,
                            changelog = changelog,
                            downloadUrl = downloadUrl
                        )
                    } else {
                        _updateState.value = UpdateState.UpToDate
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "Error checking updates", e)
                _updateState.value = UpdateState.Error(e.message ?: "Network error checking updates")
            }
        }
    }

    suspend fun downloadAndInstallApk(downloadUrl: String) {
        withContext(Dispatchers.IO) {
            _updateState.value = UpdateState.Downloading(0f)
            val apkFile = File(context.cacheDir, "GameBoostX_latest.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            try {
                val request = Request.Builder().url(downloadUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _updateState.value = UpdateState.Error("Unable to download code update")
                        return@withContext
                    }

                    val body = response.body ?: throw Exception("Empty remote body")
                    val totalBytes = body.contentLength()
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(apkFile)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var loadedBytes = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        loadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = (loadedBytes.toFloat() / totalBytes.toFloat())
                            _updateState.value = UpdateState.Downloading(progress)
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    _updateState.value = UpdateState.ReadyToInstall(apkFile)
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "Error downloading update", e)
                _updateState.value = UpdateState.Error(e.message ?: "Failed to download update APK")
            }
        }
    }

    fun triggerInstall(apkFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to start install intent", e)
            _updateState.value = UpdateState.Error("Installation failed: ${e.message}")
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    fun launchPackageInstallSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun resetIdle() {
        _updateState.value = UpdateState.Idle
    }
}
