package com.example.java_proje;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Eğer doğrudan RegisterScreen ya da LoginScreen'e gitmek istiyorsanız
        Intent intent = new Intent(MainActivity.this, RegisterScreen.class);  // veya LoginScreen.class
        startActivity(intent);

        // MainActivity'yi sonlandırıyoruz ki kullanıcı başka bir ekrana yönlendirilsin
        finish();
    }
}
