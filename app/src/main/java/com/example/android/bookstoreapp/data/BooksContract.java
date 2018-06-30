package com.example.android.bookstoreapp.data;

import android.provider.BaseColumns;

public final class BooksContract {

    public static abstract class BooksEntry implements BaseColumns {
        public static final String TABLE_NAME = "books";

        public static final String _ID = "_id";
        public static final String COLUMN_BOOKS_NAME = "product_name";
        public static final String COLUMN_BOOKS_PRICE = "price";
        public static final String COLUMN_BOOKS_QUANTITY = "quantity";
        public static final String COLUMN_BOOKS_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_BOOKS_SUPPLIER_PHONE = "supplier_phone_number";

    }
}
