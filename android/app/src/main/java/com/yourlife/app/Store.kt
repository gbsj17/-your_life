package com.yourlife.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Session(
    val date: String, // AAAA-MM-DD
    val ts: Long,
    val planId: String,
    val name: String,
    val seconds: Int
)

// Histórico salvo no próprio celular (SharedPreferences), sem servidor e sem login.
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("yourlife", Context.MODE_PRIVATE)
    private val key = "history.v1"

    fun all(): List<Session> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Session(
                    o.getString("date"),
                    o.getLong("ts"),
                    o.getString("planId"),
                    o.getString("name"),
                    o.getInt("seconds")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(session: Session) {
        val arr = JSONArray()
        (all() + session).forEach {
            arr.put(
                JSONObject()
                    .put("date", it.date)
                    .put("ts", it.ts)
                    .put("planId", it.planId)
                    .put("name", it.name)
                    .put("seconds", it.seconds)
            )
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun clear() {
        prefs.edit().remove(key).apply()
    }
}
