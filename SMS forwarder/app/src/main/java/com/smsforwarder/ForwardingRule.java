package com.smsforwarder;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "forwarding_rules")
public class ForwardingRule {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String senderNumber;
    public boolean senderExactMatch; // true for exact match, false for partial match
    public String messageContent;
    public String forwardToNumber;
    public boolean isEnabled;
    
    public ForwardingRule() {
        this.isEnabled = true;
    }
    
    @androidx.room.Ignore
    public ForwardingRule(String senderNumber, boolean senderExactMatch, 
                         String messageContent, String forwardToNumber) {
        this.senderNumber = senderNumber;
        this.senderExactMatch = senderExactMatch;
        this.messageContent = messageContent;
        this.forwardToNumber = forwardToNumber;
        this.isEnabled = true;
    }
    
    public boolean matchesSms(String fromNumber, String messageBody) {
        // Check sender number match
        if (senderNumber != null && !senderNumber.trim().isEmpty()) {
            if (senderExactMatch) {
                if (!senderNumber.equals(fromNumber)) {
                    return false;
                }
            } else {
                if (!fromNumber.contains(senderNumber)) {
                    return false;
                }
            }
        }
        
        // Check message content match (always partial match for content)
        if (messageContent != null && !messageContent.trim().isEmpty()) {
            if (!messageBody.toLowerCase().contains(messageContent.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "ForwardingRule{" +
                "id=" + id +
                ", senderNumber='" + senderNumber + '\'' +
                ", senderExactMatch=" + senderExactMatch +
                ", messageContent='" + messageContent + '\'' +
                ", forwardToNumber='" + forwardToNumber + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}