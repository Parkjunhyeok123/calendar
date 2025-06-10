package com.company.calendar.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;

import java.util.List;

public class ShortcutAdapter extends RecyclerView.Adapter<ShortcutAdapter.ViewHolder> {

    private List<Shortcut> shortcuts;
    private Context context;

    public interface OnShortcutClickListener {
        void onShortcutClick(int position);
    }

    private OnShortcutClickListener listener;

    public ShortcutAdapter(Context context, List<Shortcut> shortcuts, OnShortcutClickListener listener) {
        this.context = context;
        this.shortcuts = shortcuts;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.shortcut_icon);
            title = itemView.findViewById(R.id.shortcut_title);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onShortcutClick(pos);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shortcut, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shortcut item = shortcuts.get(position);
        holder.icon.setImageResource(item.iconResId);
        holder.title.setText(item.title);
    }

    @Override
    public int getItemCount() {
        return shortcuts.size();
    }
}
