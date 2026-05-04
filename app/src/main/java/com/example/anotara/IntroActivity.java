package com.example.anotara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    Button startBtn;

    SharedPreferences userPrefs;
    boolean isSetupDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userPrefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        isSetupDone = userPrefs.getBoolean("isSetupDone", false);

        if (isSetupDone) {
            Intent intent = new Intent(IntroActivity.this, FireflyActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_intro);

        startBtn = findViewById(R.id.startBtn);

        startBtn.animate()
                .alpha(1f)
                .setDuration(5000)
                .withEndAction(() -> startBtn.setEnabled(true))
                .start();

        startBtn.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity.this, IntroActivity2.class));
            finish();
        });
    }
}