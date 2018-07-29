package com.example.android.bookstoreapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.BooksContract;

/**
 * This adapter knows how to create list items for each row of books data in the {@link Cursor}.
 */
public class BooksCursorAdapter extends CursorAdapter {

    // Constructs a new {@link BooksCursorAdapter}.
    public BooksCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // Makes a new blank list item view. No data is set (or bound) to the view yet.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //This method binds the books data (in the current row pointed to by cursor) to the given
    //list item layout.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView titleTextView = (TextView) view.findViewById(R.id.book_title_tv);
        TextView authorTextView = (TextView) view.findViewById(R.id.book_author_tv);
        TextView priceTextView = (TextView) view.findViewById(R.id.book_price_tv);
        TextView quantityTextView = (TextView) view.findViewById(R.id.in_stock_quantity_tv);

        // Find the columnns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
        int authorColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR);
        int priceColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY);

        // Read the attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookAuthor = cursor.getString(authorColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);

        // Populate fields with extracted properties
        titleTextView.setText(bookTitle);
        authorTextView.setText(bookAuthor);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(bookQuantity);
    }
}

}
