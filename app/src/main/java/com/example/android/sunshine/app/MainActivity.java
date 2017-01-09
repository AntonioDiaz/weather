package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback   {

	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	//	private static final String FORECASTFRAGMENT_TAG = "FORECASTFRAGMENT_TAG";
	private static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT_TAG";
	public String mLocation;
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreate.....");
		super.onCreate(savedInstanceState);
		mLocation = Utility.getPreferredLocation(this);
		setContentView(R.layout.activity_main);
		Log.d(LOG_TAG, "onCreate..... " + (findViewById(R.id.weather_detail_container) != null));
		if (findViewById(R.id.weather_detail_container) != null) {
			/* The detailed container view will be present only in large screen layouts (res/layout-ws600dp). */
			mTwoPane = true;
			if (savedInstanceState != null) {
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG);
				fragmentTransaction.commit();
			}
		} else {
			mTwoPane = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		//getMenuInflater().inflate(R.menu.forecastfragment, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_settings) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			mLocation = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
			Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", mLocation).build();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(geoLocation);
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Log.d(LOG_TAG, "Couln't call " + mLocation + "no intent defined.");
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String currentLocation = Utility.getPreferredLocation(this);
		if (mLocation != null && !mLocation.equals(currentLocation)) {
			ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
			if (forecastFragment != null) {
				forecastFragment.onLocationChanged();
			}
			DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
			if (detailFragment!=null) {
				detailFragment.onLocationChanged(currentLocation);
			}
			mLocation = currentLocation;
		}
	}

	@Override
	public void onItemSelected(Uri uri) {
		if (mTwoPane) {
			Bundle bundle = new Bundle();
			bundle.putParcelable(DetailFragment.DETAIL_URI, uri);
			DetailFragment detailFragment = new DetailFragment();
			detailFragment.setArguments(bundle);
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG);
			fragmentTransaction.commit();

		} else {
			Intent intent = new Intent(this, DetailActivity.class);
			intent.setData(uri);
			startActivity(intent);
		}




	}
}
