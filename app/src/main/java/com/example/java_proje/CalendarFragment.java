package com.example.java_proje;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

            EventDialogFragment dialogFragment = EventDialogFragment.newInstance(selectedDate, teams);
            dialogFragment.show(getParentFragmentManager(), "EventDialogFragment");
        });

        return view;
    }

    public void addEvent(String title, String description, String selectedTeam, Date selectedDate) {
        // Burada etkinlik ekleme işlemini gerçekleştirin
        // Örn: veritabanına kaydet veya listeye ekle
    }
}
