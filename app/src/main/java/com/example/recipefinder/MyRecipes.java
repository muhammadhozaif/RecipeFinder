package com.example.recipefinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Ensure this is androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyRecipes extends AppCompatActivity implements RecipeAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<RecipeModel> savedRecipes;
    private FirebaseFirestore db;
    private String uid;

    private ActivityResultLauncher<Intent> editRecipeLauncher;

    private static final String TAG = "MyRecipes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);
        Log.d(TAG, "LIFECYCLE: onCreate() called.");
        Log.d(TAG, "onCreate: Activity created.");

        // Initialize UI
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        savedRecipes = new ArrayList<>();

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            Log.d(TAG, "onCreate: User UID obtained: " + uid);
        } else {
            Toast.makeText(this, "Please log in to view recipes.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            Log.w(TAG, "onCreate: User not logged in, finishing activity.");
            return;
        }

        adapter = new RecipeAdapter(this, savedRecipes, this, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        editRecipeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "onActivityResult: Result received. ResultCode: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(MyRecipes.this, "Recipe updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onActivityResult: Reloading recipes after successful update.");
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Log.d(TAG, "onActivityResult: EditRecipeActivity cancelled or no changes.");
                    }
                }
        );

        // --- Bottom Navigation Setup for MyRecipes Activity ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_my_recipes);

        // Highlight the "My Recipes" item when this activity is active
        bottomNavigationView.setSelectedItemId(R.id.nav_view_recipes);

        // Set up the listener for item clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_view_recipes) {
                    // We are already on the MyRecipes screen, do nothing or refresh if needed
                    return true;
                } else if (itemId == R.id.nav_add_recipe) {
                    startActivity(new Intent(MyRecipes.this, EditRecipeActivity.class));
                    overridePendingTransition(0, 0); // Optional: for smooth transition without animation
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MyRecipes.this, ProfileActivity.class));
                    overridePendingTransition(0, 0); // Optional: for smooth transition without animation
                    return true;
                }
                return false;
            }
        });
        // --- End Bottom Navigation Setup ---
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "LIFECYCLE: onStart() called.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LIFECYCLE: onResume() called. Attempting to load recipes.");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            loadFavorites();
        } else {
            Toast.makeText(this, "User not logged in. Redirecting to login.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onResume: User not logged in, redirecting to LoginActivity.");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LIFECYCLE: onPause() called.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LIFECYCLE: onStop() called.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LIFECYCLE: onDestroy() called.");
    }

    private void loadFavorites() {
        Log.d(TAG, "loadFavorites() ENTERED: Current savedRecipes size before clear: " + savedRecipes.size());
        if (uid == null) {
            Log.w(TAG, "loadFavorites: UID is null, cannot load recipes.");
            return;
        }

        savedRecipes.clear();
        Log.d(TAG, "loadFavorites() AFTER CLEAR: savedRecipes size: " + savedRecipes.size());
        adapter.notifyDataSetChanged();

        db.collection("Users").document(uid)
                .collection("SavedRecipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int recipesFetchedCount = 0;
                    Log.d(TAG, "loadFavorites() Firestore SUCCESS: Fetched " + queryDocumentSnapshots.size() + " documents.");
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(MyRecipes.this, "No saved recipes yet. Add one from the 'Add Recipe' tab!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "loadFavorites() Firestore: No documents found for user.");
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RecipeModel recipe = doc.toObject(RecipeModel.class);
                        if (recipe != null) {
                            recipe.setFirebaseDocId(doc.getId());
                            savedRecipes.add(recipe);
                            recipesFetchedCount++;
                            Log.d(TAG, "loadFavorites() ADDED: " + (recipe.getCustomTitle() != null && !recipe.getCustomTitle().isEmpty() ? recipe.getCustomTitle() : recipe.getTitle()) +
                                    " (Firebase ID: " + recipe.getFirebaseDocId() + ", Spoonacular ID: " + recipe.getId() + "). Current list size: " + savedRecipes.size());
                        } else {
                            Log.e(TAG, "loadFavorites() ERROR: Failed to parse document ID " + doc.getId() + " to RecipeModel.");
                        }
                    }
                    Log.d(TAG, "loadFavorites() COMPLETE: Total recipes added: " + recipesFetchedCount + ". Final savedRecipes size: " + savedRecipes.size());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "loadFavorites() Firestore ERROR: " + e.getMessage(), e);
                });
    }

    @Override
    public void onItemClick(RecipeModel recipe) {
        Log.d(TAG, "onItemClick: Saved Recipe clicked. Firebase ID: " + recipe.getFirebaseDocId());
        Intent intent = new Intent(MyRecipes.this, RecipeDetailActivity.class);
        intent.putExtra("firebaseDocId", recipe.getFirebaseDocId());
        editRecipeLauncher.launch(intent);
    }

    @Override
    public void onRecipeLongClick(RecipeModel recipe) {
        Log.d(TAG, "onRecipeLongClick: Recipe long-clicked for deletion. Firebase ID: " + recipe.getFirebaseDocId());
        showDeleteConfirmationDialog(recipe);
    }

    private void showDeleteConfirmationDialog(RecipeModel recipeToDelete) {
        String displayTitle = recipeToDelete.getCustomTitle();
        if (displayTitle == null || displayTitle.isEmpty()) {
            displayTitle = recipeToDelete.getTitle();
        }
        Log.d(TAG, "showDeleteConfirmationDialog: Confirming deletion for: " + displayTitle);

        // Reverted to original AlertDialog.Builder call
        new AlertDialog.Builder(this) // <--- THIS LINE IS REVERTED
                .setTitle("Remove Recipe")
                .setMessage("Are you sure you want to remove \"" + displayTitle + "\" from your saved recipes?")
                .setPositiveButton("Remove", (dialog, which) -> deleteSavedRecipe(recipeToDelete.getFirebaseDocId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSavedRecipe(String docId) {
        Log.d(TAG, "deleteSavedRecipe: Attempting to delete Firebase ID: " + docId);
        if (uid == null || docId == null) {
            Toast.makeText(this, "Error: Cannot delete recipe (missing user/doc ID).", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "deleteSavedRecipe: UID or Doc ID is null.");
            return;
        }

        db.collection("Users").document(uid)
                .collection("SavedRecipes").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyRecipes.this, "Recipe removed successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "deleteSavedRecipe: Successfully deleted Firebase ID: " + docId);
                    loadFavorites();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyRecipes.this, "Error removing recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error deleting recipe Firebase ID " + docId + ": " + e.getMessage(), e);
                });
    }
}