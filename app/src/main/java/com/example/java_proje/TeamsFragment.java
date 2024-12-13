package com.example.java_proje;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
    // Users listesi tanımlanmalı
    private ArrayList<User> users = new ArrayList<>();

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
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", getContext().MODE_PRIVATE);
        String role=sharedPreferences.getString("role","user");

//Button btnCreateTeam=getView().findViewById(R.id.btnCreateTeam);
        if("admin".equals(role)){
            btnCreateTeam.setVisibility(View.VISIBLE);

        }else{
            btnCreateTeam.setVisibility(View.GONE);
        }
        // Set listener for adding a user to a team
        teamAdapter.setOnAddUserClickListener(new TeamAdapter.OnAddUserClickListener() {
            @Override
            public void onAddUserClick(Team team) {

                fetchUsersForTeam(team.getId());
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
        Button btnAdd = dialog.findViewById(R.id.btnSave2User);  // Correct the button ID to match the one in the layout
        CheckBox cbCanCreateMeeting=dialog.findViewById(R.id.cbMeetingPermission);
        CheckBox cbCanAddUser=dialog.findViewById(R.id.cbPermissionAddUser);


        btnAdd.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = etRole.getText().toString().trim();

            // Validate input fields
            if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                if (!teamList.isEmpty()) { // If there is a valid team
                    addUserToTeam(teamList.get(0), username, password, role,cbCanCreateMeeting.isChecked(),cbCanAddUser.isChecked());
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
        CheckBox cbCanCreateMeeting=dialog.findViewById(R.id.cbMeetingPermission);
        CheckBox cbCanAddUser=dialog.findViewById(R.id.cbPermissionAddUser);


        // Button click for saving team
        btnSaveTeam.setOnClickListener(view -> {
            String teamName = etTeamName.getText().toString().trim();
            String teamDescription = etTeamDescription.getText().toString().trim();
        //    boolean canCreateMeeting = cbCanCreateMeeting.isChecked();

            if (teamName.isEmpty() || teamDescription.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                addTeams(teamName, teamDescription);
                Toast.makeText(requireContext(), "Ekip oluşturuldu: " + teamName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addTeams(String teamName, String teamDescription) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_team);
        dialog.setCancelable(true);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", getContext().MODE_PRIVATE);

        String adminId = sharedPreferences.getString("adminId", null);




        if (adminId != null) {
            Map<String, Object> team = new HashMap<>();
            team.put("teamName", teamName);
            team.put("teamDescription", teamDescription);
            team.put("adminId", adminId);

            // Firestore'da yeni bir takım oluştur
            db.collection("teams").add(team)
                    .addOnSuccessListener(documentReference -> {
                        // Ekip başarıyla oluşturuldu ve document ID'si alındı
                        String teamId = documentReference.getId(); // Team ID'si alındı

                        // Şimdi bu ID'yi teams koleksiyonuna kaydetmek veya başka bir işlem yapmak için kullanabilirsiniz
                        Map<String, Object> updatedTeam = new HashMap<>();
                        updatedTeam.put("teamId", teamId); // Yeni oluşturduğumuz team ID'sini ekliyoruz

                        // Eğer bu ID'yi Firestore'a kaydetmek isterseniz:
                        db.collection("teams").document(teamId) // ID ile belgeyi buluyoruz
                                .update(updatedTeam)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Ekip oluşturuldu: " + teamName, Toast.LENGTH_SHORT).show();
                                    fetchTeams();  // Takımları yeniden al
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Ekip ID'si kaydedilemedi.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Ekip oluşturulamadı.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "Kullanıcı ID bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch teams from Firestore
    private void fetchTeams() {
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("user_prefs",getContext().MODE_PRIVATE);
        String adminId=sharedPreferences.getString("adminId",null);
        if(adminId==null){
            Toast.makeText(getContext(),"Admin Id bulunamadı",Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("teams")
                .whereEqualTo("adminId",adminId)
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
    private void addUserToTeam(Team team, String username, String password, String role,boolean canCreateMeeting,boolean canAddUser) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", getContext().MODE_PRIVATE);
        String adminId = sharedPreferences.getString("adminId", null);

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("role", role);
        user.put("teamId", team.getId());
        user.put("adminId",adminId);
        user.put("canCreateMeeting", canCreateMeeting); // Add the permission
        user.put("canAddUser",canAddUser);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Kullanıcı eklendi: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Kullanıcı eklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUsersForTeam(String teamId){
        // Verileri alırken önce users listesini temizliyoruz
        users.clear();

        db.collection("users")
                .whereEqualTo("teamId", teamId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("username");
                        String role = document.getString("role");
                        users.add(new User(username, role));
                    }

                    // RecyclerView ve Adapter'ı güncelle
                    UserAdapter userAdapter = new UserAdapter(users);
                    RecyclerView usersRecyclerView = getView().findViewById(R.id.usersRecyclerView);
                    usersRecyclerView.setAdapter(userAdapter);

                    // Adapter'a veri değişikliklerini bildir
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Kullanıcılar alınamadı", Toast.LENGTH_SHORT).show());
    }

}
