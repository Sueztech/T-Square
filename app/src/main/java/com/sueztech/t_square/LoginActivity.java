package com.sueztech.t_square;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sueztech.t_square.common.GTLoginUtils;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private GTUserLoginTask mAuthTask = null;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;

	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
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

		Button mSubmitButton = (Button) findViewById(R.id.button_submit);
		mSubmitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View view) {
				attemptLogin();
			}
		});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (missing fields), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin () {

		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		View focusView = null;

		// Store values at the time of the login attempt.
		String username = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();

		// Check for an empty password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
		}

		// Check for an empty username.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
		}

		if (focusView != null) {

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
				mAuthTask = new GTUserLoginTask(username, password);
				mAuthTask.execute();

			}
		}
	}

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
		new AlertDialog.Builder(this).setMessage(resID).setPositiveButton(getString(R.string.button_ok), null).show();
	}

	/**
	 * Represents an asynchronous login task used to authenticate
	 * the user.
	 */
	public class GTUserLoginTask extends AsyncTask<Void, Void, GTLoginUtils.LoginStatus> {

		private final String mUsername;
		private final String mPassword;

		GTUserLoginTask (String username, String password) {
			mUsername = username;
			mPassword = password;
		}

		@Override
		protected GTLoginUtils.LoginStatus doInBackground (Void... params) {
			return GTLoginUtils.doLogin(mUsername, mPassword);
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
					mPasswordView.setError(getString(R.string.error_incorrect_login));
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

