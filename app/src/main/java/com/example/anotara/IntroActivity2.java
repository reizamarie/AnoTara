package com.example.anotara;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity2 extends AppCompatActivity {

    Button nextBtn2;
    CheckBox Drive, Gawa, Gala;
    ImageView globe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro2);

        // Initialize views FIRST
        globe = findViewById(R.id.globe);
        nextBtn2 = findViewById(R.id.nextBtn2);
        Drive = findViewById(R.id.Drive);
        Gawa = findViewById(R.id.Gawa);
        Gala = findViewById(R.id.Gala);

        // Disable button by default
        nextBtn2.setEnabled(false);
        nextBtn2.setAlpha(0.5f);

        // Start globe rotation (continuous spin)
        if (globe != null) {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(globe, "rotation", 0f, 360f);
            rotation.setDuration(3000); // 3 seconds per rotation
            rotation.setRepeatCount(ObjectAnimator.INFINITE);
            rotation.setRepeatMode(ObjectAnimator.RESTART);
            rotation.start();
        }

        // Listener to check all checkboxes (all must be checked)
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (Drive.isChecked() && Gawa.isChecked() && Gala.isChecked()) {
                nextBtn2.setEnabled(true);
                nextBtn2.setAlpha(1f);
            } else {
                nextBtn2.setEnabled(false);
                nextBtn2.setAlpha(0.5f);
            }
        };

        // Attach listener to all checkboxes
        Drive.setOnCheckedChangeListener(listener);
        Gawa.setOnCheckedChangeListener(listener);
        Gala.setOnCheckedChangeListener(listener);

        // Next button click
        nextBtn2.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity2.this, IntroActivity3.class));
            finish();
        });
    }
}