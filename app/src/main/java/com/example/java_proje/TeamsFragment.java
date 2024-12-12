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

        LayoutInflater layoutInflater=LayoutInflater.from(getContext());
View addUserview=layoutInflater.inflate(R.layout.item_team,null);
        // Fragmanı inflate et
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // RecyclerView'yi ayarla
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Takım listesi oluştur
        teamList = new ArrayList<>();
        teamAdapter = new TeamAdapter(teamList);
        recyclerView.setAdapter(teamAdapter);
        Button btnAddUser = addUserview.findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(v -> showAddUserDialog());


        // Ekip Oluştur butonuna tıklama işlemi
        Button btnCreateTeam = view.findViewById(R.id.btnCreateTeam);
        btnCreateTeam.setOnClickListener(v -> showCreateTeamDialog());
        teamAdapter.setOnAddUserClickListener(new TeamAdapter.OnAddUserClickListener() {
            @Override
            public void onAddUserClick(Team team) {
                showAddUserDialog();

            }

            // Add User dialogunu göster
        });

        // Mevcut ekipleri Firestore'dan çek
        fetchTeams();

        return view;
    }
    private void showAddUserDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_user); // Yeni bir XML dosyası oluşturun
        dialog.setCancelable(true);

        EditText etUsername = dialog.findViewById(R.id.etUsername);
        EditText etPassword = dialog.findViewById(R.id.etPassword);
        EditText etRole = dialog.findViewById(R.id.etRole);
        Button btnAdd = dialog.findViewById(R.id.btnSaveTeam);

        btnAdd.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = etRole.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                if (!getTeamList().isEmpty()) {

                    addUserToTeam(getTeamList().get(0), username, password, role);
                    dialog.dismiss();

                }else{
                    Toast.makeText(getContext(),"geçerli bir takım  bulunamadı",Toast.LENGTH_SHORT).show();
                }
                // Kullanıcıyı ekle

            }
        });

        dialog.
                show();
    }


    // Custom dialog gösterme metodu
    private void showCreateTeamDialog() {
        // Yeni bir dialog oluştur
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_team);
        dialog.setCancelable(true); // Kullanıcı dışarı tıklarsa kapatabilsin

        // Diyalog içindeki bileşenlere eriş
        EditText etTeamName = dialog.findViewById(R.id.etTeamName);
        EditText etTeamDescription = dialog.findViewById(R.id.etTeamDescription);
        Button btnSaveTeam = dialog.findViewById(R.id.btnSaveTeam);

        // Kaydet butonuna tıklama işlemi
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

    // Ekipleri Firestore'a kaydetme metodu
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
                        fetchTeams();  // Ekip oluşturulduktan sonra tekrar ekipleri çek
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Ekip oluşturulamadı.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Kullanıcı ID bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    // Mevcut ekipleri Firestore'dan çekme metodu
    private void fetchTeams() {
        db.collection("teams")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    teamList.clear();  // Önceki verileri temizle
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String teamId=document.getId();
                        String teamName = document.getString("teamName");
                        teamList.add(new Team(teamName,teamId));
                    }
                    teamAdapter.notifyDataSetChanged();  // Adapter'ı güncelle
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ekipler alınamadı.", Toast.LENGTH_SHORT).show());
    }
    // Firestore'a kullanıcı ekleme metodu
    private void addUserToTeam(Team team, String username, String password, String role) {
        db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("role", role);

        db.collection("teams").document(team.getId())
                .collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Kullanıcı eklendi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Kullanıcı eklenemedi.", Toast.LENGTH_SHORT).show();
                });
    }
    // Dummy Team Listesi
    private ArrayList<Team> getTeamList() {
        ArrayList<Team> list = new ArrayList<>();

        return list;
    }

}
