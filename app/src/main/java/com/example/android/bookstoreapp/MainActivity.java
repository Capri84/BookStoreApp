package com.example.android.bookstoreapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.android.bookstoreapp.data.BooksContract.BooksEntry;
import com.example.android.bookstoreapp.data.BooksDbHelper;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private BooksDbHelper mDbHelper;
    private TextView dbInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new BooksDbHelper(this);
        dbInfo = findViewById(R.id.db_info);

        insertBook();
        queryData();
    }

    // Insert new book into database.
    private void insertBook() {
        // Create and/or open a database to write into it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_BOOKS_NAME, getString(R.string.book_name));
        values.put(BooksEntry.COLUMN_BOOKS_PRICE, Integer.parseInt(getString(R.string.book_price)));
        values.put(BooksEntry.COLUMN_BOOKS_QUANTITY, Integer.parseInt(getString(R.string.book_quantity)));
        values.put(BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME, getString(R.string.supplier_name));
        values.put(BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE, getString(R.string.supplier_phone));
        // Insert the new row, returning the primary key value of the new row
        db.insert(BooksEntry.TABLE_NAME, null, values);
    }

    private void queryData() {
        // Create and/or open a database to read from it
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = database.query(
                BooksEntry.TABLE_NAME, // The table to query
                null,         // The array of columns to return (pass null to get all)
                null,        // The columns for the WHERE clause
                null,     // The values for the WHERE clause
                null,        // don't group the rows
                null,         // don't filter by row groups
                null         // The sort order
        );

        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(getString(R.string.table_contains) + " " + cursor.getCount() + " " +
                    getString(R.string.book) + "." + "\n\n");
            // Build a header to be displayed in the TextView
            stringBuilder.append(BooksEntry._ID + " | " +
                    BooksEntry.COLUMN_BOOKS_NAME + " | " +
                    BooksEntry.COLUMN_BOOKS_PRICE + " | " +
                    BooksEntry.COLUMN_BOOKS_QUANTITY + " | " +
                    BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME + " | " +
                    BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(BooksEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_BOOKS_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_BOOKS_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_BOOKS_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                stringBuilder.append("\n" + currentId + " | " + currentName + " | " +
                        currentPrice + " | " + currentQuantity + " | " + currentSupplierName + " | " +
                        currentSupplierPhone);
                dbInfo.setText(stringBuilder);
            }
        } finally {
            cursor.close();
            stringBuilder.delete(0, stringBuilder.length());
        }
    }
}
