package com.example.anotara;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class IntroActivity3 extends AppCompatActivity {

    CheckBox Tara;
    Button notifyBtn, notifyBtn2, nextBtn3;

    private boolean isCheckboxChecked = false;
    private boolean isNotificationEnabled = false;
    private boolean isLocationEnabled = false;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
                    isNotificationEnabled = true;
                } else {
                    Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show();
                    isNotificationEnabled = false;
                }
                updateNextButtonState();
            });

    // 📍 Location Permission Launcher
    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkIfLocationIsOn();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    isLocationEnabled = false;
                    updateNextButtonState();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro3);

        nextBtn3 = findViewById(R.id.nextBtn3);
        Tara = findViewById(R.id.Tara);

        notifyBtn = findViewById(R.id.btnLocation);
        notifyBtn2 = findViewById(R.id.btnNotif);

        nextBtn3.setEnabled(false);
        nextBtn3.setAlpha(0.5f);

        // ☑ Checkbox listener
        Tara.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCheckboxChecked = isChecked;
            updateNextButtonState();
        });

        // ➡ Next button
        nextBtn3.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity3.this, IntroActivity4.class));
            finish();
        });

        notifyBtn2.setOnClickListener(v -> enableNotifications());

        notifyBtn.setOnClickListener(v -> enableLocation());
    }

    private void enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Notifications already enabled", Toast.LENGTH_SHORT).show();
                isNotificationEnabled = true;

            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }

        } else {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            boolean enabled = manager != null && manager.areNotificationsEnabled();

            if (enabled) {
                Toast.makeText(this, "Notifications already enabled", Toast.LENGTH_SHORT).show();
                isNotificationEnabled = true;
            } else {
                Toast.makeText(this, "Enable notifications in settings", Toast.LENGTH_LONG).show();
                isNotificationEnabled = false;
            }
        }

        updateNextButtonState();
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            checkIfLocationIsOn();

        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkIfLocationIsOn() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isOn = locationManager != null &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isOn) {
            Toast.makeText(this, "Location enabled!", Toast.LENGTH_SHORT).show();
            isLocationEnabled = true;
        } else {
            Toast.makeText(this, "Turn on location (GPS)", Toast.LENGTH_LONG).show();
            isLocationEnabled = false;

            // Open location settings
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        updateNextButtonState();
    }

    private void updateNextButtonState() {
        if (isCheckboxChecked && isNotificationEnabled && isLocationEnabled) {
            nextBtn3.setEnabled(true);
            nextBtn3.setAlpha(1f);
        } else {
            nextBtn3.setEnabled(false);
            nextBtn3.setAlpha(0.5f);
        }
    }
}