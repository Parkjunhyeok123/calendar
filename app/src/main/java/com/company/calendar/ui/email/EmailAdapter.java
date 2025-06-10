package com.company.calendar.ui.email;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.google.api.services.gmail.model.Message;
import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {
    private final List<EmailItem> emails;
    private final OnEmailClickListener listener;

    public interface OnEmailClickListener {
        void onEmailClick(EmailItem item);
    }

    public EmailAdapter(List<EmailItem> emails, OnEmailClickListener listener) {
        this.emails = emails;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        EmailItem email = emails.get(position);
        holder.subject.setText(email.subject);
        holder.from.setText(email.from);
        holder.date.setText(email.date);

        holder.itemView.setOnClickListener(v -> listener.onEmailClick(email));
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    static class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView subject, from, date;

        EmailViewHolder(View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.email_subject);
            from = itemView.findViewById(R.id.email_from);
            date = itemView.findViewById(R.id.email_date);
        }
    }
}
