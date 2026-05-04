package com.example.anotara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class PlantActivity extends AppCompatActivity {

    LinearLayout firefly, location, plant, profile;
    ImageView seedImage;

    // 🌱 2 stages per level
    int[][] seedStagesPerLevel = {
            {R.drawable.seed1, R.drawable.seed2},   // Level 1
            {R.drawable.seed3, R.drawable.seed4},   // Level 2
            {R.drawable.seed5, R.drawable.seed6},   // Level 3
            {R.drawable.seed7, R.drawable.seed8},   // Level 4
            {R.drawable.seed9, R.drawable.seed10},  // Level 5
            {R.drawable.seed11, R.drawable.seed12}  // Level 6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        firefly = findViewById(R.id.firefly);
        location = findViewById(R.id.location);
        plant = findViewById(R.id.plant);
        profile = findViewById(R.id.profile);
        seedImage = findViewById(R.id.seedImage);

        // for master the basics
        SharedPreferences prefs = getSharedPreferences("Progress", MODE_PRIVATE);
        prefs.edit().putBoolean("plantClicked", true).apply();

        // NAVIGATION
        firefly.setOnClickListener(v ->
                startActivity(new Intent(this, FireflyActivity.class)));

        location.setOnClickListener(v ->
                startActivity(new Intent(this, LocationActivity.class)));

        profile.setOnClickListener(v ->
                startActivity(new Intent(this, Profile2Activity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSeed();
    }

    private void updateSeed() {
        SharedPreferences prefs = getSharedPreferences("Progress", MODE_PRIVATE);

        int currentLevel = prefs.getInt("currentLevel", 1);
        int progress = prefs.getInt("progress", 0);

        int levelIndex = currentLevel - 1;

        // safety check
        if (levelIndex < 0 || levelIndex >= seedStagesPerLevel.length) return;

        if (progress < 50) {
            seedImage.setImageResource(seedStagesPerLevel[levelIndex][0]);
        } else {
            seedImage.setImageResource(seedStagesPerLevel[levelIndex][1]);
        }
    }
}