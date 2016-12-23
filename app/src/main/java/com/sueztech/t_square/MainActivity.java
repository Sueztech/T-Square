package com.sueztech.t_square;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sueztech.t_square.common.GTLoginUtils;
import com.sueztech.t_square.common.T2Utils;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private static final String TAG = "T2";

	private static final int LOGIN_REQUEST = 1;

	private String mGTLoginToken;

	private SwipeRefreshLayout mSwipeRefreshLayout;

	private SharedPreferences mPrefs;

	private RefreshTask mRefreshTask = null;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

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
				mRefreshTask = new RefreshTask();
				mRefreshTask.execute((Void) null);
			}
		});

		try {
			File httpCacheDir = new File(getCacheDir(), "http");
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			HttpResponseCache.install(httpCacheDir, httpCacheSize);
		} catch (IOException e) {
			Log.i(TAG, "HTTP response cache installation failed:" + e);
		}

		mPrefs = getPreferences(MODE_PRIVATE);
		mGTLoginToken = mPrefs.getString(GTLoginUtils.LOGIN_TOKEN, null);
		if (mGTLoginToken == null)
			startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST);
		else {
			mSwipeRefreshLayout.setRefreshing(true);
			CookieManager cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);
			try {
				cookieManager.getCookieStore().add(new URI("http://login.gatech.edu"), new HttpCookie(GTLoginUtils.LOGIN_TOKEN, mGTLoginToken));
			} catch (URISyntaxException e) {
				e.printStackTrace();
				Snackbar.make(findViewById(R.id.content_main), R.string.error_unexpected, Snackbar.LENGTH_LONG).show();
			}
			mRefreshTask = new RefreshTask();
			mRefreshTask.execute((Void) null);
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
				mSwipeRefreshLayout.setRefreshing(true);
				mRefreshTask = new RefreshTask();
				mRefreshTask.execute((Void) null);
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
			Snackbar.make(findViewById(R.id.content_main), R.string.not_implemented, Snackbar.LENGTH_LONG).show();
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

	public class RefreshTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground (Void... params) {
			T2Utils.doLogin();
			T2Utils.User.refresh();
			return T2Utils.User.getFirstName();
		}

		@Override
		protected void onPostExecute (final String firstName) {
			mRefreshTask = null;
			Snackbar.make(findViewById(R.id.content_main), getString(R.string.welcome, firstName), Snackbar.LENGTH_LONG).show();
			mSwipeRefreshLayout.setRefreshing(false);
		}

		@Override
		protected void onCancelled () {
			mRefreshTask = null;
		}
	}
}
