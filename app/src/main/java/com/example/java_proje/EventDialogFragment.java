package com.example.java_proje;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        // Dialog için layout'u inflate et
        View view = inflater.inflate(R.layout.fragment_event_dialog, container, false);

        db = FirebaseFirestore.getInstance(); // Firestore bağlantısı

        // Tarihi göster
        TextView selectedDateTextView = view.findViewById(R.id.tv_selected_date);
        Date selectedDay = (Date) getArguments().getSerializable("selected_day");
        selectedDateTextView.setText("Tarih: " + selectedDay.toString());

        // Saat seçici butonu
        Button timeButton = view.findViewById(R.id.btn_select_time);
        timeButton.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view1, hourOfDay, minute) -> timeButton.setText(String.format("Saat Seç: %02d:%02d", hourOfDay, minute)),
                    19, 0, true
            );
            timePicker.show();
        });

        /*// Takvim seçici butonu
        Button dateButton = view.findViewById(R.id.btn_select_date);
        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        // Seçilen tarihi güncelle
                        selectedDateTextView.setText("Tarih: " + selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear);
                    }, year, month, day);
            datePickerDialog.show();
        });*/

        // Spinner'ı takımlar için doldur
        Spinner teamSpinner = view.findViewById(R.id.sp_team_selection);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getArguments().getStringArrayList("teams"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        // Ekle butonu
        Button addButton = view.findViewById(R.id.btn_add);
        addButton.setOnClickListener(v -> {
            EditText titleEditText = view.findViewById(R.id.et_event_title);
            EditText descriptionEditText = view.findViewById(R.id.et_event_description);
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String selectedTeam = teamSpinner.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                if (calendarFragment != null) {
                    // Etkinliği CalendarFragment'e gönder
                    calendarFragment.addEvent(title, description, selectedTeam, selectedDay);
                }

                // Firestore'a kaydet
                saveEventToFirestore(title, description, selectedTeam, selectedDay);
                dismiss(); // Dialog'u kapat
            }
        });

        // İptal butonu
        Button cancelButton = view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v -> dismiss()); // Dialog'u kapat

        return view;
    }

    private void saveEventToFirestore(String title, String description, String selectedTeam, Date selectedDate) {
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
                    Toast.makeText(requireContext(), "Etkinlik başarıyla Firestore'a eklendi!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Etkinlik eklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // CalendarFragment'ı Activity içinde bulmak için
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            calendarFragment = (CalendarFragment) activity.getSupportFragmentManager().findFragmentByTag("CalendarFragment");
        }
    }
}
