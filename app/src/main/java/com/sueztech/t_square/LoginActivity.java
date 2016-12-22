package com.sueztech.t_square;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sueztech.t_square.common.GTLoginUtils;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	/**
	 * Id to identity READ_CONTACTS permission request.
	 */
	private static final int REQUEST_READ_CONTACTS = 0;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private GTUserLoginTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;

	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setupActionBar();

		// Set up the login form.
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
		populateAutoComplete();

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction (TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View view) {
				attemptLogin();
			}
		});
	}

	private void populateAutoComplete () {
		if (!mayRequestContacts()) {
			return;
		}

		getLoaderManager().initLoader(0, null, this);
	}

	private boolean mayRequestContacts () {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}
		if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
			Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
				@Override
				@TargetApi (Build.VERSION_CODES.M)
				public void onClick (View v) {
					requestPermissions(new String[] {READ_CONTACTS}, REQUEST_READ_CONTACTS);
				}
			});
		} else {
			requestPermissions(new String[] {READ_CONTACTS}, REQUEST_READ_CONTACTS);
		}
		return false;
	}

	/**
	 * Callback received when a permissions request has been completed.
	 */
	@Override
	public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_READ_CONTACTS) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				populateAutoComplete();
			}
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi (Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar () {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin () {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
			//		} else if (!isPasswordValid(password)) {
			//			mPasswordView.setError(getString(R.string.error_invalid_password));
			//			focusView = mPasswordView;
			//			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
			//		} else if (!isEmailValid(email)) {
			//			mEmailView.setError(getString(R.string.error_invalid_email));
			//			focusView = mEmailView;
			//			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {

			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo == null || !networkInfo.isConnected()) {
				showAlert(R.string.error_internet_access);
			} else {

				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				showProgress(true);
				mAuthTask = new GTUserLoginTask(email, password);
				mAuthTask.execute((Void) null);

			}
		}
	}

	//	private boolean isEmailValid (String email) {
	//		return true;
	//	}
	//
	//	private boolean isPasswordValid (String password) {
	//		return true;
	//	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress (final boolean show) {
		if (show)
			mProgressDialog = ProgressDialog.show(this, "Signing in...", "Please wait...");
		else
			mProgressDialog.dismiss();
	}

	private void showAlert (final int resID) {
		new AlertDialog.Builder(this).setMessage(resID).setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick (DialogInterface dialogInterface, int i) {

			}
		}).show();
	}

	@Override
	public Loader<Cursor> onCreateLoader (int i, Bundle bundle) {
		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

				// Select only email addresses.
				ContactsContract.Contacts.Data.MIMETYPE + " = ?", new String[] {ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished (Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> emails = new ArrayList<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}

		addEmailsToAutoComplete(emails);
	}

	@Override
	public void onLoaderReset (Loader<Cursor> cursorLoader) {

	}

	private void addEmailsToAutoComplete (List<String> emailAddressCollection) {
		//Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
		ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

		mEmailView.setAdapter(adapter);
	}


	private interface ProfileQuery {
		String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.IS_PRIMARY,};

		int ADDRESS = 0;
		//		int IS_PRIMARY = 1;
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class GTUserLoginTask extends AsyncTask<Void, Void, GTLoginUtils.LoginStatus> {

		private final String mEmail;
		private final String mPassword;

		GTUserLoginTask (String email, String password) {
			mEmail = email;
			mPassword = password;
		}

		@Override
		protected GTLoginUtils.LoginStatus doInBackground (Void... params) {

			return new GTLoginUtils().doGTLogin(mEmail, mPassword);

		}

		@Override
		protected void onPostExecute (final GTLoginUtils.LoginStatus status) {
			mAuthTask = null;
			showProgress(false);

			if (status.loggedIn) {
				Intent result = new Intent();
				result.putExtra(GTLoginUtils.LOGIN_TOKEN, status.payload);
				setResult(Activity.RESULT_OK, result);
				finish();
			} else {
				if (status.errorMsg == GTLoginUtils.ERR_NOERROR)
					mPasswordView.setError(getString(R.string.error_incorrect_password));
				else if (status.errorMsg == GTLoginUtils.ERR_UNEXPECT)
					showAlert(R.string.error_unexpected);
				else if (status.errorMsg == GTLoginUtils.ERR_GETLOGIN)
					showAlert(R.string.error_fetching_login);
				else if (status.errorMsg == GTLoginUtils.ERR_PUTLOGIN)
					showAlert(R.string.error_submitting_login);
				else if (status.errorMsg == GTLoginUtils.ERR_SIGNINRQ)
					showAlert(R.string.error_internet_access);
				else
					showAlert(R.string.error_unexpected);
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled () {
			mAuthTask = null;
			showProgress(false);
		}
	}
}

