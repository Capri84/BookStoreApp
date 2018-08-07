package com.example.android.bookstoreapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BooksContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.bookstoreapp";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    private BooksContract() {
    }

    public static abstract class BooksEntry implements BaseColumns {

        // The content URI to access the data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // The MIME type of the {@link #CONTENT_URI} for a list of books.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // The MIME type of the {@link #CONTENT_URI} for a single book.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String TABLE_NAME = "books";

        public static final String _ID = "_id";
        public static final String COLUMN_BOOKS_NAME = "product_name";
        public static final String COLUMN_BOOKS_AUTHOR = "author";
        public static final String COLUMN_BOOKS_PRICE = "price";
        public static final String COLUMN_BOOKS_QUANTITY = "quantity";
        public static final String COLUMN_BOOKS_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_BOOKS_SUPPLIER_PHONE = "supplier_phone_number";
    }
}

