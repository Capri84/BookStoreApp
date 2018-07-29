package com.example.android.bookstoreapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BooksDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = BooksDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "books.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " +
            BooksContract.BooksEntry.TABLE_NAME + " (" +
            BooksContract.BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BooksContract.BooksEntry.COLUMN_BOOKS_NAME + " TEXT NOT NULL, " +
            BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR + " TEXT NOT NULL, " +
            BooksContract.BooksEntry.COLUMN_BOOKS_PRICE + " INTEGER NOT NULL, " +
            BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
            BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME + " TEXT NOT NULL, " +
            BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE + " TEXT NOT NULL" + ");";

    public BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
