package com.example.java_proje;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class ChatsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Gerekli varsayılan yapıcı
    }

    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);


        Button sendFeedback = view.findViewById(R.id.send_feedback);
        sendFeedback.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rumeysasemiz11@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim");
            startActivity(Intent.createChooser(emailIntent, "E-posta Uygulamasını Seçin"));
        });

        Switch themeSwitch = view.findViewById(R.id.theme_switch);
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });



        // Diğer elemanların bağlanması burada olacak
        ImageView profileAvatar = view.findViewById(R.id.profile_avatar);
        TextView username = view.findViewById(R.id.username);
        Button changePasswordButton = view.findViewById(R.id.change_password_button);
        Button logoutButton = view.findViewById(R.id.logout_button);


        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("username", "");
        String userId = sharedPreferences.getString("userId", "");

        username.setText("Kullanıcı Adı: " + name);

        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog(userId));
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(requireActivity(), LoginScreen.class);
            startActivity(intent);
            requireActivity().finish();

            Toast.makeText(requireActivity(), "Başarıyla çıkış yapıldı.", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void showChangePasswordDialog(String userId) {
        View dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_change_password, null);

        EditText oldPasswordInput = dialogView.findViewById(R.id.old_password);
        EditText newPasswordInput = dialogView.findViewById(R.id.new_password);
        EditText newPasswordInput2 = dialogView.findViewById(R.id.new_password_again);
        Button submitButton = dialogView.findViewById(R.id.submit_button);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        submitButton.setOnClickListener(v -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String newPassword2 = newPasswordInput2.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || newPassword2.isEmpty()) {
                Toast.makeText(requireActivity(), "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(newPassword2)) {
                Toast.makeText(requireActivity(), "Yeni şifreler eşleşmiyor.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String storedPassword = documentSnapshot.getString("password");
                    if (storedPassword != null && storedPassword.equals(oldPassword)) {
                        db.collection("users").document(userId)
                                .update("password", newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireActivity(), "Şifre başarıyla güncellendi.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireActivity(), "Şifre güncellenirken hata oluştu.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(requireActivity(), "Eski şifre hatalı.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Kullanıcı bilgileri bulunamadı.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireActivity(), "Hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }
}
