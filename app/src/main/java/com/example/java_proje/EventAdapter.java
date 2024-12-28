package com.example.java_proje;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventActionListener listener;

    public EventAdapter(List<Event> eventList, OnEventActionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    public interface OnEventActionListener {
        void onEditEvent(Event event);
        void onDeleteEvent(Event event);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDescription.setText(event.getDescription());
        holder.tvEventDate.setText(event.getDate().toString());
        holder.tvEventTeam.setText(event.getTeamName());

        // Admin kontrolü
        SharedPreferences sharedPreferences = holder.itemView.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "user");

        if ("admin".equals(userRole)) {
            holder.ivEditEvent.setVisibility(View.VISIBLE);
            holder.ivDeleteEvent.setVisibility(View.VISIBLE);
        } else {
            holder.ivEditEvent.setVisibility(View.GONE);
            holder.ivDeleteEvent.setVisibility(View.GONE);
        }

        // Düzenle ve silme işlemleri için listener'ları ayarla
        holder.ivEditEvent.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditEvent(event);
            }
        });

        holder.ivDeleteEvent.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteEvent(event);
            }
        });
    }



    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvEventDescription, tvEventDate, tvEventTeam;
        ImageView ivEditEvent, ivDeleteEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventTeam = itemView.findViewById(R.id.tvEventTeam);
            ivEditEvent = itemView.findViewById(R.id.ivEditEvent);
            ivDeleteEvent = itemView.findViewById(R.id.ivDeleteEvent);
        }
    }
}
