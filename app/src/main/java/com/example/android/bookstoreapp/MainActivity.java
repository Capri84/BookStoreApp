package com.example.android.bookstoreapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.BooksContract.BooksEntry;
import com.example.android.bookstoreapp.data.BooksDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BOOKS_LOADER = 0;
    BooksCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open AddEditActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView to populate
        ListView booksListView = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        booksListView.setEmptyView(emptyView);

        // Setup cursor adapter using cursor from last step
        mCursorAdapter = new BooksCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        booksListView.setAdapter(mCursorAdapter);

        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BooksEntry.CONTENT_URI, id);
                intent.setData(currentBookUri);
                startActivity(intent);
            }
        });

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(BOOKS_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all items" menu option
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
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new android.content.CursorLoader(this,   // Parent activity context
                BooksEntry.CONTENT_URI,          // Query the content URI for the current pet
                projection,                      // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BooksEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from books database");
    }
}
