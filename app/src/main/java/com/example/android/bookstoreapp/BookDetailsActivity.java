package com.example.android.bookstoreapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BooksContract;

public class BookDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {

    /**
     * Identifier for the books data loader
     */
    private static final int BOOK_DETAILS_LOADER = 0;
    /**
     * Content URI for the existing book
     */
    private Uri mCurrentBookUri;
    // Fields of the Activity
    private EditText mTitleEditText;
    private EditText mAuthorEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mSuppliersNameEditText;
    private EditText mSuppliersPhoneEditText;
    private int inStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Examine the intent that was used to launch this activity.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Initialize a loader to read the book data from the database
        // and display the current values in the Activity
        getLoaderManager().initLoader(BOOK_DETAILS_LOADER, null, this);


        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_book_title);
        mAuthorEditText = findViewById(R.id.edit_book_author);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityTextView = findViewById(R.id.quantity_text_view);
        mSuppliersNameEditText = findViewById(R.id.supplier_name);
        mSuppliersPhoneEditText = findViewById(R.id.supplier_phone);
        ImageButton mDecrementImgBtn = findViewById(R.id.decrement_img_btn);
        ImageButton mIncrementImgBtn = findViewById(R.id.increment_img_btn);
        Button deleteButton = findViewById(R.id.delete_record_btn);
        Button contactSupplierButton = findViewById(R.id.contact_supplier_btn);

        // Block editText fields from editing (because it's not an editor activity).
        mTitleEditText.setEnabled(false);
        mAuthorEditText.setEnabled(false);
        mPriceEditText.setEnabled(false);
        mSuppliersNameEditText.setEnabled(false);
        mSuppliersPhoneEditText.setEnabled(false);
        mTitleEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mAuthorEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mPriceEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mSuppliersNameEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mSuppliersPhoneEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));

        mDecrementImgBtn.setOnClickListener(this);
        mIncrementImgBtn.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        contactSupplierButton.setOnClickListener(this);
    }

    /**
     * Get user input from editor and save book into database.
     */
    private void saveBook() {
        String quantityString = mQuantityTextView.getText().toString().trim();

        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        ContentValues values = new ContentValues();
        values.put(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY, quantity);

        int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, getString(R.string.add_edit_activity_update_book_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.add_edit_activity_update_book_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_add_edit.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_book_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save book to database
                saveBook();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                saveBook();
                Intent editIntent = new Intent(this, AddEditActivity.class);
                editIntent.setData(mCurrentBookUri);
                startActivity(editIntent);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        showUnsavedChangesDialog();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BooksContract.BooksEntry._ID,
                BooksContract.BooksEntry.COLUMN_BOOKS_NAME,
                BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR,
                BooksContract.BooksEntry.COLUMN_BOOKS_PRICE,
                BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY,
                BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME,
                BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_NAME);
            int authorColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR);
            int priceColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY);
            int suppliersNameColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME);
            int suppliersPhoneColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String suppliersName = cursor.getString(suppliersNameColumnIndex);
            String suppliersPhone = cursor.getString(suppliersPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mAuthorEditText.setText(author);
            mPriceEditText.setText(String.valueOf(price));
            inStock = quantity;
            mQuantityTextView.setText(String.valueOf(quantity));
            mSuppliersNameEditText.setText(suppliersName);
            mSuppliersPhoneEditText.setText(suppliersPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mAuthorEditText.setText("");
        mPriceEditText.setText(String.valueOf(0));
        mQuantityTextView.setText(String.valueOf(0));
        mSuppliersNameEditText.setText("");
        mSuppliersPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.add_edit_activity_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.add_edit_activity_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.decrement_img_btn:
                if (inStock > 0) {
                    inStock -= 1;
                    mQuantityTextView.setText(String.valueOf(inStock));
                }
                break;
            case R.id.increment_img_btn:
                inStock += 1;
                mQuantityTextView.setText(String.valueOf(inStock));
                break;
            case R.id.delete_record_btn:
                showDeleteConfirmationDialog();
                break;
            case R.id.contact_supplier_btn:
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                        mSuppliersPhoneEditText.getText().toString().trim(), null));
                if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(phoneIntent);
                }
                break;
        }
    }
}




