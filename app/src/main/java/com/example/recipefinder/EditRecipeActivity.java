package com.example.recipefinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem; // NEW: Import MenuItem
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // NEW: Import NonNull
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView; // NEW: Import BottomNavigationView
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditRecipeActivity extends AppCompatActivity {

    private EditText editRecipeTitle, editRecipeSummary, editRecipeIngredients,
            editRecipeInstructions, editRecipeUserNotes, editRecipeImageUrl;
    private Button saveRecipeButton;
    private TextView screenTitleTextView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uid;
    private String firebaseDocId = null;
    private RecipeModel currentRecipe;

    private static final String TAG = "EditRecipeActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "You need to be logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        screenTitleTextView = findViewById(R.id.screenTitle);
        editRecipeTitle = findViewById(R.id.editRecipeTitle);
        editRecipeSummary = findViewById(R.id.editRecipeSummary);
        editRecipeIngredients = findViewById(R.id.editRecipeIngredients);
        editRecipeInstructions = findViewById(R.id.editRecipeInstructions);
        editRecipeUserNotes = findViewById(R.id.editRecipeUserNotes);
        editRecipeImageUrl = findViewById(R.id.editRecipeImageUrl);
        saveRecipeButton = findViewById(R.id.saveRecipeButton);

        // Check if we are editing an existing recipe or adding a new one
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("firebaseDocId")) {
            firebaseDocId = intent.getStringExtra("firebaseDocId");
            screenTitleTextView.setText("Edit Your Saved Recipe");
            loadRecipeData(); // Load existing recipe data for editing
        } else {
            screenTitleTextView.setText("Add New Recipe");
            saveRecipeButton.setText("Add Recipe"); // Change button text for adding
            currentRecipe = new RecipeModel(); // Initialize an empty model for new recipe
        }

        saveRecipeButton.setOnClickListener(v -> saveRecipe());

        // --- Bottom Navigation Setup for EditRecipeActivity ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_edit_recipe);

        // Highlight the "Add Recipe" item when this activity is active
        bottomNavigationView.setSelectedItemId(R.id.nav_add_recipe);

        // Set up the listener for item clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_view_recipes) {
                    startActivity(new Intent(EditRecipeActivity.this, MyRecipes.class));
                    // Optional: remove transition animation for smoother feel
                    overridePendingTransition(0, 0);
                    // Consider finishing this activity if you don't want it on the back stack
                    // after navigating to a main section.
                    return true;
                } else if (itemId == R.id.nav_add_recipe) {
                    // We are already on the Add/Edit Recipe screen, do nothing.
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(EditRecipeActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
        // --- End Bottom Navigation Setup ---
    }

    private void loadRecipeData() {
        if (uid == null || firebaseDocId == null) {
            Toast.makeText(this, "Error: Cannot load recipe data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Users").document(uid)
                .collection("SavedRecipes").document(firebaseDocId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentRecipe = documentSnapshot.toObject(RecipeModel.class);
                        if (currentRecipe != null) {
                            // Pre-fill fields with existing data
                            editRecipeTitle.setText(currentRecipe.getTitle());
                            editRecipeSummary.setText(currentRecipe.getSummary());
                            editRecipeIngredients.setText(currentRecipe.getIngredients());
                            editRecipeInstructions.setText(currentRecipe.getInstructions());
                            editRecipeUserNotes.setText(currentRecipe.getUserNotes());
                            editRecipeImageUrl.setText(currentRecipe.getImageUrl());
                            saveRecipeButton.setText("Save All Changes");
                        }
                    } else {
                        Toast.makeText(EditRecipeActivity.this, "Recipe not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditRecipeActivity.this, "Failed to load recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading recipe data", e);
                    finish();
                });
    }

    private void saveRecipe() {
        String title = editRecipeTitle.getText().toString().trim();
        String summary = editRecipeSummary.getText().toString().trim();
        String ingredients = editRecipeIngredients.getText().toString().trim();
        String instructions = editRecipeInstructions.getText().toString().trim();
        String userNotes = editRecipeUserNotes.getText().toString().trim();
        String imageUrl = editRecipeImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editRecipeTitle.setError("Recipe title is required!");
            return;
        }

        if (currentRecipe == null) {
            currentRecipe = new RecipeModel();
            currentRecipe.setId(0); // Mark as custom recipe
        }

        currentRecipe.setTitle(title);
        currentRecipe.setCustomTitle(title);
        currentRecipe.setSummary(summary);
        currentRecipe.setIngredients(ingredients);
        currentRecipe.setInstructions(instructions);
        currentRecipe.setUserNotes(userNotes);
        currentRecipe.setImageUrl(imageUrl);

        DocumentReference docRef;
        if (firebaseDocId != null) {
            docRef = db.collection("Users").document(uid)
                    .collection("SavedRecipes").document(firebaseDocId);
        } else {
            docRef = db.collection("Users").document(uid)
                    .collection("SavedRecipes").document();
        }

        docRef.set(currentRecipe)
                .addOnSuccessListener(aVoid -> {
                    if (firebaseDocId == null) {
                        Toast.makeText(EditRecipeActivity.this, "Recipe added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditRecipeActivity.this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                    }
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditRecipeActivity.this, "Error saving recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving recipe", e);
                });
    }
}