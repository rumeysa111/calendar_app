package com.example.java_proje;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreen extends AppCompatActivity {


    private FirebaseFirestore db;

    // EditText'leri tanımlıyoruz
    private EditText nameEditText, surnameEditText, emailEditText, passwordEditText, usernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // EditText'leri buluyoruz
        nameEditText = findViewById(R.id.isim_edit);
        surnameEditText = findViewById(R.id.soyisim_edit);
        emailEditText = findViewById(R.id.email_edit);
        passwordEditText = findViewById(R.id.password_edit);
        usernameEditText = findViewById(R.id.username_edit);

        // Kayıt ol butonunu buluyoruz
        Button registerButton = findViewById(R.id.register_button);

        Button loginButton=findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> {
            Intent intent=new Intent(RegisterScreen.this,LoginScreen.class);
            startActivity(intent);
        });

        // Kayıt ol butonuna tıklama olayını ekliyoruz
        registerButton.setOnClickListener(view -> {
            // EditText'lerden bilgileri alıyoruz
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();

            // Kullanıcı bilgilerini Firestore'a kaydediyoruz
            addUser(name, surname, email, password, username);


        });
    }

    // addUser metodunu oluşturuyoruz
    private void addUser(String name, String surname, String email, String password, String username) {
        // Kullanıcı adı benzersiz mi kontrol ediyoruz
        db.collection("users")
                .whereEqualTo("username", username)  // "username" alanında eşleşen bir değer olup olmadığını kontrol et
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Eğer username zaten varsa, hata mesajı gösteriyoruz
                        Toast.makeText(RegisterScreen.this, "Kullanıcı adı zaten alınmış, lütfen başka bir tane deneyin.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Kullanıcı adı benzersizse, kullanıcıyı Firestore'a kaydediyoruz
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("surname", surname);
                        user.put("email", email);
                        user.put("password", password);
                        user.put("username", username);
                        user.put("role", "admin");
                        user.put("isRegistered", true);

                        // Firestore'a kaydediyoruz
                        db.collection("users")
                                .add(user)  // Kullanıcı verisini ekliyoruz
                                .addOnSuccessListener(documentReference -> {
                                    // Başarılı olduğunda yapılacak işlemler
                                    Toast.makeText(RegisterScreen.this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show();

                                    // LoginScreen'e yönlendiriyoruz
                                    Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Hata durumunda yapılacak işlemler
                                    Toast.makeText(RegisterScreen.this, "Kayıt başarısız!", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda yapılacak işlemler
                    Toast.makeText(RegisterScreen.this, "Kullanıcı adı kontrolü sırasında hata oluştu.", Toast.LENGTH_SHORT).show();
                });
    }

}
