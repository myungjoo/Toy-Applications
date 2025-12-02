package com.webagent.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.webagent.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(binding.settings.id, SettingsFragment())
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            
            val app = requireActivity().application as WebAgentApplication
            
            // 초기값 설정
            findPreference<androidx.preference.ListPreference>("llm_provider")?.value = app.preferenceStore.llmProvider
            findPreference<androidx.preference.EditTextPreference>("gemini_api_key")?.text = app.preferenceStore.geminiApiKey
            findPreference<androidx.preference.EditTextPreference>("chatgpt_api_key")?.text = app.preferenceStore.chatgptApiKey
            findPreference<androidx.preference.EditTextPreference>("chatgpt_api_url")?.text = app.preferenceStore.chatgptApiUrl
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_sms_monitoring")?.isChecked = app.preferenceStore.enableSmsMonitoring
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_notification_monitoring")?.isChecked = app.preferenceStore.enableNotificationMonitoring
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_keyboard_monitoring")?.isChecked = app.preferenceStore.enableKeyboardMonitoring
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_email_monitoring")?.isChecked = app.preferenceStore.enableEmailMonitoring
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_overlay")?.isChecked = app.preferenceStore.enableOverlay
            
            // LLM Provider 설정
            findPreference<androidx.preference.ListPreference>("llm_provider")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.llmProvider = newValue as String
                true
            }
            
            // Gemini API Key
            findPreference<androidx.preference.EditTextPreference>("gemini_api_key")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.geminiApiKey = newValue as? String
                Toast.makeText(context, "Gemini API Key가 저장되었습니다", Toast.LENGTH_SHORT).show()
                true
            }
            
            // ChatGPT API Key
            findPreference<androidx.preference.EditTextPreference>("chatgpt_api_key")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.chatgptApiKey = newValue as? String
                Toast.makeText(context, "ChatGPT API Key가 저장되었습니다", Toast.LENGTH_SHORT).show()
                true
            }
            
            // ChatGPT API URL
            findPreference<androidx.preference.EditTextPreference>("chatgpt_api_url")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.chatgptApiUrl = newValue as? String ?: "https://api.openai.com/v1/chat/completions"
                Toast.makeText(context, "ChatGPT API URL이 저장되었습니다", Toast.LENGTH_SHORT).show()
                true
            }
            
            // 모니터링 설정
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_sms_monitoring")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.enableSmsMonitoring = newValue as Boolean
                true
            }
            
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_notification_monitoring")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.enableNotificationMonitoring = newValue as Boolean
                true
            }
            
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_keyboard_monitoring")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.enableKeyboardMonitoring = newValue as Boolean
                true
            }
            
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_email_monitoring")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.enableEmailMonitoring = newValue as Boolean
                true
            }
            
            findPreference<androidx.preference.SwitchPreferenceCompat>("enable_overlay")?.setOnPreferenceChangeListener { _, newValue ->
                app.preferenceStore.enableOverlay = newValue as Boolean
                true
            }
        }
    }
}
