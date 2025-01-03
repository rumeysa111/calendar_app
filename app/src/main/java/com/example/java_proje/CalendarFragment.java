package com.example.java_proje;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment implements EventAdapter.OnEventActionListener {

    public interface TeamsCallback {
        void onTeamsFetched(List<String> teams); // Takımlar başarıyla alındığında çağrılacak metot
        void onError(String errorMessage); // Hata durumunda çağrılacak metot
    }

    private Date selectedDate;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initializeFirestore();
        setupRecyclerView(view);
        setupCalendarView(view);
        setupAddEventButton(view);
        fetchEventsFromFirestore();

        // Kullanıcının rolünü kontrol et ve butonu görünür/görünmez yap
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "user"); // Varsayılan olarak "user"
        Button addEventButton = view.findViewById(R.id.buttonAddEvent);
        if ("admin".equals(userRole)) {
            addEventButton.setVisibility(View.VISIBLE);
        } else {
            addEventButton.setVisibility(View.GONE);
        }

        return view;
    }


    private void initializeFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(eventList, this); // "this" artık OnEventActionListener'ı temsil eder
        recyclerView.setAdapter(eventAdapter);
    }

    @Override
    public void onEditEvent(Event event) {
        EventDialogFragment dialogFragment = EventDialogFragment.newInstance(
                event.getEventId(),      // Mevcut etkinlik ID'si
                event.getDate(),         // Mevcut tarih
                event.getTitle(),        // Mevcut başlık
                event.getDescription(),  // Mevcut açıklama
                Arrays.asList(event.getTeamName()) // Mevcut takım
        );
        dialogFragment.show(getParentFragmentManager(), "EditEventDialog");
    }



    @Override
    public void onDeleteEvent(Event event) {
        // Delete işlemi
        db.collection("events").document(event.getEventId())
                .delete()
                .addOnSuccessListener(aVoid -> showToast("Etkinlik başarıyla silindi!"))
                .addOnFailureListener(e -> showToast("Etkinlik silinirken hata oluştu: " + e.getMessage()));
    }
    private void updateEventInFirestore(String eventId, String title, String description, Date date, String teamName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("eventName", title);
        updatedEvent.put("eventDescription", description);
        updatedEvent.put("selectedDate", date);
        updatedEvent.put("teamName", teamName);

        db.collection("events").document(eventId)
                .update(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Etkinlik başarıyla güncellendi!", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Etkinlik güncellenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void setupCalendarView(View view) {
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = new Date(year - 1900, month, dayOfMonth);
        });
    }

    private void setupAddEventButton(View view) {
        Button addEventButton = view.findViewById(R.id.buttonAddEvent);
        addEventButton.setOnClickListener(v -> {
            if (selectedDate == null) {
                selectedDate = new Date();
            }

            fetchTeamsFromFirestore(new TeamsCallback() {
                @Override
                public void onTeamsFetched(List<String> teams) {
                    // Yeni etkinlik eklemek için null parametrelerle çağrı yapılır
                    EventDialogFragment dialogFragment = EventDialogFragment.newInstance(
                            null,         // eventId yok
                            selectedDate, // Seçilen tarih
                            "",           // Boş başlık
                            "",           // Boş açıklama
                            teams         // Mevcut takımlar
                    );
                    dialogFragment.show(getParentFragmentManager(), "EventDialogFragment");
                }

                @Override
                public void onError(String errorMessage) {
                    showToast(errorMessage);
                }
            });
        });
    }

    private void fetchTeamsFromFirestore(TeamsCallback callback) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String adminId = sharedPreferences.getString("adminId", "unknownAdmin");

        db.collection("teams")
                .whereEqualTo("adminId", adminId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> teams = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String teamName = document.getString("teamName");
                        if (teamName != null) {
                            teams.add(teamName);
                        }
                    }
                    if (!teams.isEmpty()) {
                        callback.onTeamsFetched(teams);
                    } else {
                        callback.onError("Takım bulunamadı!");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Takım bilgileri alınırken hata oluştu: " + e.getMessage()));
    }

    public void addEvent(String title, String description, String selectedTeam, Date selectedDate) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String adminId = sharedPreferences.getString("adminId", "unknownAdmin");
        String teamId = "teamId_placeholder"; // Gerçek teamId gerekli.

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", db.collection("events").document().getId());
        event.put("eventName", title);
        event.put("eventDescription", description);
        event.put("teamName", selectedTeam);
        event.put("selectedDate", selectedDate);
        event.put("adminId", adminId);
        event.put("teamId", teamId);

        db.collection("events").add(event)
                .addOnSuccessListener(documentReference -> showToast("Etkinlik başarıyla eklendi!"))
                .addOnFailureListener(e -> showToast("Etkinlik eklenirken hata oluştu: " + e.getMessage()));
    }

    private void fetchEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId(); // Firestore'dan document ID'yi alın
                            String title = document.getString("eventName");
                            String description = document.getString("eventDescription");
                            Date date = document.getDate("selectedDate");
                            String teamName = document.getString("teamName");

                            Event event = new Event(eventId, title, description, date, teamName);
                            eventList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    } else {
                        showToast("Veri alınırken hata oluştu!");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
