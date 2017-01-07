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


public class MainActivity extends AppCompatActivity {


	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	private static final String FORECASTFRAGMENT_TAG = "FORECASTFRAGMENT_TAG";
	public String mLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mLocation = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG);
			fragmentTransaction.commit();
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
		if (mLocation!=null && !mLocation.equals(Utility.getPreferredLocation(this))) {
			ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
			if (forecastFragment!=null) {
				forecastFragment.onLocationChanged();
			}
			mLocation = Utility.getPreferredLocation(this);
		}
	}
}
