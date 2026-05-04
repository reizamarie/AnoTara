package com.example.anotara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    ImageView settupprofile;
    Spinner citySpinner;
    Button nextBtn;
    EditText firstNameInput, lastNameInput, usernameInput;

    SharedPreferences userPrefs;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Intent intent = new Intent(ProfileActivity.this, CropActivity.class);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        userPrefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        settupprofile = findViewById(R.id.profileImage);
        ImageButton btnAddImage = findViewById(R.id.btnAddImage);

        firstNameInput = findViewById(R.id.firstName);
        lastNameInput = findViewById(R.id.lastName);
        usernameInput = findViewById(R.id.username);

        firstNameInput.setFilters(new InputFilter[]{
                new InputFilter.AllCaps()
        });

        lastNameInput.setFilters(new InputFilter[]{
                new InputFilter.AllCaps()
        });

        usernameInput.setFilters(new InputFilter[]{
                new InputFilter.AllCaps()
        });

        nextBtn = findViewById(R.id.nextBtn);
        citySpinner = findViewById(R.id.citySpinner);

        setupCitySpinner();

        nextBtn.setOnClickListener(v -> {
            String firstName = firstNameInput.getText().toString().trim().toUpperCase(Locale.ROOT);
            String lastName = lastNameInput.getText().toString().trim().toUpperCase(Locale.ROOT);
            String username = usernameInput.getText().toString().trim().toUpperCase(Locale.ROOT);
            String selectedCity = citySpinner.getSelectedItem().toString();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || selectedCity.equals("SELECT CITY")) {
                Toast.makeText(ProfileActivity.this, "Please fill out all fields and select a city.", Toast.LENGTH_SHORT).show();
                return;
            }

            userPrefs.edit()
                    .putString("firstName", firstName)
                    .putString("lastName", lastName)
                    .putString("username", username)
                    .putString("city", selectedCity)
                    .putBoolean("isSetupDone", true)
                    .apply();

            Intent intent = new Intent(ProfileActivity.this, Profile2Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void setupCitySpinner() {
        String[] cities = {
                "SELECT CITY",
                "MANILA CITY",
                "QUEZON CITY",
                "MAKATI CITY",
                "TAGUIG CITY",
                "PASIG CITY"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                cities
        );

        citySpinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            String croppedImagePath = data.getStringExtra("croppedImagePath");

            if (croppedImagePath != null) {
                settupprofile.setImageURI(Uri.fromFile(new java.io.File(croppedImagePath)));

                userPrefs.edit()
                        .putString("profileImageUri", croppedImagePath)
                        .apply();
            } else {
                Toast.makeText(this, "No cropped image received", Toast.LENGTH_SHORT).show();
            }
        }
    }
}