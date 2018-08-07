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
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BooksContract;

import es.dmoral.toasty.Toasty;

public class AddEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {

    // Identifier for the books data loader
    private static final int EXISTING_BOOKS_LOADER = 0;
    // Content URI for the existing book (null if it's a new book)
    private Uri mCurrentBookUri;
    // Fields of the Activity
    private EditText mTitleEditText;
    private EditText mAuthorEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mSuppliersNameEditText;
    private EditText mSuppliersPhoneEditText;
    private ImageButton mDecrementImgBtn;
    private ImageButton mIncrementImgBtn;
    private int inStock;
    //Boolean flag that keeps track of whether the book has been edited (true) or not (false)
    private boolean mBookHasChanged = false;
    private boolean isUserInputCompleted;

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

        // Get intent that was used to launch this activity.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if (mCurrentBookUri == null) {
            // This is a new book, set app bar title to "Add a Book"
            setTitle(getString(R.string.add_edit_activity_title_new_book));
        } else {
            // This is an existing book, set app bar title to "Edit a Book"
            setTitle(getString(R.string.add_edit_activity_title_edit_book));
            // Initialize a loader
            getLoaderManager().initLoader(EXISTING_BOOKS_LOADER, null, this);
        }
        // Initialize views and buttons
        initViews();
        // Set listeners to the fields and buttons
        setListeners();

        String quantityString = mQuantityTextView.getText().toString().trim();
        inStock = Integer.parseInt(quantityString);
    }

    // This method saves the book into the database.
    private void saveBook() {
        // Read from input fields
        String titleString = mTitleEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String suppiersNameString = mSuppliersNameEditText.getText().toString().trim();
        String suppliersPhoneString = mSuppliersPhoneEditText.getText().toString().trim();

        // Check if this is a new book and if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(suppiersNameString) && TextUtils.isEmpty(suppliersPhoneString)) {
            isUserInputCompleted = true;
        } else {
            if (!TextUtils.isEmpty(titleString) && !TextUtils.isEmpty(authorString) &&
                    !TextUtils.isEmpty(priceString) && !TextUtils.isEmpty(quantityString) &&
                    !TextUtils.isEmpty(suppiersNameString) && !TextUtils.isEmpty(suppliersPhoneString)) {
                isUserInputCompleted = true;
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
                    // This is a new book, so insert
                    Uri newUri = getContentResolver().insert(BooksContract.BooksEntry.CONTENT_URI, values);
                    // Show a toast message depending on whether or not the insertion was successful.
                    if (newUri == null) {
                        // There was an error with insertion.
                        Toasty.error(this, getString(R.string.add_edit_activity_insert_book_failed),
                                Toast.LENGTH_SHORT, true).show();
                    } else {
                        // The insertion was successful.
                        Toasty.success(this, getString(R.string.add_edit_activity_insert_book_successful),
                                Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    // This is an existing book, so update
                    int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // There was an error with the update.
                        Toasty.error(this, getString(R.string.add_edit_activity_update_book_failed),
                                Toast.LENGTH_SHORT, true).show();
                    } else {
                        // The update was successful.
                        Toasty.success(this, getString(R.string.add_edit_activity_update_book_successful),
                                Toast.LENGTH_SHORT, true).show();
                    }
                }
            } else {
                isUserInputCompleted = false;
                // Check which fields are not filled with data and warn the user
                if (TextUtils.isEmpty(titleString)) {
                    Toasty.warning(this, getString(R.string.title_not_null),
                            Toast.LENGTH_SHORT, true).show();
                }
                if (TextUtils.isEmpty(authorString)) {
                    Toasty.warning(this, getString(R.string.author_not_null),
                            Toast.LENGTH_SHORT, true).show();
                }
                if (TextUtils.isEmpty(priceString)) {
                    Toasty.warning(this, getString(R.string.price_not_null),
                            Toast.LENGTH_SHORT, true).show();
                }
                if (TextUtils.isEmpty(suppiersNameString)) {
                    Toasty.warning(this, getString(R.string.suppliers_name_not_null),
                            Toast.LENGTH_SHORT, true).show();
                }
                if (TextUtils.isEmpty(suppliersPhoneString)) {
                    Toasty.warning(this, getString(R.string.suppliers_phone_not_null),
                            Toast.LENGTH_SHORT, true).show();
                }
            }
        }
    }

    // This method adds menu items to the app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }

    // This method handles user clicks in the app bar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveBook();
                if (isUserInputCompleted) {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise warn the user.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BooksContract.BooksEntry._ID,
                BooksContract.BooksEntry.COLUMN_BOOKS_NAME,
                BooksContract.BooksEntry.COLUMN_BOOKS_AUTHOR,
                BooksContract.BooksEntry.COLUMN_BOOKS_PRICE,
                BooksContract.BooksEntry.COLUMN_BOOKS_QUANTITY,
                BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_NAME,
                BooksContract.BooksEntry.COLUMN_BOOKS_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, mCurrentBookUri, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Leave early if the cursor is null or there is less than 1 row in the cursor
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

    // Warning about unsaved changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // This method handles user clicks on the buttons of the Activity
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // "-" button
            case R.id.decrement_img_btn:
                if (inStock > 0) {
                    inStock -= 1;
                    mQuantityTextView.setText(String.valueOf(inStock));
                }
                break;
            // "+" button
            case R.id.increment_img_btn:
                inStock += 1;
                mQuantityTextView.setText(String.valueOf(inStock));
                break;
        }
    }

    // Initializing views
    public void initViews() {
        mTitleEditText = findViewById(R.id.edit_book_title);
        mAuthorEditText = findViewById(R.id.edit_book_author);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityTextView = findViewById(R.id.quantity_text_view);
        mSuppliersNameEditText = findViewById(R.id.supplier_name);
        mSuppliersPhoneEditText = findViewById(R.id.supplier_phone);
        mDecrementImgBtn = findViewById(R.id.decrement_img_btn);
        mIncrementImgBtn = findViewById(R.id.increment_img_btn);
    }

    // This method sets listeners to the fields and the buttons
    public void setListeners() {
        mTitleEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSuppliersNameEditText.setOnTouchListener(mTouchListener);
        mSuppliersPhoneEditText.setOnTouchListener(mTouchListener);
        mDecrementImgBtn.setOnClickListener(this);
        mIncrementImgBtn.setOnClickListener(this);
    }
}
