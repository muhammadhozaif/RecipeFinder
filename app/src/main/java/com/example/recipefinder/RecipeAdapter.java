package com.example.recipefinder;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<RecipeModel> recipeList;
    private OnItemClickListener listener;
    private boolean isMyRecipesList; // Renamed from isFavoriteList for clarity

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(RecipeModel recipe);
        void onRecipeLongClick(RecipeModel recipe); // For deletion in My Recipes list
    }

    // Constructor - updated parameter name
    public RecipeAdapter(Context context, List<RecipeModel> recipeList, OnItemClickListener listener, boolean isMyRecipesList) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
        this.isMyRecipesList = isMyRecipesList; // Updated
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeModel recipe = recipeList.get(position);

        // Display title: prioritize customTitle for My Recipes list, otherwise use original title
        String displayTitle = recipe.getTitle();
        if (isMyRecipesList && !TextUtils.isEmpty(recipe.getCustomTitle())) {
            displayTitle = recipe.getCustomTitle();
        }
        holder.title.setText(displayTitle);

        // Load image using Picasso into the ImageView
        String imageUrl = recipe.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            // Attempt to load the image from the URL
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image) // Show this while loading
                    .error(R.drawable.ic_error_image)       // Show this if URL is invalid or image fails to load
                    .into(holder.image);
        } else {
            // If no URL or empty URL, set a default local image
            holder.image.setImageResource(R.drawable.ic_default_recipe_image); // Set a generic default
        }

        // Handle the "Remove" button specifically for the My Recipes list
        if (isMyRecipesList) {
            holder.saveButton.setText("Remove");
            // Set a distinct background color for the "Remove" button
            holder.saveButton.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.saveButton.setVisibility(View.VISIBLE); // Ensure it's visible

            // Set the click listener for the Remove button to trigger a long click action
            holder.saveButton.setOnClickListener(v -> listener.onRecipeLongClick(recipe));
        } else {
            // If this adapter is ever used for a list that isn't 'My Recipes', hide the button
            holder.saveButton.setVisibility(View.GONE);
        }

        // Set click listener for the entire item (for viewing details/editing)
        holder.itemView.setOnClickListener(v -> listener.onItemClick(recipe));

        // Set long click listener for the entire item (alternative for deletion in My Recipes)
        if (isMyRecipesList) {
            holder.itemView.setOnLongClickListener(v -> {
                listener.onRecipeLongClick(recipe); // Trigger the long click action
                return true; // Consume the long click event
            });
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // ViewHolder class
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        Button saveButton; // Repurposed for "Remove" in My Recipes list

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recipeImage); // Ensure this ID matches your item_recipe.xml
            title = itemView.findViewById(R.id.recipeTitle);
            saveButton = itemView.findViewById(R.id.btnSave);
        }
    }
}