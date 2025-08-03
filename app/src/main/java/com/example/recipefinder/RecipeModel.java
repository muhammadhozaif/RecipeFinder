package com.example.recipefinder;

public class RecipeModel {
    private String firebaseDocId; // Unique ID from Firestore document
    private int id; // Spoonacular ID (will be 0 or -1 for user-created recipes)
    private String title; // Original Spoonacular title OR user-provided title for custom recipe
    private String imageUrl; // Original Spoonacular image URL OR user-provided URL for custom recipe
    private String customTitle; // User-editable custom title (if different from title)
    private String userNotes; // User-editable notes
    private String summary; // User-editable summary
    private String ingredients; // User-editable ingredients string
    private String instructions; // User-editable instructions string

    // Constructor for loading from Firestore (includes firebaseDocId)
    public RecipeModel(String firebaseDocId, int id, String title, String imageUrl,
                       String customTitle, String userNotes, String summary,
                       String ingredients, String instructions) {
        this.firebaseDocId = firebaseDocId;
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.customTitle = customTitle;
        this.userNotes = userNotes;
        this.summary = summary;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    // Constructor for new recipes (without firebaseDocId initially) - used when creating
    public RecipeModel(int id, String title, String imageUrl, String customTitle,
                       String userNotes, String summary, String ingredients, String instructions) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.customTitle = customTitle;
        this.userNotes = userNotes;
        this.summary = summary;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.firebaseDocId = null; // Will be set after saving to Firestore
    }

    // Default constructor for Firebase deserialization
    public RecipeModel() {
        // No-argument constructor required for Firebase deserialization
    }

    // Getters
    public String getFirebaseDocId() { return firebaseDocId; }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getCustomTitle() { return customTitle; }
    public String getUserNotes() { return userNotes; }
    public String getSummary() { return summary; }
    public String getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }

    // Setters (Firebase needs setters for deserialization AND we need setFirebaseDocId)
    public void setFirebaseDocId(String firebaseDocId) { this.firebaseDocId = firebaseDocId; }
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCustomTitle(String customTitle) { this.customTitle = customTitle; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}