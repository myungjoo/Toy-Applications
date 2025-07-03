package com.smsforwarder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ForwardingRuleAdapter.OnRuleActionListener {
    
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_MONITORING_ENABLED = "monitoring_enabled";
    
    private TextView tvMonitoringStatus;
    private Button btnToggleMonitoring;
    private Button btnRequestPermissions;
    private RecyclerView recyclerViewRules;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddRule;
    
    private AppDatabase database;
    private ForwardingRuleAdapter adapter;
    private List<ForwardingRule> rules;
    private SharedPreferences prefs;
    private boolean isMonitoringEnabled;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeData();
        setupRecyclerView();
        setupClickListeners();
        checkPermissions();
        updateUI();
    }
    
    private void initializeViews() {
        tvMonitoringStatus = findViewById(R.id.tvMonitoringStatus);
        btnToggleMonitoring = findViewById(R.id.btnToggleMonitoring);
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions);
        recyclerViewRules = findViewById(R.id.recyclerViewRules);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddRule = findViewById(R.id.fabAddRule);
    }
    
    private void initializeData() {
        database = AppDatabase.getInstance(this);
        rules = new ArrayList<>();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isMonitoringEnabled = prefs.getBoolean(KEY_MONITORING_ENABLED, false);
    }
    
    private void setupRecyclerView() {
        adapter = new ForwardingRuleAdapter(rules, this);
        recyclerViewRules.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRules.setAdapter(adapter);
        loadRules();
    }
    
    private void setupClickListeners() {
        btnToggleMonitoring.setOnClickListener(v -> toggleMonitoring());
        btnRequestPermissions.setOnClickListener(v -> requestPermissions());
        fabAddRule.setOnClickListener(v -> showAddRuleDialog());
    }
    
    private void checkPermissions() {
        String[] requiredPermissions = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        };
        
        boolean allPermissionsGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        
        btnRequestPermissions.setVisibility(allPermissionsGranted ? View.GONE : View.VISIBLE);
        btnToggleMonitoring.setEnabled(allPermissionsGranted);
    }
    
    private void requestPermissions() {
        String[] requiredPermissions = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        };
        
        ActivityCompat.requestPermissions(this, requiredPermissions, SMS_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            checkPermissions();
            updateUI();
        }
    }
    
    private void toggleMonitoring() {
        isMonitoringEnabled = !isMonitoringEnabled;
        prefs.edit().putBoolean(KEY_MONITORING_ENABLED, isMonitoringEnabled).apply();
        
        Intent serviceIntent = new Intent(this, SmsMonitoringService.class);
        
        if (isMonitoringEnabled) {
            startForegroundService(serviceIntent);
            Toast.makeText(this, R.string.monitoring_enabled, Toast.LENGTH_SHORT).show();
        } else {
            stopService(serviceIntent);
            Toast.makeText(this, R.string.monitoring_disabled, Toast.LENGTH_SHORT).show();
        }
        
        updateUI();
    }
    
    private void updateUI() {
        if (isMonitoringEnabled) {
            tvMonitoringStatus.setText(R.string.monitoring_enabled);
            btnToggleMonitoring.setText(R.string.disable_monitoring);
        } else {
            tvMonitoringStatus.setText(R.string.monitoring_disabled);
            btnToggleMonitoring.setText(R.string.enable_monitoring);
        }
        
        if (rules.isEmpty()) {
            recyclerViewRules.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewRules.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void loadRules() {
        rules.clear();
        rules.addAll(database.forwardingRuleDao().getAllRules());
        adapter.updateRules(rules);
        updateUI();
    }
    
    private void showAddRuleDialog() {
        showRuleDialog(null);
    }
    
    private void showRuleDialog(ForwardingRule editRule) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_rule, null);
        
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etSenderNumber = dialogView.findViewById(R.id.etSenderNumber);
        RadioButton rbSenderPartial = dialogView.findViewById(R.id.rbSenderPartial);
        RadioButton rbSenderExact = dialogView.findViewById(R.id.rbSenderExact);
        TextInputEditText etMessageContent = dialogView.findViewById(R.id.etMessageContent);
        TextInputEditText etForwardToNumber = dialogView.findViewById(R.id.etForwardToNumber);
        
        // Set dialog title and populate fields if editing
        if (editRule != null) {
            tvDialogTitle.setText(R.string.edit_rule);
            etSenderNumber.setText(editRule.senderNumber);
            if (editRule.senderExactMatch) {
                rbSenderExact.setChecked(true);
            } else {
                rbSenderPartial.setChecked(true);
            }
            etMessageContent.setText(editRule.messageContent);
            etForwardToNumber.setText(editRule.forwardToNumber);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        btnSave.setOnClickListener(v -> {
            String senderNumber = etSenderNumber.getText().toString().trim();
            boolean senderExactMatch = rbSenderExact.isChecked();
            String messageContent = etMessageContent.getText().toString().trim();
            String forwardToNumber = etForwardToNumber.getText().toString().trim();
            
            if (forwardToNumber.isEmpty()) {
                Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (editRule != null) {
                editRule.senderNumber = senderNumber;
                editRule.senderExactMatch = senderExactMatch;
                editRule.messageContent = messageContent;
                editRule.forwardToNumber = forwardToNumber;
                database.forwardingRuleDao().updateRule(editRule);
                Toast.makeText(this, R.string.rule_updated, Toast.LENGTH_SHORT).show();
            } else {
                ForwardingRule newRule = new ForwardingRule(senderNumber, senderExactMatch, messageContent, forwardToNumber);
                database.forwardingRuleDao().insertRule(newRule);
                Toast.makeText(this, R.string.rule_added, Toast.LENGTH_SHORT).show();
            }
            
            loadRules();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    @Override
    public void onEditRule(ForwardingRule rule) {
        showRuleDialog(rule);
    }
    
    @Override
    public void onDeleteRule(ForwardingRule rule) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_rule)
                .setMessage("Are you sure you want to delete this rule?")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    database.forwardingRuleDao().deleteRule(rule);
                    loadRules();
                    Toast.makeText(this, R.string.rule_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}