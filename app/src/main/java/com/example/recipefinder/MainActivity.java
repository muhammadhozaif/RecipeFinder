package com.example.recipefinder;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // You can keep a simple layout for MainActivity if needed,
        // or just have it act as a launcher to MyRecipes after login.
        // For simplicity, let's keep the current activity_main layout.
        setContentView(R.layout.activity_main); // Your main layout, even if we don't display a fragment here anymore

        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user logged in, redirect to LoginActivity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Finish MainActivity if no user is logged in
            return;
        }

        // If user is logged in, immediately redirect to MyRecipes.
        // We are effectively bypassing MainActivity as a hub and making MyRecipes the primary hub.
        // The BottomNavigationView setup in MainActivity below will become redundant for initial launch,
        // but it's crucial for the *other* activities to implement their own.
        Intent myRecipesIntent = new Intent(MainActivity.this, MyRecipes.class);
        startActivity(myRecipesIntent);
        finish(); // Finish MainActivity so the user can't press back to it.

        // --- The following BottomNavigationView logic in MainActivity will now be redundant ---
        // --- because MainActivity immediately launches MyRecipes and finishes itself. ---
        // --- This code is effectively never reached for actual navigation from MainActivity. ---
        // --- You will implement similar logic in MyRecipes, EditRecipeActivity, and ProfileActivity. ---

        /* Original BottomNavigationView code (now largely redundant in MainActivity)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_view_recipes); // This selection won't be visible before finish()

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_view_recipes) {
                    startActivity(new Intent(MainActivity.this, MyRecipes.class));
                    return true;
                } else if (itemId == R.id.nav_add_recipe) {
                    Intent intent = new Intent(MainActivity.this, EditRecipeActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
        */
    }

    // This method `loadFragment` is no longer needed as WelcomeFragment is removed.
    // public void loadFragment(Fragment fragment) { ... }
}