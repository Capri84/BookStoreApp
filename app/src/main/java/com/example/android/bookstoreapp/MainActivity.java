package com.example.android.bookstoreapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.bookstoreapp.data.BooksContract.BooksEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BOOKS_LOADER = 0;
    BooksCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open AddEditActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });

        ListView booksListView = findViewById(R.id.list);
        // Find and set empty view on the ListView
        View emptyView = findViewById(R.id.empty_view);
        booksListView.setEmptyView(emptyView);

        // Setup cursor adapter and attach it to the ListView
        mCursorAdapter = new BooksCursorAdapter(this, null);
        booksListView.setAdapter(mCursorAdapter);

        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, BookDetailsActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BooksEntry.CONTENT_URI, id);
                intent.setData(currentBookUri);
                startActivity(intent);
            }
        });

        // Prepare the loader.
        getLoaderManager().initLoader(BOOKS_LOADER, null, this);
    }

    //Create top-bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_items:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Called when a new Loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BooksEntry._ID,
                BooksEntry.COLUMN_BOOKS_NAME,
                BooksEntry.COLUMN_BOOKS_AUTHOR,
                BooksEntry.COLUMN_BOOKS_PRICE,
                BooksEntry.COLUMN_BOOKS_QUANTITY};
        // Create and return a CursorLoader that will take care of creating a Cursor for the data.
        return new android.content.CursorLoader(this, BooksEntry.CONTENT_URI, projection,
                null, null, null);
    }

    // Called when loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    // Called when loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished() is about to be closed.
        mCursorAdapter.swapCursor(null);
    }

    // Delete all books from the books database
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BooksEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + getText(R.string.rows_deleted).toString());
    }
}
