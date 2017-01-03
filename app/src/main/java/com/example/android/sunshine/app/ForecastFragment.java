package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final int LOADER_ID = 0;
	private ArrayAdapter<String> forecastAdapter;
	private ForecastAdapter mForecastAdapter;

	public ForecastFragment() {
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		getLoaderManager().initLoader(LOADER_ID, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.forecastfragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_refresh) {
			updateWeather();
			return true;
		} else if (itemId == R.id.action_location) {
			Uri gmmIntentUri = Uri.parse("geo:0,0?q=leganes");
			Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
			if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "ELSE", Toast.LENGTH_SHORT).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateWeather() {
		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(this.getContext());
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String defaultLocation = getString(R.string.pref_location_default);
		String location = sharedPref.getString(getString(R.string.pref_location_key), defaultLocation);
		String defaultUnits = getString(R.string.pref_pref_units_default);
		String units = sharedPref.getString(getString(R.string.pref_units_key), defaultUnits);
		fetchWeatherTask.execute(location, units);
	}

	@Override
	public void onStart() {
		super.onStart();
		updateWeather();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
		listView.setAdapter(mForecastAdapter);
		return rootView;
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String locationSetting = Utility.getPreferredLocation(getActivity());
		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
		return new CursorLoader(getActivity(), WeatherContract.WeatherEntry.CONTENT_URI, null, null, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mForecastAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mForecastAdapter.swapCursor(null);
	}


}
