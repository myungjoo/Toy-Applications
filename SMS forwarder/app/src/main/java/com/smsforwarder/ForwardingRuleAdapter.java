package com.smsforwarder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ForwardingRuleAdapter extends RecyclerView.Adapter<ForwardingRuleAdapter.RuleViewHolder> {
    
    private List<ForwardingRule> rules;
    private OnRuleActionListener listener;
    
    public interface OnRuleActionListener {
        void onEditRule(ForwardingRule rule);
        void onDeleteRule(ForwardingRule rule);
    }
    
    public ForwardingRuleAdapter(List<ForwardingRule> rules, OnRuleActionListener listener) {
        this.rules = rules;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forwarding_rule, parent, false);
        return new RuleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        ForwardingRule rule = rules.get(position);
        holder.bind(rule);
    }
    
    @Override
    public int getItemCount() {
        return rules.size();
    }
    
    public void updateRules(List<ForwardingRule> newRules) {
        this.rules = newRules;
        notifyDataSetChanged();
    }
    
    class RuleViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvSenderNumber;
        private TextView tvMessageContent;
        private TextView tvForwardToNumber;
        private Button btnEdit;
        private Button btnDelete;
        
        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderNumber = itemView.findViewById(R.id.tvSenderNumber);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvForwardToNumber = itemView.findViewById(R.id.tvForwardToNumber);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(ForwardingRule rule) {
            // Display sender number with match type
            String senderDisplay = rule.senderNumber != null && !rule.senderNumber.trim().isEmpty() 
                    ? rule.senderNumber + (rule.senderExactMatch ? " (Exact)" : " (Partial)")
                    : "Any sender";
            tvSenderNumber.setText(senderDisplay);
            
            // Display message content
            String contentDisplay = rule.messageContent != null && !rule.messageContent.trim().isEmpty()
                    ? rule.messageContent
                    : "Any content";
            tvMessageContent.setText(contentDisplay);
            
            // Display forward to number
            tvForwardToNumber.setText(rule.forwardToNumber);
            
            // Set click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditRule(rule);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteRule(rule);
                }
            });
        }
    }
}