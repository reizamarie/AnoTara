package com.example.anotara;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity4 extends AppCompatActivity {

    Button nextBtn4;
    ImageView globe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro4);

        // Initialize views FIRST
        globe = findViewById(R.id.globe);
        nextBtn4 = findViewById(R.id.nextBtn4);

        // Start globe rotation (continuous spin)
        if (globe != null) {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(globe, "rotation", 0f, 360f);
            rotation.setDuration(3000); // 3 seconds per rotation
            rotation.setRepeatCount(ObjectAnimator.INFINITE);
            rotation.setRepeatMode(ObjectAnimator.RESTART);
            rotation.start();
        }

        // Next button click
        nextBtn4.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity4.this, MainActivity.class));
            finish();
        });
    }
}