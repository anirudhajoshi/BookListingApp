package com.example.android.booklistingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Get the entered text
        final EditText bookstosearch = (EditText) findViewById(R.id.bookstosearch);
        bookstosearch.setHint(R.string.searchhint);

        // Set a click listener on Button
        Button buttonsearch = (Button) findViewById(R.id.buttonsearch);
        buttonsearch.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Search location is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link BooksActivity}
                Intent booksIntent = new Intent(MainActivity.this, BooksActivity.class);

                // Setup the base URL to the google api REST service
                String url = "https://www.googleapis.com/books/v1/volumes?q=";
                String booksToSearch = bookstosearch.getText().toString();
                if (booksToSearch.equals(""))   // If nothing is entered re-prompt user with hint
                {
                    bookstosearch.setHint(R.string.searchhintonempty);
                } else {
                    // Valid URL - go to next activity to make network connection
                    url = url + booksToSearch;

                    // Pass the modified url to the books activity
                    Bundle b = new Bundle();
                    b.putString("key", url);
                    booksIntent.putExtras(b);

                    // Start the books activity
                    startActivity(booksIntent);
                }
            }
        });
    }
}
