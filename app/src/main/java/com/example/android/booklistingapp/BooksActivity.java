package com.example.android.booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BooksActivity extends AppCompatActivity {

    // Private variables that will be used in the Async class method
    String mUrlText = "";
    ArrayList<Book> mBookList = null;
    BookAdapter mBookAdapter = null;
    ListView mListView = null;
    final int READ_TIMEOUT = 5000;  // In milliseconds
    final int CONNECTION_TIMEOUT = 15000; // In milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_books);

        setTitle(R.string.books);

        // Get the list view to show the book search results
        mListView = (ListView) findViewById(R.id.bookslist);

        ShowBookListView();

        // Retrieve the passed in search URL
        Bundle b = getIntent().getExtras();
        if (b != null)      // Only execute if the URL was successfully passed in from the parent activity
        {
            mUrlText = b.getString("key");

            // Check to see if a network connection is available
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // We have a network connection - fetch data on a seperate thread
                new DownloadWebpageTask().execute(mUrlText);
            } else {
                // display error
                Toast.makeText(getApplicationContext(), R.string.noconnection, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ShowBookListView() {
        TextView emptyView = (TextView) findViewById(R.id.booklistempty);
        emptyView.setVisibility(View.GONE);

        ListView bookslistView = (ListView) findViewById(R.id.bookslist);
        bookslistView.setVisibility(View.VISIBLE);
    }

    private void ShowEmptyTextview() {
        TextView emptyView = (TextView) findViewById(R.id.booklistempty);
        emptyView.setVisibility(View.VISIBLE);

        ListView bookslistView = (ListView) findViewById(R.id.bookslist);
        bookslistView.setVisibility(View.GONE);
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.d("DEBUG_BOOKAPP",getResources().getString(R.string.invalidurl));
                return getResources().getString(R.string.invalidurl);
            }
        }

        // Take the URL string and create the URL object
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            HttpURLConnection conn = null;

            try {
                // Valid URL
                URL url = new URL(myurl);

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Start the query
                conn.connect();
                int response = conn.getResponseCode();
                switch (response) {     // Validate result of request. On error, log to console
                    case HttpURLConnection.HTTP_OK:
                        Log.d("DEBUG_BOOKAPP", "Server responded");
                        // Get server response
                        is = conn.getInputStream();
                        // Convert the InputStream into a string
                        String contentAsString = readIt(is);
                        return contentAsString;
                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                        Log.d("DEBUG_BOOKAPP", "Server timed out");
                        break;// retry
                    case HttpURLConnection.HTTP_UNAVAILABLE:
                        Log.d("DEBUG_BOOKAPP", "Server unavailable");
                        break;// server is unstable - how would you retry?
                    default:
                        Log.d("DEBUG_BOOKAPP", "Server returned an unknown response code: " + response);
                        break;
                }
                // To keep compiler happy
                return "";
            }
            catch (Exception e) {
                Log.d("DEBUG_BOOKAPP",e.toString());
                return "";
            }
            finally {
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
                if (is != null) {
                    is.close();
                }
            }
        }

        // Convert the input stream to a String
        public String readIt(InputStream stream) throws IOException {

            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            String result = total.toString();
            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                    // We will reach here once the async network task has completed
                    // Create a list to hold the books
                    mBookList = new ArrayList<Book>();

                    // Create a JSON object
                    JSONObject jsonRootObject = new JSONObject(result);

                    int totalItems = jsonRootObject.getInt("totalItems");   // Make sure total items returned by server > 0
                    boolean bookitems = jsonRootObject.isNull("items");     // Extra check - Make sure server returned book items

                    if( totalItems==0 || bookitems)     // There are no bookvolume objects returned by the server
                    {
                        ShowEmptyTextview();    // Show the empty textview message to the user

                        // Create an {@link BookAdapter}, whose data source is a list of {@link Book}s. The
                        // adapter knows how to create list items for each item in the list.
                        mBookAdapter = new BookAdapter(BooksActivity.this, mBookList);

                        // Make the {@link ListView} use the {@link BookAdapter} we created above, so that the
                        // {@link ListView} will display list items for each {@link Book} in the list.
                        mListView.setAdapter(mBookAdapter);

                        return;
                    }


                    //Get the instance of JSONArray that contains JSONObjects
                    JSONArray jsonArray = jsonRootObject.optJSONArray("items");

                    // Get data out of the JSON array and into the BookList
                    for (int i = 0; i < jsonArray.length(); i++) {

                        Book book = new Book();

                        JSONObject item = jsonArray.getJSONObject(i);

                        // Get book title
                        JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                        if (!volumeInfo.isNull("title")) {
                            String title = volumeInfo.getString("title");
                            book.setTitle(title);
                        } else    // Unlikely that a book has no title but use it as a safety check
                        {
                            book.setTitle(getResources().getString(R.string.notitle));
                        }
                        Log.i("BookActivity", "Title: " + book.getTitle());

                        // Get book author(s) if they exist
                        if (!volumeInfo.isNull("authors")) {
                            JSONArray authors = volumeInfo.getJSONArray("authors");
                            String authorsString = authors.getString(0);
                            book.setAuthors(authorsString);
                        } else    // There are books without authors, ex. search for Zombie
                        {
                            book.setAuthors(getResources().getString(R.string.noauthors));
                        }
                        Log.i("BookActivity", "Authors: " + book.getAuthors());

                        // Add the book to the booklist
                        mBookList.add(book);
                    }
                // }
            } catch (JSONException e) {
                Log.d("DEBUG_BOOKAPP","JSON exception reading string - partial or no string returned from server");
                e.printStackTrace();
            }

            if( mBookList.isEmpty()) {  // There was an error/exception returned that prevented the mBookList from populating
                ShowEmptyTextview();    // Show the empty textview message to the user
            }
            else  {
                ShowBookListView();     // mBookList was populated show the user the list
            }

            // Create an {@link BookAdapter}, whose data source is a list of {@link Book}s. The
            // adapter knows how to create list items for each item in the list.
            mBookAdapter = new BookAdapter(BooksActivity.this, mBookList);

            // Make the {@link ListView} use the {@link BookAdapter} we created above, so that the
            // {@link ListView} will display list items for each {@link Book} in the list.
            mListView.setAdapter(mBookAdapter);
        }
    }
}
