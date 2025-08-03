package com.example.recipefinder;

import android.content.Intent;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import android.view.MenuItem;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView recipeDetailTitle, recipeSummary, recipeIngredients, recipeInstructions, recipeUserNotes;
    private ImageView recipeDetailImage;
    private ProgressBar detailProgressBar;
    private Button editCustomRecipeButton;
    private TextView userNotesLabel;

    private FirebaseFirestore db;
    private String uid;
    private String firebaseDocId;

    private static final String TAG = "RecipeDetailActivity";

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeDetailTitle = findViewById(R.id.recipeDetailTitle);
        recipeDetailImage = findViewById(R.id.recipeDetailImage);
        recipeSummary = findViewById(R.id.recipeSummary);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        recipeUserNotes = findViewById(R.id.recipeUserNotes);
        userNotesLabel = findViewById(R.id.recipeUserNotesLabel);
        detailProgressBar = findViewById(R.id.detailProgressBar);
        editCustomRecipeButton = findViewById(R.id.editCustomRecipeButton);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_view_recipes);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_view_recipes) {
                Intent intent = new Intent(RecipeDetailActivity.this, MyRecipes.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_add_recipe) {
                Intent intent = new Intent(RecipeDetailActivity.this, EditRecipeActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(RecipeDetailActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "Please log in to view recipe details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("firebaseDocId")) {
            firebaseDocId = intent.getStringExtra("firebaseDocId");
            loadRecipeDetailsFromFirestore(firebaseDocId);
        } else {
            Toast.makeText(this, "No recipe ID provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        editCustomRecipeButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(RecipeDetailActivity.this, EditRecipeActivity.class);
            editIntent.putExtra("firebaseDocId", firebaseDocId);
            startActivityForResult(editIntent, 1);
        });
    }

    private void loadRecipeDetailsFromFirestore(String docId) {
        if (uid == null || docId == null) {
            Toast.makeText(this, "Error: User or recipe ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        detailProgressBar.setVisibility(View.VISIBLE);
        db.collection("Users").document(uid)
                .collection("SavedRecipes").document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    detailProgressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        RecipeModel recipe = documentSnapshot.toObject(RecipeModel.class);
                        if (recipe != null) {
                            displayRecipeDetails(recipe);
                            editCustomRecipeButton.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, "Failed to parse recipe data.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Recipe object is null after conversion for docId: " + docId);
                        }
                    } else {
                        Toast.makeText(this, "Recipe not found or deleted.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Document not found for docId: " + docId);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    detailProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading recipe details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error loading recipe details from Firestore for docId: " + docId, e);
                });
    }

    private void displayRecipeDetails(RecipeModel recipe) {
        if (!TextUtils.isEmpty(recipe.getCustomTitle())) {
            recipeDetailTitle.setText(recipe.getCustomTitle());
        } else {
            recipeDetailTitle.setText(recipe.getTitle());
        }

        String imageUrl = recipe.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(recipeDetailImage);
        } else {
            recipeDetailImage.setImageResource(R.drawable.ic_default_recipe_image);
        }

        recipeSummary.setText(formatHtml(recipe.getSummary()));
        recipeIngredients.setText(formatHtml(recipe.getIngredients()));
        recipeInstructions.setText(formatHtml(recipe.getInstructions()));

        if (userNotesLabel != null && recipeUserNotes != null) {
            if (!TextUtils.isEmpty(recipe.getUserNotes())) {
                userNotesLabel.setVisibility(View.VISIBLE);
                recipeUserNotes.setText(formatHtml(recipe.getUserNotes()));
                recipeUserNotes.setVisibility(View.VISIBLE);
            } else {
                userNotesLabel.setVisibility(View.GONE);
                recipeUserNotes.setVisibility(View.GONE);
            }
        }
    }

    private CharSequence formatHtml(String htmlString) {
        if (htmlString == null) return "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(htmlString);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (firebaseDocId != null) {
                    loadRecipeDetailsFromFirestore(firebaseDocId);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "EditRecipeActivity cancelled or no changes.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseDocId != null) {
            loadRecipeDetailsFromFirestore(firebaseDocId);
        }
    }
}