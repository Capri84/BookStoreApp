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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BooksContract;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class BookDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {


    // Identifier for the books data loader
    private static final int BOOK_DETAILS_LOADER = 0;
    // Content URI for the existing book
    private Uri mCurrentBookUri;
    // Fields of the Activity
    private EditText mTitleEditText;
    private EditText mAuthorEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mSuppliersNameEditText;
    private EditText mSuppliersPhoneEditText;
    private int inStock;
    // Buttons
    private ImageButton mDecrementImgBtn;
    private ImageButton mIncrementImgBtn;
    private FancyButton deleteButton;
    private FancyButton contactSupplierButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Get intent that was used to launch this activity.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        // Initialize a loader
        getLoaderManager().initLoader(BOOK_DETAILS_LOADER, null, this);
        // Initialize views and buttons
        initViews();
        // Block editText fields from editing (because it's not an editor activity).
        blockEtFromEditing();
        // Set onClickListeners to the buttons
        setListeners();
    }


    // This method saves the book into the database.
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
            // There was an error with the update.
            Toasty.error(this, getString(R.string.add_edit_activity_update_book_failed),
                    Toast.LENGTH_SHORT, true).show();
        } else {
            // The update was successful.
            Toasty.success(this, getString(R.string.add_edit_activity_update_book_successful),
                    Toast.LENGTH_SHORT, true).show();
        }
    }

    // This method adds menu to the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_details, menu);
        return true;
    }

    // This method handles user clicks in the app bar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveBook();
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
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        showUnsavedChangesDialog();
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
            // Find the columns of book attributes
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
    private void showUnsavedChangesDialog() {
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
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Prompt the users to confirm that they want to delete this book.
    private void showDeleteConfirmationDialog() {
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
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // This method deletes the book from the database.
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // There was an error with the deletion.
                Toasty.error(this, getString(R.string.add_edit_activity_update_book_failed),
                        Toast.LENGTH_SHORT, true).show();
            } else {
                // Deletion was successful.
                Toasty.success(this, getString(R.string.add_edit_activity_delete_book_successful),
                        Toast.LENGTH_SHORT, true).show();
            }
        }
        // Close the activity
        finish();
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
            // "Delete record" button
            case R.id.delete_record_btn:
                showDeleteConfirmationDialog();
                break;
            // "Contact supplier" button
            case R.id.contact_supplier_btn:
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                        mSuppliersPhoneEditText.getText().toString().trim(), null));
                if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(phoneIntent);
                }
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
        deleteButton = findViewById(R.id.delete_record_btn);
        contactSupplierButton = findViewById(R.id.contact_supplier_btn);
    }

    // This method blocks editText fields from editing
    public void blockEtFromEditing() {
        // Disable fields
        mTitleEditText.setEnabled(false);
        mAuthorEditText.setEnabled(false);
        mPriceEditText.setEnabled(false);
        mSuppliersNameEditText.setEnabled(false);
        mSuppliersPhoneEditText.setEnabled(false);
        // Set text color to grey
        mTitleEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mAuthorEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mPriceEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mSuppliersNameEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
        mSuppliersPhoneEditText.setTextColor(getResources().getColor(R.color.etEnabledTextColor));
    }

    // This method sets onClickListeners to the buttons
    public void setListeners() {
        mDecrementImgBtn.setOnClickListener(this);
        mIncrementImgBtn.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        contactSupplierButton.setOnClickListener(this);
    }
}




