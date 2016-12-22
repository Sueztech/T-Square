package com.sueztech.t_square;

import android.content.Intent;
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

import com.sueztech.t_square.common.LoginUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private static final int LOGIN_REQUEST = 1;

	private String mLoginToken;

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

		mLoginToken = getPreferences(MODE_PRIVATE).getString(LoginUtils.GT_LOGIN_TOKEN, null);
		if (mLoginToken == null)
			startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST);
		else {
			mSwipeRefreshLayout.setRefreshing(true);
			mAuthTask = new T2UserLoginTask(mLoginToken);
			mAuthTask.execute((Void) null);
		}

	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == LOGIN_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				mLoginToken = data.getStringExtra(LoginUtils.GT_LOGIN_TOKEN);
				getPreferences(MODE_PRIVATE).edit().putString(LoginUtils.GT_LOGIN_TOKEN, mLoginToken).apply();
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

		if (id == R.id.nav_camera) {
			// Handle the camera action
		} else if (id == R.id.nav_gallery) {
			Snackbar.make(findViewById(R.id.fab), "Not implemented yet", Snackbar.LENGTH_LONG).show();
		} else if (id == R.id.nav_slideshow) {
			Snackbar.make(findViewById(R.id.fab), "Not implemented yet", Snackbar.LENGTH_LONG).show();
		} else if (id == R.id.nav_manage) {
			Snackbar.make(findViewById(R.id.fab), "Not implemented yet", Snackbar.LENGTH_LONG).show();
		} else if (id == R.id.nav_share) {
			Snackbar.make(findViewById(R.id.fab), "Not implemented yet", Snackbar.LENGTH_LONG).show();
		} else if (id == R.id.nav_send) {
			Snackbar.make(findViewById(R.id.fab), "Not implemented yet", Snackbar.LENGTH_LONG).show();
		} else if (id == R.id.nav_logout) {
			getPreferences(MODE_PRIVATE).edit().remove(LoginUtils.GT_LOGIN_TOKEN).apply();
			startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	public void refreshData () {
		mSwipeRefreshLayout.setRefreshing(false);
	}

	public class T2UserLoginTask extends AsyncTask<Void, Void, LoginUtils.LoginStatus> {

		private final String mLoginToken;

		T2UserLoginTask (String loginToken) {
			mLoginToken = loginToken;
		}

		@Override
		protected LoginUtils.LoginStatus doInBackground (Void... params) {

			return new LoginUtils().doT2Login(mLoginToken);

		}

		@Override
		protected void onPostExecute (final LoginUtils.LoginStatus status) {
			mAuthTask = null;
			Snackbar.make(findViewById(R.id.fab), status.payload, Snackbar.LENGTH_INDEFINITE).show();
			getPreferences(MODE_PRIVATE).edit().putString(LoginUtils.T2_LOGIN_TOKEN_1, status.payload).putString(LoginUtils.T2_LOGIN_TOKEN_2, status.payload2).apply();
			refreshData();
		}

		@Override
		protected void onCancelled () {
			mAuthTask = null;
		}
	}
}
