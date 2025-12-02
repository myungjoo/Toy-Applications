package com.webagent.app

import android.app.Application
import com.webagent.app.data.PreferenceStore
import com.webagent.app.data.WebAgentDatabase

class WebAgentApplication : Application() {
    lateinit var database: WebAgentDatabase
        private set
    
    lateinit var preferenceStore: PreferenceStore
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        database = androidx.room.Room.databaseBuilder(
            applicationContext,
            WebAgentDatabase::class.java,
            "webagent_database"
        ).build()
        
        preferenceStore = PreferenceStore(applicationContext)
    }
}
