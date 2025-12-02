package com.webagent.app

import android.webkit.JavascriptInterface
import com.webagent.app.data.EventData
import com.webagent.app.data.Recommendation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val activity: MainActivity) {
    private val app = activity.application as WebAgentApplication
    private val scope = CoroutineScope(Dispatchers.IO)
    
    @JavascriptInterface
    fun getEvents(callback: String) {
        scope.launch {
            try {
                val eventList = app.database.eventDao().getRecentEvents(100).first()
                val jsonArray = JSONArray()
                eventList.forEach { event ->
                    jsonArray.put(JSONObject().apply {
                        put("id", event.id)
                        put("type", event.type.name)
                        put("content", event.content)
                        put("timestamp", event.timestamp)
                        put("metadata", event.metadata)
                    })
                }
                activity.runOnUiThread {
                    activity.binding.webView.evaluateJavascript("$callback($jsonArray);", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    @JavascriptInterface
    fun getRecommendations(callback: String) {
        scope.launch {
            try {
                val recList = app.database.recommendationDao().getUnreadRecommendations().first()
                val jsonArray = JSONArray()
                recList.forEach { rec ->
                    jsonArray.put(JSONObject().apply {
                        put("id", rec.id)
                        put("type", rec.type.name)
                        put("title", rec.title)
                        put("description", rec.description)
                        put("action", rec.action)
                        put("priority", rec.priority)
                        put("timestamp", rec.timestamp)
                    })
                }
                activity.runOnUiThread {
                    activity.binding.webView.evaluateJavascript("$callback($jsonArray);", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    @JavascriptInterface
    fun markRecommendationAsRead(id: Long) {
        scope.launch(Dispatchers.IO) {
            app.database.recommendationDao().markAsRead(id)
        }
    }
    
    @JavascriptInterface
    fun openSettings() {
        activity.runOnUiThread {
            android.content.Intent(activity, SettingsActivity::class.java).also {
                activity.startActivity(it)
            }
        }
    }
}
