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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BooksContract;

import java.text.NumberFormat;

public class AddEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    /**
     * Identifier for the books data loader
     */
    private static final int EXISTING_BOOKS_LOADER = 0;
    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;
    // Fields of the Activity
    private EditText mTitleEditText;
    private EditText mAuthorEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private ImageButton mDecrementImgBtn;
    private ImageButton mIncrementImgBtn;
    private int inStock;
    private EditText mSuppliersNameEditText;
    private EditText mSuppliersPhoneEditText;
    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a book content URI, then we know that we are
        // creating a new book.
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.add_edit_activity_title_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit a Book"
            setTitle(getString(R.string.add_edit_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOKS_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_book_title);
        mAuthorEditText = findViewById(R.id.edit_book_author);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityTextView = findViewById(R.id.quantity_text_view);
        mSuppliersNameEditText = findViewById(R.id.supplier_name);
        mSuppliersPhoneEditText = findViewById(R.id.supplier_phone);
        mDecrementImgBtn = findViewById(R.id.decrement_img_btn);
        mIncrementImgBtn = findViewById(R.id.increment_img_btn);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        //mQuantityTextView.setOnTouchListener(mTouchListener);
        mSuppliersNameEditText.setOnTouchListener(mTouchListener);
        mSuppliersPhoneEditText.setOnTouchListener(mTouchListener);
        mDecrementImgBtn.setOnClickListener(this);
        mIncrementImgBtn.setOnClickListener(this);
    }

    /**
     * Get user input from editor and save book into database.
     */
    private void saveBook() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mTitleEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String suppiersNameString = mSuppliersNameEditText.getText().toString().trim();
        String suppliersPhoneString = mSuppliersPhoneEditText.getText().toString().trim();

        // Check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(suppiersNameString) && TextUtils.isEmpty(suppliersPhoneString)) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        } else {
            if (!TextUtils.isEmpty(titleString) && !TextUtils.isEmpty(authorString) &&
                    !TextUtils.isEmpty(priceString) && !TextUtils.isEmpty(quantityString) &&
                    !TextUtils.isEmpty(suppiersNameString) && !TextUtils.isEmpty(suppliersPhoneString)) {
                // If all fields are modified, create ContentValues.
                ContentValues values = new ContentValues();
                values.put(BooksContract.BooksEntry.COLUMN_BOOKS_NAME, titleString);
                values.put(BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR, authorString);
                values.put(BooksContract.BooksEntry.COLUMN_BOOKS_PRICE, priceString);
                // If the quantity is not provided by the user, don't try to parse the string into an
                // integer value. Use 0 by default.
                int quantity = 0;
                if (!TextUtils.isEmpty(quantityString)) {
                    quantity = Integer.parseInt(quantityString);
                }
                values.put(BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY, quantity);
                values.put(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME, suppiersNameString);
                values.put(BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE, suppliersPhoneString);

                // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
                if (mCurrentBookUri == null) {
                    // This is a NEW book, so insert a new book into the provider,
                    // returning the content URI for the new pet.
                    Uri newUri = getContentResolver().insert(BooksContract.BooksEntry.CONTENT_URI, values);

                    // Show a toast message depending on whether or not the insertion was successful.
                    if (newUri == null) {
                        // If the new content URI is null, then there was an error with insertion.
                        Toast.makeText(this, getString(R.string.add_edit_activity_insert_book_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the insertion was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.add_edit_activity_insert_book_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
                    // and pass in the new ContentValues. Pass in null for the selection and selection args
                    // because mCurrentBookUri will already identify the correct row in the database that
                    // we want to modify.
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
            } else {
                if (TextUtils.isEmpty(titleString)) {
                    Toast.makeText(this, getString(R.string.title_not_null), Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(authorString)) {
                    Toast.makeText(this, getString(R.string.author_not_null), Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(priceString)) {
                    Toast.makeText(this, getString(R.string.price_not_null), Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(suppiersNameString)) {
                    Toast.makeText(this, getString(R.string.suppliers_name_not_null), Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(suppliersPhoneString)) {
                    Toast.makeText(this, getString(R.string.suppliers_phone_not_null), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_add_edit.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
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
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
            mPriceEditText.setText(Integer.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
            mSuppliersNameEditText.setText(suppliersName);
            mSuppliersPhoneEditText.setText(suppliersPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mAuthorEditText.setText("");
        mPriceEditText.setText(0);
        mQuantityTextView.setText(0);
        mSuppliersNameEditText.setText("");
        mSuppliersPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
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
        // for the postivie and negative buttons on the dialog.
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
        }
    }
}
