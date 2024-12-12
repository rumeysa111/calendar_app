package com.example.java_proje;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private ArrayList<Team> teamList;
    private OnAddUserClickListener onAddUserClickListener;

    public TeamAdapter(ArrayList<Team> teamList) {
        this.teamList = teamList;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout'u inflate et
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);

        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        // Mevcut takımı al
        Team team = teamList.get(position);
        // Takım adını TextView'e ayarla
        holder.teamName.setText(team.getTeamName());
        holder.btnAddUser.setOnClickListener(v -> {
            // Kullanıcı ekleme dialogunu göster
            if (onAddUserClickListener != null) {
                onAddUserClickListener.onAddUserClick(team);
            }
        });
    }

    public void setOnAddUserClickListener(OnAddUserClickListener listener) {
        this.onAddUserClickListener = listener;
    }

    public interface OnAddUserClickListener {
        void onAddUserClick(Team team);
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {

        TextView teamName;

        Button btnAddUser;
        public TeamViewHolder( View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.teamNameTextView); // item_team.xml içinde tanımlanan TextView ID'si
            btnAddUser = itemView.findViewById(R.id.btnAddUser); // item_team.xml içinde tanımlanan Button ID'si
        }
    }
}
