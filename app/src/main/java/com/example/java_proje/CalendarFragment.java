package com.example.java_proje;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private Date selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Takvim
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = new Date(year - 1900, month, dayOfMonth); // Seçilen tarihi kaydet
        });

        // Etkinlik Ekle Butonu
        Button addEventButton = view.findViewById(R.id.buttonAddEvent);
        addEventButton.setOnClickListener(v -> {
            if (selectedDate == null) {
                selectedDate = new Date(); // Eğer tarih seçilmediyse bugünün tarihi
            }

            List<String> teams = new ArrayList<>();
            teams.add("Yönetim Kurulu");
            teams.add("Asistan");
            teams.add("UI Ekibi");

            // EventDialogFragment ile seçilen tarih ve takımları gönder
            EventDialogFragment dialogFragment = EventDialogFragment.newInstance(selectedDate, teams);
            dialogFragment.show(getParentFragmentManager(), "EventDialogFragment");
        });

        return view;
    }

    public void addEvent(String title, String description, String selectedTeam, Date selectedDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String adminId = sharedPreferences.getString("adminId", "unknownAdmin");
        String teamId = "teamId_placeholder"; // Gerçek teamId'yi almanız gerekebilir.

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", db.collection("events").document().getId()); // Otomatik ID
        event.put("eventName", title);
        event.put("eventDescription", description);
        event.put("teamName", selectedTeam);
        event.put("selectedDate", selectedDate);
        event.put("adminId", adminId);
        event.put("teamId", teamId);

        db.collection("events").add(event)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Etkinlik başarıyla eklendi!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Etkinlik eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
