package com.example.android.booklistingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anirudha.joshi on 7/8/2016.
 */
public class BookAdapter extends ArrayAdapter<Book> {
    List<Book> booksList;

    private boolean mError = false;

    static class ViewHolder {
        private TextView bookTitleTextView;
        private TextView bookAuthorsTextView;
    }

    /**
     * Create a new {@link BookAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param books   is the list of {@link Book}s to be displayed.
     */
    public BookAdapter(Context context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Implement the ViewHolder pattern to optimize adpater performance
        ViewHolder holder;
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);

                TextView errorMsg = (TextView) listItemView.findViewById(R.id.errorMsg);

                if(mError) {
                    errorMsg.setVisibility(View.VISIBLE);
                    errorMsg.setText("Could not fetch data");
                    return listItemView;
                }

                errorMsg.setVisibility(View.GONE);
                holder = new ViewHolder();
                holder.bookTitleTextView = (TextView) listItemView.findViewById(R.id.bookTitle);
                holder.bookAuthorsTextView = (TextView) listItemView.findViewById(R.id.bookAuthor);
                listItemView.setTag(holder);
        } else {
            holder = (ViewHolder) listItemView.getTag();
        }

        Book currentLocation = getItem(position);

        holder.bookTitleTextView.setText(currentLocation.getTitle());
        holder.bookAuthorsTextView.setText(currentLocation.getAuthors());

        // Return the whole list item layout (containing 2 TextViews) so that it can be shown in
        // the ListView.
        return listItemView;
    }
}
