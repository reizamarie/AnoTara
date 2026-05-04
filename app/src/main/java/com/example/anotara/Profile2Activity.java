package com.example.anotara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class Profile2Activity extends AppCompatActivity {

    LinearLayout firefly, location, plant, profile;

    TextView usernameText;
    TextView cityText;

    ImageView profileImage;
    ImageButton btnChangeImage;

    SharedPreferences progressPrefs;
    SharedPreferences userPrefs;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Intent intent = new Intent(Profile2Activity.this, CropActivity.class);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        progressPrefs = getSharedPreferences("Progress", MODE_PRIVATE);
        userPrefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        progressPrefs.edit()
                .putBoolean("profileDone", true)
                .putBoolean("profileClicked", true)
                .apply();

        usernameText = findViewById(R.id.username);
        cityText = findViewById(R.id.city);

        profileImage = findViewById(R.id.setupprofile);
        btnChangeImage = findViewById(R.id.btnChangeImage);

        firefly = findViewById(R.id.firefly);
        location = findViewById(R.id.location);
        plant = findViewById(R.id.plant);
        profile = findViewById(R.id.profile);

        profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnChangeImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        plant.setOnClickListener(v ->
                startActivity(new Intent(Profile2Activity.this, PlantActivity.class)));

        location.setOnClickListener(v ->
                startActivity(new Intent(Profile2Activity.this, LocationActivity.class)));

        firefly.setOnClickListener(v ->
                startActivity(new Intent(Profile2Activity.this, FireflyActivity.class)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadBadges();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            String croppedImagePath = data.getStringExtra("croppedImagePath");

            if (croppedImagePath != null) {
                profileImage.setImageURI(Uri.fromFile(new java.io.File(croppedImagePath)));

                userPrefs.edit()
                        .putString("profileImageUri", croppedImagePath)
                        .apply();
            }
        }
    }

    private void loadUserProfile() {
        String username = userPrefs.getString("username", "");
        String city = userPrefs.getString("city", "");
        String imagePath = userPrefs.getString("profileImageUri", null);

        usernameText.setText(username);
        cityText.setText(city);

        if (imagePath != null && !imagePath.isEmpty()) {
            profileImage.setImageURI(Uri.fromFile(new java.io.File(imagePath)));
        } else {
            profileImage.setImageResource(R.drawable.profile_icon);
        }
    }

    private void loadBadges() {
        int[] badgeIds = {
                R.id.badge1, R.id.badge2, R.id.badge3, R.id.badge4,
                R.id.badge5, R.id.badge6, R.id.badge7, R.id.badge8,
                R.id.badge9, R.id.badge10, R.id.badge11, R.id.badge12,
                R.id.badge13, R.id.badge14, R.id.badge15, R.id.badge16,
                R.id.badge17, R.id.badge18, R.id.badge19, R.id.badge20,
                R.id.badge21, R.id.badge22, R.id.badge23, R.id.badge24
        };

        int[] notDoneBadges = {
                R.drawable.badge1, R.drawable.badge2, R.drawable.badge3, R.drawable.badge4,
                R.drawable.badge5, R.drawable.badge6, R.drawable.badge7, R.drawable.badge8,
                R.drawable.badge9, R.drawable.badge10, R.drawable.badge11, R.drawable.badge12,
                R.drawable.badge13, R.drawable.badge14, R.drawable.badge15, R.drawable.badge16,
                R.drawable.badge17, R.drawable.badge18, R.drawable.badge19, R.drawable.badge20,
                R.drawable.badge21, R.drawable.badge22, R.drawable.badge23, R.drawable.badge24
        };

        int[] doneBadges = {
                R.drawable.badge_1, R.drawable.badge_2, R.drawable.badge_3, R.drawable.badge_4,
                R.drawable.badge_5, R.drawable.badge_6, R.drawable.badge_7, R.drawable.badge_8,
                R.drawable.badge_9, R.drawable.badge_10, R.drawable.badge_11, R.drawable.badge_12,
                R.drawable.badge_13, R.drawable.badge_14, R.drawable.badge_15, R.drawable.badge_16,
                R.drawable.badge_17, R.drawable.badge_18, R.drawable.badge_19, R.drawable.badge_20,
                R.drawable.badge_21, R.drawable.badge_22, R.drawable.badge_23, R.drawable.badge_24
        };

        for (int i = 0; i < badgeIds.length; i++) {
            ImageView badge = findViewById(badgeIds[i]);

            if (badge == null) {
                continue;
            }

            int levelNumber = (i / 4) + 1;
            int questNumber = (i % 4) + 1;

            boolean claimed = progressPrefs.getBoolean("L" + levelNumber + "_Q" + questNumber, false);

            badge.setImageResource(claimed ? doneBadges[i] : notDoneBadges[i]);
        }
    }
}