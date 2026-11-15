package com.yourlife.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Histórico e ajustes salvos no próprio aparelho (SharedPreferences). */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("yourlife", Context.MODE_PRIVATE)
    private val historyKey = "history.v1"
    private val settingsKey = "settings.v1"
    private val enrollKey = "enrollment.v1"
    private val swapsKey = "swaps.v1"

    fun history(): List<Session> {
        val raw = prefs.getString(historyKey, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val ts = o.getLong("ts")
                val seconds = o.getInt("seconds")
                Session(
                    id = o.optLong("id", ts),
                    date = o.getString("date"),
                    startTs = o.optLong("startTs", ts - seconds * 1000L),
                    ts = ts,
                    planId = o.getString("planId"),
                    name = o.getString("name"),
                    seconds = seconds,
                    synced = o.optBoolean("synced", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun save(list: List<Session>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("date", it.date)
                    .put("startTs", it.startTs)
                    .put("ts", it.ts)
                    .put("planId", it.planId)
                    .put("name", it.name)
                    .put("seconds", it.seconds)
                    .put("synced", it.synced)
            )
        }
        prefs.edit().putString(historyKey, arr.toString()).apply()
    }

    fun add(session: Session) = save(history() + session)

    fun markSynced(ids: Set<Long>) =
        save(history().map { if (it.id in ids) it.copy(synced = true) else it })

    fun clearHistory() {
        prefs.edit().remove(historyKey).apply()
    }

    fun settings(): Settings {
        val raw = prefs.getString(settingsKey, null) ?: return Settings()
        return try {
            val o = JSONObject(raw)
            Settings(theme = o.optInt("theme", 0), autoSync = o.optBoolean("autoSync", true))
        } catch (e: Exception) {
            Settings()
        }
    }

    fun saveSettings(s: Settings) {
        prefs.edit()
            .putString(settingsKey, JSONObject().put("theme", s.theme).put("autoSync", s.autoSync).toString())
            .apply()
    }

    fun enrollment(): Enrollment? {
        val raw = prefs.getString(enrollKey, null) ?: return null
        return try {
            val o = JSONObject(raw)
            val arr = o.getJSONArray("phaseDays")
            Enrollment(
                programId = o.getString("programId"),
                startDay = o.getLong("startDay"),
                phaseDays = (0 until arr.length()).map { arr.getInt(it) },
                levelOffset = o.optInt("levelOffset", 0)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveEnrollment(e: Enrollment?) {
        if (e == null) {
            prefs.edit().remove(enrollKey).apply()
            return
        }
        prefs.edit().putString(
            enrollKey,
            JSONObject()
                .put("programId", e.programId)
                .put("startDay", e.startDay)
                .put("phaseDays", JSONArray(e.phaseDays))
                .put("levelOffset", e.levelOffset)
                .toString()
        ).apply()
    }

    fun swaps(): Map<String, String> {
        val raw = prefs.getString(swapsKey, null) ?: return emptyMap()
        return try {
            val o = JSONObject(raw)
            o.keys().asSequence().associateWith { o.getString(it) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveSwaps(m: Map<String, String>) {
        val o = JSONObject()
        m.forEach { (k, v) -> o.put(k, v) }
        prefs.edit().putString(swapsKey, o.toString()).apply()
    }
}
