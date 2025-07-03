package com.smsforwarder;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ForwardingRuleDao {
    
    @Query("SELECT * FROM forwarding_rules WHERE isEnabled = 1")
    List<ForwardingRule> getAllEnabledRules();
    
    @Query("SELECT * FROM forwarding_rules")
    List<ForwardingRule> getAllRules();
    
    @Query("SELECT * FROM forwarding_rules WHERE id = :id")
    ForwardingRule getRuleById(int id);
    
    @Insert
    long insertRule(ForwardingRule rule);
    
    @Update
    void updateRule(ForwardingRule rule);
    
    @Delete
    void deleteRule(ForwardingRule rule);
    
    @Query("DELETE FROM forwarding_rules WHERE id = :id")
    void deleteRuleById(int id);
    
    @Query("SELECT COUNT(*) FROM forwarding_rules")
    int getRuleCount();
}