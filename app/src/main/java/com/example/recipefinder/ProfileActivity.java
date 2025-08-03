package com.example.recipefinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // NEW: Import MenuItem
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // NEW: Import NonNull
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView; // NEW: Import BottomNavigationView
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtEmail, txtPreference;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    private EditText editPreference;
    private Button btnSavePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtEmail = findViewById(R.id.txtEmail);
        txtPreference = findViewById(R.id.txtPreference);
        btnLogout = findViewById(R.id.btnLogout);

        editPreference = findViewById(R.id.editPreference);
        btnSavePref = findViewById(R.id.btnSavePref);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
            txtEmail.setText("Email: " + mAuth.getCurrentUser().getEmail());

            db.collection("Users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String pref = documentSnapshot.getString("diet");
                            txtPreference.setText("Dietary Preference: " + (pref != null ? pref : "None"));
                            if (pref != null) {
                                editPreference.setText(pref);
                            }
                        } else {
                            Log.d("ProfileActivity", "User document for " + uid + " does not exist. Creating it.");
                            db.collection("Users").document(uid).set(new HashMap<>())
                                    .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "User document created."))
                                    .addOnFailureListener(e -> Log.e("ProfileActivity", "Error creating user document: " + e.getMessage()));
                            txtPreference.setText("Dietary Preference: None");
                            editPreference.setText("");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Error fetching user document: " + e.getMessage());
                        Toast.makeText(this, "Error loading profile data.", Toast.LENGTH_SHORT).show();
                    });

            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(ProfileActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });

            btnSavePref.setOnClickListener(v -> {
                String pref = editPreference.getText().toString().trim();
                if (pref.isEmpty()) {
                    Toast.makeText(this, "Preference cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> userData = new HashMap<>();
                userData.put("diet", pref);

                db.collection("Users").document(uid)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Preference updated!", Toast.LENGTH_SHORT).show();
                            txtPreference.setText("Dietary Preference: " + pref);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileActivity", "Failed to update preference: " + e.getMessage(), e);
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });

        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // --- Bottom Navigation Setup for ProfileActivity ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_profile);

        // Highlight the "Profile" item when this activity is active
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Set up the listener for item clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_view_recipes) {
                    startActivity(new Intent(ProfileActivity.this, MyRecipes.class));
                    overridePendingTransition(0, 0);
                    // You might finish() this activity here if you want, same considerations as before.
                    return true;
                } else if (itemId == R.id.nav_add_recipe) {
                    startActivity(new Intent(ProfileActivity.this, EditRecipeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // We are already on the Profile screen, do nothing.
                    return true;
                }
                return false;
            }
        });
        // --- End Bottom Navigation Setup ---
    }
}