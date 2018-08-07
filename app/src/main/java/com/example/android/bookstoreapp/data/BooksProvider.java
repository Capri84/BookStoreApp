package com.example.android.bookstoreapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class BooksProvider extends ContentProvider {

    public static final String LOG_TAG = BooksProvider.class.getSimpleName();

    // URI matcher code for the content URI for the books table
    private static final int BOOKS = 100;

    // URI matcher code for the content URI for a single book in the books table
    private static final int BOOK_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    // Database helper object
    private BooksDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BooksDbHelper(getContext());
        return true;
    }

    // Perform the query for the given URI.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BooksContract.BooksEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BooksContract.BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BooksContract.BooksEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification uri on the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert a book into the database with the given content values. Perform validation checks.
    private Uri insertBook(Uri uri, ContentValues values) {
        String title = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
        if (title == null) {
            throw new IllegalArgumentException("The title of the book is required");
        }

        String author = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR);
        if (author == null) {
            throw new IllegalArgumentException("The author of the book is required");
        }

        Integer price = values.getAsInteger(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("The price is invalid");
        }

        Integer quantity = values.getAsInteger(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("The quantity is invalid");
        }

        String supplierName = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier's name is required");
        }

        String supplierPhone = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Supplier's phone number is required");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new book with the given values
        long id = database.insert(BooksContract.BooksEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    // Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = BooksContract.BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Update books in the database. Return the number of rows that were successfully updated.
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(BooksContract.BooksEntry.COLUMN_BOOKS_NAME)) {
            String title = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
            if (title == null) {
                throw new IllegalArgumentException("The title of the book is required");
            }
        }

        if (values.containsKey(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR)) {
            String author = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR);
            if (author == null) {
                throw new IllegalArgumentException("The author of the book is required");
            }
        }

        // Check that the price value is valid.
        if (values.containsKey(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE)) {
            Integer price = values.getAsInteger(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("The price is invalid");
            }
        }

        // Check that the quantity value is valid.
        if (values.containsKey(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY)) {
            Integer quantity = values.getAsInteger(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("The quantity is invalid");
            }
        }

        if (values.containsKey(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier's name is required");
            }
        }

        if (values.containsKey(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Supplier's phone number is required");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BooksContract.BooksEntry.TABLE_NAME, values, selection,
                selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    // Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BooksContract.BooksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BooksContract.BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BooksContract.BooksEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    // Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BooksContract.BooksEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BooksContract.BooksEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
