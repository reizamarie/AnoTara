package com.example.anotara;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.Random;

public class FireflyActivity extends AppCompatActivity {

    LinearLayout firefly, location, plant, profile;
    LinearLayout cardProfile, cardScout, cardPersonalize, cardMaster;

    AppCompatButton btnClaimProfile, btnClaimScout, btnClaimPersonalize, btnClaimMaster;

    ImageView imgChange1, imgChange2, imgChange3, imgChange4;
    SeekBar seekBar;
    TextView txtLevel;

    TextView txtQuestTitle1, txtQuestDesc1;
    TextView txtQuestTitle2, txtQuestDesc2;
    TextView txtQuestTitle3, txtQuestDesc3;
    TextView txtQuestTitle4, txtQuestDesc4;

    int currentLevel;
    int progress;

    boolean q1Done, q2Done, q3Done, q4Done;
    boolean q1Claimed, q2Claimed, q3Claimed, q4Claimed;

    SharedPreferences prefs;

    String[][][] levels = {
            {
                    {"Create Your Profile", "Sign up and log in to begin your journey."},
                    {"Scout the Area", "Stay in the location screen for 5 seconds."},
                    {"Personalize Your Vibe", "Set up your name and explorer profile."},
                    {"Master the Basics", "Click all bottom navigation buttons."}
            },
            {
                    {"Caffeine Quest", "Visit an independent coffee shop."},
                    {"Park Ranger", "Check in at your nearest public park."},
                    {"Street Food Crawl", "Try a local food stall."},
                    {"Heritage Hunt", "Visit a historical landmark."}
            },
            {
                    {"Joystick Joyride", "Go to an arcade."},
                    {"Golden Hour", "Check in at sunset."},
                    {"Culture Vulture", "Visit a museum."},
                    {"Thrifty Finds", "Find something cheap and unique."}
            },
            {
                    {"Nature Explorer", "Explore green places."},
                    {"Care Routine", "Maintain your plant."},
                    {"Firefly Saver", "Collect enough fireflies."},
                    {"Level Builder", "Complete all tasks."}
            },
            {
                    {"Community Scout", "Explore helpful places."},
                    {"Plant Guardian", "Keep plant active."},
                    {"Firefly Collector", "Gather fireflies."},
                    {"Almost There", "Finish all tasks."}
            },
            {
                    {"Final Explorer", "Complete last exploration."},
                    {"Master Gardener", "Max your plant."},
                    {"Firefly Champion", "Final collection."},
                    {"Journey Complete", "Finish everything."}
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firefly);

        firefly = findViewById(R.id.firefly);
        location = findViewById(R.id.location);
        plant = findViewById(R.id.plant);
        profile = findViewById(R.id.profile);

        cardProfile = findViewById(R.id.cardProfile);
        cardScout = findViewById(R.id.cardScout);
        cardPersonalize = findViewById(R.id.cardPersonalize);
        cardMaster = findViewById(R.id.cardMaster);

        btnClaimProfile = findViewById(R.id.btnClaimProfile);
        btnClaimScout = findViewById(R.id.btnClaimScout);
        btnClaimPersonalize = findViewById(R.id.btnClaimPersonalize);
        btnClaimMaster = findViewById(R.id.btnClaimMaster);

        imgChange1 = findViewById(R.id.imgChange1);
        imgChange2 = findViewById(R.id.imgChange2);
        imgChange3 = findViewById(R.id.imgChange3);
        imgChange4 = findViewById(R.id.imgChange4);

        seekBar = findViewById(R.id.seekBar);
        txtLevel = findViewById(R.id.txtLevel);

        txtQuestTitle1 = findViewById(R.id.txtQuestTitle1);
        txtQuestDesc1 = findViewById(R.id.txtQuestDesc1);

        txtQuestTitle2 = findViewById(R.id.txtQuestTitle2);
        txtQuestDesc2 = findViewById(R.id.txtQuestDesc2);

        txtQuestTitle3 = findViewById(R.id.txtQuestTitle3);
        txtQuestDesc3 = findViewById(R.id.txtQuestDesc3);

        txtQuestTitle4 = findViewById(R.id.txtQuestTitle4);
        txtQuestDesc4 = findViewById(R.id.txtQuestDesc4);

        prefs = getSharedPreferences("Progress", MODE_PRIVATE);

        seekBar.setEnabled(false);
        seekBar.setOnTouchListener((v, e) -> true);

        plant.setOnClickListener(v -> {
            markBottomNavClicked("plantClicked");
            startActivity(new Intent(this, PlantActivity.class));
        });

        location.setOnClickListener(v -> {
            markBottomNavClicked("locationClicked");
            startActivity(new Intent(this, LocationActivity.class));
        });

        profile.setOnClickListener(v -> {
            markBottomNavClicked("profileClicked");
            startActivity(new Intent(this, Profile2Activity.class));
        });

        cardProfile.setOnClickListener(v -> openQuestLocation(1));
        cardScout.setOnClickListener(v -> openQuestLocation(2));
        cardPersonalize.setOnClickListener(v -> openQuestLocation(3));
        cardMaster.setOnClickListener(v -> openQuestLocation(4));

        btnClaimProfile.setOnClickListener(v -> claimQuest(1));
        btnClaimScout.setOnClickListener(v -> claimQuest(2));
        btnClaimPersonalize.setOnClickListener(v -> claimQuest(3));
        btnClaimMaster.setOnClickListener(v -> claimQuest(4));
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadState();
        checkLevelOneRequirements();
        loadState();
        updateUI();
    }

    private void loadState() {
        currentLevel = prefs.getInt("currentLevel", 1);
        progress = prefs.getInt("progress", 0);

        q1Done = prefs.getBoolean("L" + currentLevel + "_Q1_DONE", false);
        q2Done = prefs.getBoolean("L" + currentLevel + "_Q2_DONE", false);
        q3Done = prefs.getBoolean("L" + currentLevel + "_Q3_DONE", false);
        q4Done = prefs.getBoolean("L" + currentLevel + "_Q4_DONE", false);

        q1Claimed = prefs.getBoolean("L" + currentLevel + "_Q1", false);
        q2Claimed = prefs.getBoolean("L" + currentLevel + "_Q2", false);
        q3Claimed = prefs.getBoolean("L" + currentLevel + "_Q3", false);
        q4Claimed = prefs.getBoolean("L" + currentLevel + "_Q4", false);
    }

    private void checkLevelOneRequirements() {
        if (currentLevel != 1) return;

        SharedPreferences.Editor editor = prefs.edit();

        // Q1: Create Your Profile
        // Since user is already logged in when they reach FireflyActivity
        editor.putBoolean("L1_Q1_DONE", true);

        // Q3: Personalize Your Vibe
        // Since login already requires setting name
        editor.putBoolean("L1_Q3_DONE", true);

        editor.apply();

        checkMasterBasicsRequirement();
    }

    private void checkMasterBasicsRequirement() {
        if (currentLevel != 1) return;

        boolean plantClicked = prefs.getBoolean("plantClicked", false);
        boolean locationClicked = prefs.getBoolean("locationClicked", false);
        boolean profileClicked = prefs.getBoolean("profileClicked", false);

        if (plantClicked && locationClicked && profileClicked) {
            prefs.edit()
                    .putBoolean("L1_Q4_DONE", true)
                    .apply();
        }
    }

    private void markBottomNavClicked(String key) {
        prefs.edit()
                .putBoolean(key, true)
                .apply();

        checkMasterBasicsRequirement();
    }

    private void updateUI() {
        txtLevel.setText("Level " + currentLevel);
        seekBar.setProgress(progress);

        loadQuestTexts();

        updateCard(cardProfile, btnClaimProfile, q1Done, q1Claimed);
        updateCard(cardScout, btnClaimScout, q2Done, q2Claimed);
        updateCard(cardPersonalize, btnClaimPersonalize, q3Done, q3Claimed);
        updateCard(cardMaster, btnClaimMaster, q4Done, q4Claimed);

        updateQuestImages();
    }

    private void updateQuestImages() {
        imgChange1.setImageResource(q1Claimed ? R.drawable.done : R.drawable.create);
        imgChange2.setImageResource(q2Claimed ? R.drawable.done : R.drawable.scout);
        imgChange3.setImageResource(q3Claimed ? R.drawable.done : R.drawable.personalize);
        imgChange4.setImageResource(q4Claimed ? R.drawable.done : R.drawable.master);
    }

    private void loadQuestTexts() {
        int index = currentLevel - 1;

        if (index < 0 || index >= levels.length) return;

        txtQuestTitle1.setText(levels[index][0][0]);
        txtQuestDesc1.setText(levels[index][0][1]);

        txtQuestTitle2.setText(levels[index][1][0]);
        txtQuestDesc2.setText(levels[index][1][1]);

        txtQuestTitle3.setText(levels[index][2][0]);
        txtQuestDesc3.setText(levels[index][2][1]);

        txtQuestTitle4.setText(levels[index][3][0]);
        txtQuestDesc4.setText(levels[index][3][1]);
    }

    private void updateCard(LinearLayout card, AppCompatButton btn, boolean done, boolean claimed) {
        if (claimed) {
            card.setBackgroundResource(R.drawable.task_claimed);
            btn.setVisibility(View.INVISIBLE);
            btn.setEnabled(false);
        } else if (done) {
            card.setBackgroundResource(R.drawable.bg_task_outline);
            btn.setVisibility(View.VISIBLE);
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.claim_ready);
        } else {
            card.setBackgroundResource(R.drawable.bg_task_outline);
            btn.setVisibility(View.VISIBLE);
            btn.setEnabled(false);
            btn.setBackgroundResource(R.drawable.claim_locked);
        }
    }

    private void claimQuest(int questNumber) {
        if (!isQuestDone(questNumber)) return;

        String key = "L" + currentLevel + "_Q" + questNumber;

        if (prefs.getBoolean(key, false)) return;

        progress += 25;

        prefs.edit()
                .putBoolean(key, true)
                .putInt("progress", progress)
                .apply();

        loadState();
        updateUI();
        checkLevelComplete();
    }
    private boolean isQuestDone(int questNumber) {
        if (questNumber == 1) return q1Done;
        if (questNumber == 2) return q2Done;
        if (questNumber == 3) return q3Done;
        if (questNumber == 4) return q4Done;
        return false;
    }

    private void openQuestLocation(int questNumber) {
        if (currentLevel == 1) {
            if (questNumber == 2 && !q2Done) {
                Intent intent = new Intent(FireflyActivity.this, LocationActivity.class);
                intent.putExtra("questNumber", 2);
                intent.putExtra("levelNumber", 1);
                intent.putExtra("destinationKey", "destination_2");
                startActivity(intent);
            }
            return;
        }

        if (isQuestDone(questNumber)) return;

        Intent intent = new Intent(FireflyActivity.this, LocationActivity.class);
        intent.putExtra("questNumber", questNumber);
        intent.putExtra("levelNumber", currentLevel);
        intent.putExtra("destinationKey", "destination_" + questNumber);
        startActivity(intent);
    }

    private void checkLevelComplete() {
        if (progress >= 100) {
            playLevelUpAnimation("Level " + currentLevel + " Complete");

            new Handler().postDelayed(() -> {
                if (currentLevel < 6) {
                    currentLevel++;
                    progress = 0;

                    prefs.edit()
                            .putInt("currentLevel", currentLevel)
                            .putInt("progress", progress)
                            .apply();

                    loadState();
                    updateUI();

                    playLevelUpAnimation("Level " + currentLevel);
                } else {
                    playLevelUpAnimation("All Levels Complete");
                }
            }, 2500);
        }
    }

    private void playLevelUpAnimation(String message) {
        txtLevel.setText(message);

        txtLevel.setScaleX(0.6f);
        txtLevel.setScaleY(0.6f);
        txtLevel.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(txtLevel, View.SCALE_X, 0.6f, 1.25f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(txtLevel, View.SCALE_Y, 0.6f, 1.25f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(txtLevel, View.ALPHA, 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, fadeIn);
        animatorSet.setDuration(1200);
        animatorSet.start();

        createFireworkBurst();
    }

    private void createFireworkBurst() {
        ViewGroup root = findViewById(android.R.id.content);

        if (root == null || txtLevel == null) return;

        root.post(() -> {
            int[] txtLocation = new int[2];
            int[] rootLocation = new int[2];

            txtLevel.getLocationOnScreen(txtLocation);
            root.getLocationOnScreen(rootLocation);

            float centerX = txtLocation[0] - rootLocation[0] + txtLevel.getWidth() / 2f;
            float centerY = txtLocation[1] - rootLocation[1] + txtLevel.getHeight() / 2f;

            String[] fireworkSymbols = {"✦", "✧", "✨", "✹", "✺"};
            int[] fireworkColors = {
                    Color.parseColor("#FFF8C9"),
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFD966"),
                    Color.parseColor("#F6C453")
            };

            Random random = new Random();

            for (int i = 0; i < 16; i++) {
                TextView spark = new TextView(this);
                spark.setText(fireworkSymbols[random.nextInt(fireworkSymbols.length)]);
                spark.setTextSize(16 + random.nextInt(12));
                spark.setTextColor(fireworkColors[random.nextInt(fireworkColors.length)]);
                spark.setAlpha(1f);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                root.addView(spark, params);

                spark.setX(centerX);
                spark.setY(centerY);

                double angle = (2 * Math.PI * i) / 16;
                float distance = 60 + random.nextInt(80);

                float targetX = (float) (Math.cos(angle) * distance);
                float targetY = (float) (Math.sin(angle) * distance);

                spark.animate()
                        .translationX(targetX)
                        .translationY(targetY)
                        .alpha(0f)
                        .scaleX(1.8f)
                        .scaleY(1.8f)
                        .setDuration(1600)
                        .withEndAction(() -> root.removeView(spark))
                        .start();
            }
        });
    }
}