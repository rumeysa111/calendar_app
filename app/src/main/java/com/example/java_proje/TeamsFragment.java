package com.example.java_proje;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TeamsFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TeamAdapter teamAdapter;
    private ArrayList<Team> teamList;

    public TeamsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the team list
        teamList = new ArrayList<>();
        teamAdapter = new TeamAdapter(teamList);
        recyclerView.setAdapter(teamAdapter);

        // Fetch teams
        fetchTeams();

        // Button to create a new team
        Button btnCreateTeam = view.findViewById(R.id.btnCreateTeam);
        btnCreateTeam.setOnClickListener(v -> showCreateTeamDialog());

        // Set listener for adding a user to a team
        teamAdapter.setOnAddUserClickListener(new TeamAdapter.OnAddUserClickListener() {
            @Override
            public void onAddUserClick(Team team) {
                showAddUserDialog();
            }
        });

        return view;
    }

    // Show dialog for adding a user
    private void showAddUserDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_user);
        dialog.setCancelable(true);

        // Get references to the EditText and Button inside the dialog
        EditText etUsername = dialog.findViewById(R.id.etUsername);
        EditText etPassword = dialog.findViewById(R.id.etPassword);
        EditText etRole = dialog.findViewById(R.id.etRole);
        Button btnAdd = dialog.findViewById(R.id.btnSaveUser);  // Correct the button ID to match the one in the layout

        btnAdd.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = etRole.getText().toString().trim();

            // Validate input fields
            if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                if (!teamList.isEmpty()) { // If there is a valid team
                    addUserToTeam(teamList.get(0), username, password, role);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Geçerli bir takım bulunamadı", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    // Show dialog for creating a new team
    private void showCreateTeamDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_team);
        dialog.setCancelable(true);

        // Get references to the EditText and Button inside the dialog
        EditText etTeamName = dialog.findViewById(R.id.etTeamName);
        EditText etTeamDescription = dialog.findViewById(R.id.etTeamDescription);
        Button btnSaveTeam = dialog.findViewById(R.id.btnSaveTeam);

        // Button click for saving team
        btnSaveTeam.setOnClickListener(view -> {
            String teamName = etTeamName.getText().toString().trim();
            String teamDescription = etTeamDescription.getText().toString().trim();

            if (teamName.isEmpty() || teamDescription.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                addTeams(teamName, teamDescription);
                Toast.makeText(getContext(), "Ekip oluşturuldu: " + teamName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Add team to Firestore
    private void addTeams(String teamName, String teamDescription) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", getContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            Map<String, Object> team = new HashMap<>();
            team.put("teamName", teamName);
            team.put("teamDescription", teamDescription);
            team.put("userId", userId);

            db.collection("teams").add(team)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Ekip oluşturuldu: " + teamName, Toast.LENGTH_SHORT).show();
                        fetchTeams();  // Fetch teams again after creating the team
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Ekip oluşturulamadı.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Kullanıcı ID bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch teams from Firestore
    private void fetchTeams() {
        db.collection("teams")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    teamList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String teamId = document.getId();
                        String teamName = document.getString("teamName");
                        teamList.add(new Team(teamName, teamId));
                    }
                    teamAdapter.notifyDataSetChanged();  // Notify adapter of new data
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ekipler alınamadı.", Toast.LENGTH_SHORT).show());
    }

    // Add user to team in Firestore
    private void addUserToTeam(Team team, String username, String password, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("role", role);
        user.put("teamId", team.getId());

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Kullanıcı eklendi: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Kullanıcı eklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
