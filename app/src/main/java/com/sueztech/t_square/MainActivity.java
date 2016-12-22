package com.sueztech.t_square;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sueztech.t_square.common.GTLoginUtils;
import com.sueztech.t_square.common.T2LoginUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private static final int LOGIN_REQUEST = 1;

	private String mGTLoginToken;
	private String mT2LoginToken1;
	private String mT2LoginToken2;

	private SharedPreferences mPrefs;

	private T2UserLoginTask mAuthTask = null;

	private SwipeRefreshLayout mSwipeRefreshLayout;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}
		});

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh () {
				refreshData();
			}
		});

		mPrefs = getPreferences(MODE_PRIVATE);
		mGTLoginToken = mPrefs.getString(GTLoginUtils.LOGIN_TOKEN, null);
		if (mGTLoginToken == null)
			startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST);
		else {
			mSwipeRefreshLayout.setRefreshing(true);
			mT2LoginToken1 = mPrefs.getString(T2LoginUtils.LOGIN_TOKEN_1, null);
			mT2LoginToken2 = mPrefs.getString(T2LoginUtils.LOGIN_TOKEN_1, null);
			if (mT2LoginToken1 == null | mT2LoginToken2 == null) {
				mAuthTask = new T2UserLoginTask(mGTLoginToken);
				mAuthTask.execute((Void) null);
			} else {
				refreshData();
			}
		}

	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == LOGIN_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				mGTLoginToken = data.getStringExtra(GTLoginUtils.LOGIN_TOKEN);
				mPrefs.edit().putString(GTLoginUtils.LOGIN_TOKEN, mGTLoginToken).apply();
				finish();
				startActivity(getIntent());
			} else {
				finish();
			}
		}
	}

	@Override
	public void onBackPressed () {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings ("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected (@NonNull MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_logout) {
			getPreferences(MODE_PRIVATE).edit().remove(GTLoginUtils.LOGIN_TOKEN).apply();
			startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	public void refreshData () {
		new T2LoginUtils(mT2LoginToken1, mT2LoginToken2).getUserId();
		mSwipeRefreshLayout.setRefreshing(false);
	}

	public class T2UserLoginTask extends AsyncTask<Void, Void, GTLoginUtils.LoginStatus> {

		private final String mLoginToken;

		T2UserLoginTask (String loginToken) {
			mLoginToken = loginToken;
		}

		@Override
		protected GTLoginUtils.LoginStatus doInBackground (Void... params) {

			return new GTLoginUtils().doT2Login(mLoginToken);

		}

		@Override
		protected void onPostExecute (final GTLoginUtils.LoginStatus status) {
			mAuthTask = null;
			Snackbar.make(findViewById(R.id.fab), status.payload, Snackbar.LENGTH_INDEFINITE).show();
			getPreferences(MODE_PRIVATE).edit().putString(T2LoginUtils.LOGIN_TOKEN_1, status.payload).putString(T2LoginUtils.LOGIN_TOKEN_2, status.payload2).apply();
			refreshData();
		}

		@Override
		protected void onCancelled () {
			mAuthTask = null;
		}
	}
}
