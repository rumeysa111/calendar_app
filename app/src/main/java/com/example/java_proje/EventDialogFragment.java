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
            // İlk önce geçerli saat ve dakikayı al
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            // TimePickerDialog oluştur
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view1, hourOfDay, minute) -> {
                        // Seçilen saat ve dakikayı takvime ayarla
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        // Seçilen saati butona yaz
                        String formattedTime = String.format("Saat Seç: %02d:%02d", hourOfDay, minute);
                        timeButton.setText(formattedTime);

                        // Seçilen saat ve dakikayı bir `Date` olarak kaydet
                        getArguments().putSerializable("selected_time", calendar.getTime());
                    },
                    currentHour, currentMinute, true // 24 saat formatı
            );
            timePicker.show();
        });


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
                // Tarih ve saat birleşimi
                Calendar finalCalendar = Calendar.getInstance();
                Date selectedDate = (Date) getArguments().getSerializable("selected_day"); // Seçilen tarih
                Date selectedTime = (Date) getArguments().getSerializable("selected_time"); // Seçilen saat

                if (selectedDate != null) {
                    // Tarih bilgilerini ayarla
                    finalCalendar.setTime(selectedDate);

                    // Saat bilgilerini güncelle
                    if (selectedTime != null) {
                        Calendar timeCalendar = Calendar.getInstance();
                        timeCalendar.setTime(selectedTime);

                        finalCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                        finalCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                    }
                }

                // Son tam tarih ve saati al
                Date finalDateTime = finalCalendar.getTime();

                // Etkinliği kaydet
                saveEventToFirestore(title, description, selectedTeam, finalDateTime);
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
