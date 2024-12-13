package com.example.java_proje;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        UserAdapter userAdapter = new UserAdapter(new ArrayList<>());

        // Kullanıcıları listelemek için recycler view ve adapteri ayarla
        holder.usersRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.usersRecyclerView.setAdapter(userAdapter);
        fetchUsersForTeam(team.getId(), userAdapter);
        SharedPreferences sharedPreferences=holder.itemView.getContext().getSharedPreferences("user_prefs",MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "user");

        boolean canAddUser=sharedPreferences.getBoolean("canAddUser",false);
        if(!"admin".equals(userRole) && !canAddUser){
            holder.btnAddUser.setVisibility(View.GONE);
        }else{
            holder.btnAddUser.setVisibility(View.VISIBLE);
        }



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
        RecyclerView usersRecyclerView;

        Button btnAddUser;
        public TeamViewHolder( View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.teamNameTextView); // item_team.xml içinde tanımlanan TextView ID'si
            usersRecyclerView = itemView.findViewById(R.id.usersRecyclerView); // item_team.xml içinde tanımlanan RecyclerView ID'si

            btnAddUser = itemView.findViewById(R.id.btnAddUser); // item_team.xml içinde tanımlanan Button ID'si
        }
    }
    private void fetchUsersForTeam(String teamId, UserAdapter userAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("teamId", teamId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("username");
                        String role = document.getString("role");
                        users.add(new User(username, role));
                    }
                    userAdapter.updateUsers(users);
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda yapılacak işlemler
                });
    }
}
