package com.example.java_proje;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDialogFragment extends DialogFragment {

    private static final String TAG = "EventDialogFragment";
    private CalendarFragment calendarFragment; // CalendarFragment referansı
    private FirebaseFirestore db;

    public static EventDialogFragment newInstance(Date selectedDay, List<String> teams) {
        EventDialogFragment fragment = new EventDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("selected_day", selectedDay); // Date objesi serializable olmalı
        args.putStringArrayList("teams", new ArrayList<>(teams)); // List<String> olarak ekleniyor
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_event_dialog, container, false);

        db = FirebaseFirestore.getInstance(); // Firestore bağlantısı

        TextView selectedDateTextView = view.findViewById(R.id.tv_selected_date);
        Date selectedDay = (Date) getArguments().getSerializable("selected_day");
        Log.d(TAG, "Selected day: " + selectedDay);

        selectedDateTextView.setText("Tarih: " + (selectedDay != null ? selectedDay.toString() : "null"));

        Button timeButton = view.findViewById(R.id.btn_select_time);
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

                        String formattedTime = String.format("Saat Seç: %02d:%02d", hourOfDay, minute);
                        timeButton.setText(formattedTime);
                        getArguments().putSerializable("selected_time", calendar.getTime());
                        Log.d(TAG, "Selected time: " + calendar.getTime());
                    },
                    currentHour, currentMinute, true
            );
            timePicker.show();
        });

        Spinner teamSpinner = view.findViewById(R.id.sp_team_selection);
        List<String> teams = getArguments().getStringArrayList("teams");
        Log.d(TAG, "Teams: " + teams);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, teams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        Button addButton = view.findViewById(R.id.btn_add);
        addButton.setOnClickListener(v -> {
            EditText titleEditText = view.findViewById(R.id.et_event_title);
            EditText descriptionEditText = view.findViewById(R.id.et_event_description);
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String selectedTeam = (String) teamSpinner.getSelectedItem();

            Log.d(TAG, "Title: " + title + ", Description: " + description + ", Selected team: " + selectedTeam);

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar finalCalendar = Calendar.getInstance();
            Date selectedDate = (Date) getArguments().getSerializable("selected_day");
            Date selectedTime = (Date) getArguments().getSerializable("selected_time");

            if (selectedDate != null) {
                finalCalendar.setTime(selectedDate);
                if (selectedTime != null) {
                    Calendar timeCalendar = Calendar.getInstance();
                    timeCalendar.setTime(selectedTime);

                    finalCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                    finalCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                }
            }

            Date finalDateTime = finalCalendar.getTime();
            Log.d(TAG, "Final DateTime: " + finalDateTime);

            saveEventToFirestore(title, description, selectedTeam, finalDateTime);
        });

        Button cancelButton = view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveEventToFirestore(String title, String description, String selectedTeam, Date selectedDate) {
        Log.d(TAG, "Saving event to Firestore: Title: " + title + ", Description: " + description + ", Team: " + selectedTeam + ", Date: " + selectedDate);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String adminId = sharedPreferences.getString("adminId", "");

        Log.d(TAG, "Admin ID: " + adminId);

        if (adminId == null || adminId.isEmpty()) {
            Toast.makeText(getContext(), "Admin kimliği bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("teams")
                .whereEqualTo("teamName", selectedTeam)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Team query success: " + queryDocumentSnapshots);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        String teamId = queryDocumentSnapshots.getDocuments().get(0).getString("teamId");
                        Log.d(TAG, "Team ID: " + teamId);

                        if (teamId == null || teamId.isEmpty()) {
                            Toast.makeText(getContext(), "Takım kimliği bulunamadı!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> event = new HashMap<>();
                        event.put("eventId", db.collection("events").document().getId());
                        event.put("eventName", title);
                        event.put("eventDescription", description);
                        event.put("teamName", selectedTeam);
                        event.put("teamId", teamId);
                        event.put("selectedDate", selectedDate);
                        event.put("adminId", adminId);

                        db.collection("events").add(event)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Event added successfully: " + documentReference.getId());
                                    Toast.makeText(getContext(), "Etkinlik başarıyla Firestore'a eklendi!", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding event: " + e.getMessage(), e);
                                    Toast.makeText(getContext(), "Etkinlik eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.d(TAG, "Team not found in query.");
                        Toast.makeText(getContext(), "Seçilen takım bulunamadı!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying team: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Takım bilgisi alınırken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach called");

        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            calendarFragment = (CalendarFragment) activity.getSupportFragmentManager().findFragmentByTag("CalendarFragment");
            Log.d(TAG, "CalendarFragment attached: " + calendarFragment);
        }
    }
}
