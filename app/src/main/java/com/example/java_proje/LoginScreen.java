package com.example.java_proje;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginScreen extends AppCompatActivity {

    private FirebaseFirestore db;

    // EditText'leri tanımlıyoruz
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // EditText'leri buluyoruz
        usernameEditText = findViewById(R.id.username_edit);
        passwordEditText = findViewById(R.id.password_edit);

        // Giriş yap butonunu buluyoruz
        Button loginButton = findViewById(R.id.login_button);

        // Giriş yap butonuna tıklama olayını ekliyoruz
        loginButton.setOnClickListener(view -> {
            // EditText'lerden bilgileri alıyoruz
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Kullanıcı bilgilerini Firestore'dan doğruluyoruz
            loginUser(username, password);

        });
    }

    // loginUser metodunu oluşturuyoruz
    private void loginUser(String username, String password) {
        // Firestore'dan kullanıcının bilgilerini sorguluyoruz
        db.collection("admins")
                .whereEqualTo("username", username)  // Kullanıcı adı ile eşleşen admini bul
                .whereEqualTo("password", password)  // Şifre ile eşleşen admini bul
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Admin kullanıcı bulundu
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String adminId = document.getId();

                        // Admin id'sini SharedPreferences'e kaydet
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("adminId", adminId);
                        editor.apply();

                        // Başarı mesajı
                        Toast.makeText(this, "Giriş başarılı (Admin)!", Toast.LENGTH_SHORT).show();

                        // Admin giriş yaptıysa HomePage'e yönlendirme
                        Intent intent = new Intent(LoginScreen.this, HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Admin değilse, users koleksiyonunda arama yapıyoruz
                        db.collection("users")
                                .whereEqualTo("username", username)  // Kullanıcı adı ile eşleşen user'ı bul
                                .whereEqualTo("password", password)  // Şifre ile eşleşen user'ı bul
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.isEmpty()) {
                                        // User bulundu
                                        DocumentSnapshot document = queryDocumentSnapshots1.getDocuments().get(0);
                                        String userId = document.getId();

                                        // User id'sini SharedPreferences'e kaydet
                                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("userId", userId);
                                        editor.apply();

                                        // Başarı mesajı
                                        Toast.makeText(this, "Giriş başarılı (User)!", Toast.LENGTH_SHORT).show();

                                        // User giriş yaptıysa HomePage'e yönlendirme
                                        Intent intent = new Intent(LoginScreen.this, HomePage.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Kullanıcı adı ve şifre eşleşmiyor
                                        Toast.makeText(this, "Geçersiz kullanıcı adı veya şifre!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Hata durumunda yapılacak işlemler
                                    Toast.makeText(this, "Hata oluştu, tekrar deneyin.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda yapılacak işlemler
                    Toast.makeText(this, "Hata oluştu, tekrar deneyin.", Toast.LENGTH_SHORT).show();
                });
    }
}
