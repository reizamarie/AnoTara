
package com.example.anotara;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    EditText email;
    Button nextBtn, googleBtn;
    CheckBox termsCheckBox;
    ConstraintLayout contentLayout;
    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.emailInput);
        nextBtn = findViewById(R.id.nextBtn);
        googleBtn = findViewById(R.id.googleBtn);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        contentLayout = findViewById(R.id.contentLayout);

        setupTermsText();

        contentLayout.setAlpha(0f);
        contentLayout.setVisibility(View.VISIBLE);

        contentLayout.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(1000)
                .start();

        nextBtn.setOnClickListener(v -> {
            String input = email.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                Toast.makeText(MainActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            }
            else if (!termsCheckBox.isChecked()) {
                Toast.makeText(MainActivity.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                String name = account.getDisplayName();
                String email = account.getEmail();

                // SUCCESS → go to ProfileActivity
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                startActivity(intent);

            } catch (ApiException e) {
                Toast.makeText(this, "Sign-in failed. Code: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void setupTermsText() {
        String text = "I have read and agree to the Terms and Conditions, Privacy Policy, and Giveaway Terms.";

        SpannableString spannable = new SpannableString(text);

        ClickableSpan termsClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(MainActivity.this, TermsActivity.class));
            }
        };

        int start = text.indexOf("Terms");
        int end = text.length();

        spannable.setSpan(termsClick, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsCheckBox.setText(spannable);
        termsCheckBox.setMovementMethod(LinkMovementMethod.getInstance());
    }
}