package com.example.android.bookstoreapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.BooksContract;

public class BooksCursorAdapter extends CursorAdapter {

    // Constructor.
    BooksCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // This method makes a new blank list item view.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    //This method binds the books data (in the current row pointed to by cursor) to the given
    //list item layout.
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Find the columnns of book attributes
        int idColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
        int authorColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR);
        int priceColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY);

        // Read the attributes from the Cursor for the current book
        int bookId = cursor.getInt(idColumnIndex);
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookAuthor = cursor.getString(authorColumnIndex);
        int bookPrice = cursor.getInt(priceColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);

        final Uri mCurrentBookUri = ContentUris.withAppendedId(BooksContract.BooksEntry.CONTENT_URI, bookId);

        // Populate fields with extracted properties
        viewHolder.titleTextView.setText(bookTitle);
        viewHolder.authorTextView.setText(bookAuthor);
        String price = String.valueOf(bookPrice) + context.getText(R.string.currency_sign);
        viewHolder.priceTextView.setText(price);
        viewHolder.quantityTextView.setText(String.valueOf(bookQuantity));
        viewHolder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bookQuantity > 0) {
                    int currentQuantity = bookQuantity - 1;
                    viewHolder.quantityTextView.setText(String.valueOf(currentQuantity));
                    ContentValues values = new ContentValues();
                    values.put(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY, currentQuantity);
                    context.getContentResolver().update(mCurrentBookUri, values, null, null);
                }
            }
        });
    }

    // ViewHolder
    public static class ViewHolder {
        TextView titleTextView;
        TextView authorTextView;
        TextView priceTextView;
        TextView inStockTextView;
        TextView quantityTextView;
        ImageButton saleButton;

        ViewHolder(View view) {
            titleTextView = view.findViewById(R.id.book_title_tv);
            authorTextView = view.findViewById(R.id.book_author_tv);
            priceTextView = view.findViewById(R.id.book_price_tv);
            inStockTextView = view.findViewById(R.id.in_stock_tv);
            quantityTextView = view.findViewById(R.id.in_stock_quantity_tv);
            saleButton = view.findViewById(R.id.sale_btn);
        }
    }
}
