package com.example.anotara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap nMap;
    private FusedLocationProviderClient fusedLocationClient;

    LinearLayout firefly, location, plant, profile;
    LinearLayout locationBottomPanel;

    View panelHandle;

    TextView levelText, questTitle, questDescription;
    TextView placeName, placeAddress, placeLocated, placeClosing;
    ImageView placeImage;

    private long startTime;
    private int currentLevel;

    private int selectedLevel;
    private int selectedQuestNumber;
    private String selectedDestinationKey;

    private float panelStartY;
    private float panelDownY;
    private boolean isDraggingPanel = false;

    SharedPreferences prefs;

    private LocationCallback questLocationCallback;
    private long questStayStartTime = 0L;
    private boolean questTimerRunning = false;

    private double activeDestinationLat;
    private double activeDestinationLng;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final float QUEST_DISTANCE_LIMIT_METERS = 100f;
    private static final long QUEST_STAY_REQUIRED_MS = 10 * 60 * 1000; // 10 minutes

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        prefs = getSharedPreferences("Progress", MODE_PRIVATE);
        currentLevel = prefs.getInt("currentLevel", 1);

        selectedLevel = getIntent().getIntExtra("levelNumber", currentLevel);
        selectedQuestNumber = getIntent().getIntExtra("questNumber", 0);
        selectedDestinationKey = getIntent().getStringExtra("destinationKey");

        if (selectedQuestNumber == 0 && selectedDestinationKey != null) {
            selectedQuestNumber = getQuestNumberFromDestinationKey(selectedDestinationKey);
        }

        firefly = findViewById(R.id.firefly);
        location = findViewById(R.id.location);
        plant = findViewById(R.id.plant);
        profile = findViewById(R.id.profile);

        locationBottomPanel = findViewById(R.id.locationBottomPanel);
        panelHandle = findViewById(R.id.panelHandle);

        levelText = findViewById(R.id.levelText);
        questTitle = findViewById(R.id.questTitle);
        questDescription = findViewById(R.id.questDescription);

        placeName = findViewById(R.id.placeName);
        placeAddress = findViewById(R.id.placeAddress);
        placeLocated = findViewById(R.id.placeLocated);
        placeClosing = findViewById(R.id.placeClosing);
        placeImage = findViewById(R.id.placeImage);

        hideLocationPanelImmediately();
        setupPanelSwipeDown();

        // For Master the Basics quest
        prefs.edit()
                .putBoolean("locationClicked", true)
                .apply();

        firefly.setOnClickListener(v ->
                startActivity(new Intent(LocationActivity.this, FireflyActivity.class)));

        plant.setOnClickListener(v ->
                startActivity(new Intent(LocationActivity.this, PlantActivity.class)));

        profile.setOnClickListener(v ->
                startActivity(new Intent(LocationActivity.this, Profile2Activity.class)));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentLevel = prefs.getInt("currentLevel", 1);
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Level 1 only: Scout the Area = stay in LocationActivity for 5 seconds
        if (currentLevel == 1 && duration >= 5000) {
            prefs.edit()
                    .putBoolean("L1_Q2_DONE", true)
                    .apply();
        }

        stopQuestStayCheck();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        nMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        nMap.setMyLocationEnabled(true);

        if (selectedDestinationKey != null && !selectedDestinationKey.isEmpty()) {
            loadSelectedDestination(selectedLevel, selectedDestinationKey);
        } else {
            if (currentLevel == 1) {
                showUserLocation();
                showDefaultMapForLevelOne();
            } else {
                loadLevelPins(currentLevel);
            }
        }
    }

    private void showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, userLocation -> {
            if (userLocation != null && nMap != null) {
                LatLng currentLatLng = new LatLng(
                        userLocation.getLatitude(),
                        userLocation.getLongitude()
                );

                nMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("You are here"));

                nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });
    }

    private void showDefaultMapForLevelOne() {
        hideLocationPanelImmediately();
    }

    private void loadLevelPins(int level) {
        hideLocationPanelImmediately();

        if (nMap != null) {
            nMap.clear();
            showUserLocation();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("QUEST")
                .document("LEVEL_" + level)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "No destinations found for Level " + level, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    boolean hasPins = false;
                    int pinCount = 0;
                    LatLng lastPoint = null;

                    for (int i = 1; i <= 4; i++) {
                        String key = "destination_" + i;

                        Object destinationObject = documentSnapshot.get(key);

                        if (!(destinationObject instanceof Map)) {
                            continue;
                        }

                        Map<String, Object> destination = (Map<String, Object>) destinationObject;

                        Object latObj = destination.get("lat");
                        Object lngObj = destination.get("lng");

                        if (!(latObj instanceof Number) || !(lngObj instanceof Number)) {
                            continue;
                        }

                        double lat = ((Number) latObj).doubleValue();
                        double lng = ((Number) lngObj).doubleValue();

                        String markerTitle = getFirstString(
                                destination,
                                "name",
                                "questTitle",
                                "placeName",
                                "Destination"
                        );

                        LatLng point = new LatLng(lat, lng);

                        Marker marker = nMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(markerTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        if (marker != null) {
                            marker.setTag(key);
                        }

                        builder.include(point);
                        hasPins = true;
                        pinCount++;
                        lastPoint = point;
                    }

                    nMap.setOnMarkerClickListener(marker -> {
                        Object tag = marker.getTag();

                        if (tag instanceof String) {
                            String destinationKey = tag.toString();

                            selectedLevel = level;
                            selectedDestinationKey = destinationKey;
                            selectedQuestNumber = getQuestNumberFromDestinationKey(destinationKey);

                            loadSelectedDestination(level, destinationKey);
                            return true;
                        }

                        return false;
                    });

                    if (hasPins) {
                        if (pinCount == 1 && lastPoint != null) {
                            nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 15));
                        } else {
                            nMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
                        }
                    } else {
                        Toast.makeText(this, "No pins available for this level.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load destinations.", Toast.LENGTH_SHORT).show());
    }

    private void loadSelectedDestination(int level, String destinationKey) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("QUEST")
                .document("LEVEL_" + level)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Destination not found.", Toast.LENGTH_SHORT).show();

                        if (level == 1) {
                            showDefaultMapForLevelOne();
                        }

                        return;
                    }

                    Object destinationObject = documentSnapshot.get(destinationKey);

                    if (!(destinationObject instanceof Map)) {
                        Toast.makeText(this, "Destination details not found.", Toast.LENGTH_SHORT).show();

                        if (level == 1) {
                            showDefaultMapForLevelOne();
                        }

                        return;
                    }

                    Map<String, Object> destination = (Map<String, Object>) destinationObject;

                    Object latObj = destination.get("lat");
                    Object lngObj = destination.get("lng");

                    if (!(latObj instanceof Number) || !(lngObj instanceof Number)) {
                        Toast.makeText(this, "Invalid destination location.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double lat = ((Number) latObj).doubleValue();
                    double lng = ((Number) lngObj).doubleValue();

                    String qTitle = getFirstString(
                            destination,
                            "questTitle",
                            "questName",
                            "name",
                            "Sample Quest"
                    );

                    String qDesc = getFirstString(
                            destination,
                            "questDescription",
                            "description",
                            "desc",
                            "Visit this place and complete the assigned local quest."
                    );

                    String pName = getFirstString(
                            destination,
                            "placeName",
                            "place",
                            "destinationName",
                            qTitle + " Spot"
                    );

                    String address = getFirstString(
                            destination,
                            "address",
                            "placeAddress",
                            "locationAddress",
                            "Sample address, Metro Manila"
                    );

                    String located = getFirstString(
                            destination,
                            "located",
                            "locatedIn",
                            "placeLocated",
                            "Located nearby"
                    );

                    String closing = getFirstString(
                            destination,
                            "closing",
                            "closingTime",
                            "hours",
                            "Open until 10PM"
                    );

                    String markerTitle;

                    if (!pName.isEmpty()) {
                        markerTitle = pName;
                    } else if (!qTitle.isEmpty()) {
                        markerTitle = qTitle;
                    } else {
                        markerTitle = "Destination";
                    }

                    LatLng point = new LatLng(lat, lng);

                    nMap.clear();
                    showUserLocation();

                    Marker selectedMarker = nMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title(markerTitle)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    if (selectedMarker != null) {
                        selectedMarker.setTag(destinationKey);
                    }

                    nMap.setOnMarkerClickListener(marker -> {
                        Object tag = marker.getTag();

                        if (tag instanceof String) {
                            String clickedKey = tag.toString();

                            selectedLevel = level;
                            selectedDestinationKey = clickedKey;
                            selectedQuestNumber = getQuestNumberFromDestinationKey(clickedKey);

                            loadSelectedDestination(level, clickedKey);
                            return true;
                        }

                        return false;
                    });

                    nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16));

                    levelText.setText("Level " + level);

                    questTitle.setText(qTitle);
                    questDescription.setText(qDesc);

                    placeName.setText(pName);
                    placeAddress.setText(address);
                    placeLocated.setText(located);
                    placeClosing.setText(closing);

                    loadPlaceImage(destination);

                    showLocationPanel();

                    checkIfUserReachedDestination(lat, lng);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load destination details.", Toast.LENGTH_SHORT).show());
    }

    private void checkIfUserReachedDestination(double destinationLat, double destinationLng) {
        if (selectedLevel == 1) {
            return;
        }

        if (selectedQuestNumber <= 0) {
            return;
        }

        String doneKey = "L" + selectedLevel + "_Q" + selectedQuestNumber + "_DONE";

        if (prefs.getBoolean(doneKey, false)) {
            Toast.makeText(this, "Quest already completed. You can claim it.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        activeDestinationLat = destinationLat;
        activeDestinationLng = destinationLng;

        startQuestStayCheck();
    }

    private void startQuestStayCheck() {
        stopQuestStayCheck();

        questStayStartTime = 0L;
        questTimerRunning = false;

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        questLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    return;
                }

                Location userLocation = locationResult.getLastLocation();

                float[] results = new float[1];

                Location.distanceBetween(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        activeDestinationLat,
                        activeDestinationLng,
                        results
                );

                float distanceInMeters = results[0];

                if (distanceInMeters <= QUEST_DISTANCE_LIMIT_METERS) {
                    if (!questTimerRunning) {
                        questTimerRunning = true;
                        questStayStartTime = System.currentTimeMillis();

                        Toast.makeText(
                                LocationActivity.this,
                                "You reached the quest location. Stay here for 10 minutes.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    long stayedTime = System.currentTimeMillis() - questStayStartTime;

                    if (stayedTime >= QUEST_STAY_REQUIRED_MS) {
                        completeQuestAfterStay();
                    }

                } else {
                    if (questTimerRunning) {
                        Toast.makeText(
                                LocationActivity.this,
                                "You moved away from the quest location. Timer reset.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                LocationActivity.this,
                                "You are still " + Math.round(distanceInMeters) + "m away from the quest location.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    questTimerRunning = false;
                    questStayStartTime = 0L;
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                questLocationCallback,
                Looper.getMainLooper()
        );
    }

    private void completeQuestAfterStay() {
        String doneKey = "L" + selectedLevel + "_Q" + selectedQuestNumber + "_DONE";

        prefs.edit()
                .putBoolean(doneKey, true)
                .apply();

        Toast.makeText(
                this,
                "Quest completed! You can now claim it.",
                Toast.LENGTH_LONG
        ).show();

        stopQuestStayCheck();
    }

    private void stopQuestStayCheck() {
        if (fusedLocationClient != null && questLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(questLocationCallback);
        }

        questLocationCallback = null;
        questTimerRunning = false;
        questStayStartTime = 0L;
    }

    private int getQuestNumberFromDestinationKey(String destinationKey) {
        if (destinationKey == null) return 0;

        try {
            return Integer.parseInt(destinationKey.replace("destination_", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void setupPanelSwipeDown() {
        if (locationBottomPanel == null || panelHandle == null) return;

        panelHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    panelDownY = event.getRawY();
                    panelStartY = locationBottomPanel.getTranslationY();
                    isDraggingPanel = true;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!isDraggingPanel) return true;

                    float moveY = event.getRawY();
                    float distanceY = moveY - panelDownY;

                    if (distanceY > 0) {
                        locationBottomPanel.setTranslationY(panelStartY + distanceY);
                    }

                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDraggingPanel = false;

                    float finalTranslationY = locationBottomPanel.getTranslationY();

                    if (finalTranslationY > locationBottomPanel.getHeight() * 0.25f) {
                        hideLocationPanelAndReturnToPins();
                    } else {
                        locationBottomPanel.animate()
                                .translationY(0)
                                .setDuration(200)
                                .start();
                    }

                    return true;
            }

            return false;
        });
    }

    private void showLocationPanel() {
        if (locationBottomPanel == null) return;

        locationBottomPanel.animate().cancel();
        locationBottomPanel.setVisibility(View.VISIBLE);

        locationBottomPanel.post(() -> {
            locationBottomPanel.setTranslationY(locationBottomPanel.getHeight());

            locationBottomPanel.animate()
                    .translationY(0)
                    .setDuration(250)
                    .start();
        });
    }

    private void hideLocationPanelAndReturnToPins() {
        if (locationBottomPanel == null) return;

        locationBottomPanel.animate()
                .translationY(locationBottomPanel.getHeight())
                .setDuration(220)
                .withEndAction(() -> {
                    locationBottomPanel.setVisibility(View.GONE);
                    locationBottomPanel.setTranslationY(0);

                    returnToLevelQuestPins();
                })
                .start();
    }

    private void hideLocationPanelImmediately() {
        if (locationBottomPanel == null) return;

        locationBottomPanel.animate().cancel();
        locationBottomPanel.setVisibility(View.GONE);
        locationBottomPanel.setTranslationY(0);
    }

    private void returnToLevelQuestPins() {
        if (nMap == null) return;

        stopQuestStayCheck();

        selectedDestinationKey = null;
        selectedQuestNumber = 0;

        if (selectedLevel <= 1) {
            nMap.clear();
            showUserLocation();
            showDefaultMapForLevelOne();
        } else {
            loadLevelPins(selectedLevel);
        }
    }

    private void loadPlaceImage(Map<String, Object> destination) {
        String imageName = getFirstString(
                destination,
                "image",
                "imageName",
                "drawable",
                ""
        );

        if (imageName.isEmpty()) {
            placeImage.setImageResource(R.drawable.background);
            return;
        }

        int resId = getResources().getIdentifier(
                imageName,
                "drawable",
                getPackageName()
        );

        if (resId != 0) {
            placeImage.setImageResource(resId);
        } else {
            placeImage.setImageResource(R.drawable.background);
        }
    }

    private String getFirstString(Map<String, Object> map, String key1, String key2, String defaultValue) {
        Object value1 = map.get(key1);
        if (value1 != null) return value1.toString();

        Object value2 = map.get(key2);
        if (value2 != null) return value2.toString();

        return defaultValue;
    }

    private String getFirstString(Map<String, Object> map, String key1, String key2, String key3, String defaultValue) {
        Object value1 = map.get(key1);
        if (value1 != null) return value1.toString();

        Object value2 = map.get(key2);
        if (value2 != null) return value2.toString();

        Object value3 = map.get(key3);
        if (value3 != null) return value3.toString();

        return defaultValue;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (nMap != null) {
                    onMapReady(nMap);
                }

            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}