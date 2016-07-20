package com.example.android.booklistingapp;

/**
 * Created by anirudha.joshi on 7/8/2016.
 */
public class Book {

    private String mTitle = "";
    private String mAuthors = "";

    public void Book(String title, String authors, int BookImageResourceID) {
        // Initialize class variables
        mTitle = title;
        mAuthors = authors;
    }

    // Gets the book title
    public String getTitle() {
        return mTitle;
    }

    // Sets the book title
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    // Gets the Books' authors
    public String getAuthors() {
        return mAuthors;
    }

    // Sets the books authors
    public void setAuthors(String mAuthors) {
        this.mAuthors = mAuthors;
    }
}
