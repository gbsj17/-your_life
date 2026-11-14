package com.yourlife.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Dados do último release publicado no GitHub. */
class UpdateInfo(val versionCode: Int, val versionName: String, val apkUrl: String, val sizeBytes: Long)

/** Verifica, baixa e instala novas versões a partir dos Releases do GitHub (repositório público). */
object Updater {
    private const val LATEST = "https://api.github.com/repos/gbsj17/-your_life/releases/latest"

    fun installedCode(context: Context): Int = try {
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
    } catch (e: Exception) {
        0
    }

    /** Retorna o release mais recente, ou lança exceção se a consulta falhar. */
    suspend fun fetchLatest(): UpdateInfo = withContext(Dispatchers.IO) {
        val conn = URL(LATEST).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            if (conn.responseCode != 200) error("GitHub respondeu ${conn.responseCode}")
            val o = JSONObject(conn.inputStream.bufferedReader().readText())
            val tag = o.getString("tag_name")                       // v1.0.N
            val code = tag.substringAfterLast('.').toInt()
            val assets = o.getJSONArray("assets")
            var url: String? = null
            var size = 0L
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.getString("name").endsWith(".apk")) {
                    url = a.getString("browser_download_url")
                    size = a.optLong("size", 0L)
                    break
                }
            }
            UpdateInfo(code, tag.removePrefix("v"), url ?: error("Release sem APK"), size)
        } finally {
            conn.disconnect()
        }
    }

    /** Baixa o APK para o cache do app, informando o progresso (0..1). */
    suspend fun download(context: Context, info: UpdateInfo, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val out = File(dir, "YourLife-${info.versionName}.apk")
            val conn = URL(info.apkUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            try {
                if (conn.responseCode != 200) error("Download falhou (${conn.responseCode})")
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: info.sizeBytes
                var done = 0L
                conn.inputStream.use { input ->
                    out.outputStream().use { output ->
                        val buf = ByteArray(32 * 1024)
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            output.write(buf, 0, n)
                            done += n
                            if (total > 0) onProgress(done / total.toFloat())
                        }
                    }
                }
                out
            } finally {
                conn.disconnect()
            }
        }

    /** O Android exige que o usuário autorize "instalar apps desta fonte" uma vez. */
    fun canInstall(context: Context): Boolean = context.packageManager.canRequestPackageInstalls()

    fun openInstallPermission(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
