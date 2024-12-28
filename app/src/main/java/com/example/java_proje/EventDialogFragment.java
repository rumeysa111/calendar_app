package com.example.java_proje;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDialogFragment extends DialogFragment {

    private static final String TAG = "EventDialogFragment";
    private FirebaseFirestore db;

    public static EventDialogFragment newInstance(String eventId, Date selectedDate, String title, String description, List<String> teams) {
        EventDialogFragment fragment = new EventDialogFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putSerializable("selected_date", selectedDate);
        args.putString("title", title);
        args.putString("description", description);
        args.putStringArrayList("teams", new ArrayList<>(teams));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_event_dialog, container, false);

        db = FirebaseFirestore.getInstance();

        // Parametreleri al
        String eventId = getArguments().getString("event_id");
        Date selectedDate = (Date) getArguments().getSerializable("selected_date");
        String title = getArguments().getString("title");
        String description = getArguments().getString("description");
        List<String> teams = getArguments().getStringArrayList("teams");

        // UI bileşenlerini bağla
        EditText titleEditText = view.findViewById(R.id.et_event_title);
        EditText descriptionEditText = view.findViewById(R.id.et_event_description);
        Spinner teamSpinner = view.findViewById(R.id.sp_team_selection);
        Button saveButton = view.findViewById(R.id.btn_add);
        Button cancelButton = view.findViewById(R.id.btn_cancel);
        Button timeButton = view.findViewById(R.id.btn_select_time);

        // Mevcut bilgileri doldurun (düzenleme modu)
        if (eventId != null) {
            titleEditText.setText(title);
            descriptionEditText.setText(description);
            saveButton.setText("Güncelle");
        } else {
            saveButton.setText("Ekle");
        }

        // Takım spinner'ını ayarla
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, teams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        // Saat seç butonu için onClickListener
        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(
                    getContext(),
                    (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
                        timeButton.setText("Saat: " + formattedTime);
                        getArguments().putSerializable("selected_time", calendar.getTime());
                        Log.d(TAG, "Selected time: " + calendar.getTime());
                    },
                    currentHour, currentMinute, true
            );
            timePicker.show();
        });

        saveButton.setOnClickListener(v -> {
            // Kullanıcı girdilerini al
            String newTitle = titleEditText.getText().toString();
            String newDescription = descriptionEditText.getText().toString();
            String selectedTeam = (String) teamSpinner.getSelectedItem();

            if (newTitle.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(getContext(), "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Yeni tarih ve saat hesaplama
            Calendar finalCalendar = Calendar.getInstance();
            finalCalendar.setTime(selectedDate);

            Date selectedTime = (Date) getArguments().getSerializable("selected_time");
            if (selectedTime != null) {
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(selectedTime);
                finalCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                finalCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            }

            Date finalDateTime = finalCalendar.getTime();

            // Güncelleme mi, ekleme mi yapılacağını kontrol et
            if (eventId != null) {
                updateEventInFirestore(eventId, newTitle, newDescription, finalDateTime, selectedTeam);
            } else {
                saveNewEventToFirestore(newTitle, newDescription, finalDateTime, selectedTeam);
            }

            // Sayfayı yenile
            refreshPage();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void updateEventInFirestore(String eventId, String title, String description, Date date, String teamName) {
        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("eventName", title);
        updatedEvent.put("eventDescription", description);
        updatedEvent.put("selectedDate", date);
        updatedEvent.put("teamName", teamName);

        db.collection("events").document(eventId)
                .update(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Etkinlik başarıyla güncellendi!", Toast.LENGTH_SHORT).show();
                    refreshPage();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Etkinlik güncellenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveNewEventToFirestore(String title, String description, Date date, String teamName) {
        String adminId = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("adminId", "unknownAdmin");

        Map<String, Object> newEvent = new HashMap<>();
        newEvent.put("eventId", db.collection("events").document().getId());
        newEvent.put("eventName", title);
        newEvent.put("eventDescription", description);
        newEvent.put("selectedDate", date);
        newEvent.put("teamName", teamName);
        newEvent.put("adminId", adminId);

        db.collection("events").add(newEvent)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Etkinlik başarıyla eklendi!", Toast.LENGTH_SHORT).show();
                    refreshPage();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Etkinlik eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void refreshPage() {
        if (getActivity() instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) getActivity();
            activity.getSupportFragmentManager().beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
