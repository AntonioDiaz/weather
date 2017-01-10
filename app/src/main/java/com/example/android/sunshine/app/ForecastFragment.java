package com.example.android.sunshine.app;

import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;

/** A placeholder fragment containing a simple view. */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


	private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
	public static final int LOADER_ID = 0;
	private ForecastCursorAdapter mForecastCursorAdapter;
	private Callback mCallback;
	public static final String LIST_POSITION = "list_position";
	private int mPosition = ListView.INVALID_POSITION;
	private ListView mListView;
	private boolean mUseTodayLayout;

	public ForecastFragment() {
		Log.d(LOG_TAG, "forecastFragment constructor");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
		mForecastCursorAdapter = new ForecastCursorAdapter(getActivity(), null, 0);
		mForecastCursorAdapter.setmUseTodayLayout(mUseTodayLayout);
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
		mListView.setAdapter(mForecastCursorAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView adapterView, View view, int position, long l) {
				/* CursorAdapter returns a cursor at the correct position for getItem(), or null if it cannot seek to that position. */
				Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
				if (cursor != null) {
					String locationSetting = Utility.getPreferredLocation(getActivity());
					Uri uriDate = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE));
					((Callback)getActivity()).onItemSelected(uriDate);
				}
				mPosition = position;
			}
		});
		if (savedInstanceState!=null && savedInstanceState.containsKey(LIST_POSITION)) {
			mPosition = savedInstanceState.getInt(LIST_POSITION);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onActivityCreate");
		getLoaderManager().initLoader(LOADER_ID, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			/* to be sure that the container activity has implemented the callback interface. Otherwise throws an exception. */
			Log.d(LOG_TAG, "before casting");
			mCallback = (Callback) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement OnHeadlineSelectedListener");
		}
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
			String location = Utility.getPreferredLocation(getActivity());
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
	public void onSaveInstanceState(Bundle outState) {
		/* When table rotate, the current position needs to be saved. */
 		/* When no item is selected , mPosition will be set to ListView.INVALID_POSITION, so check for that before storing. */
		if (mPosition != ListView.INVALID_POSITION) {
			outState.putInt(LIST_POSITION, mPosition);
		}
		super.onSaveInstanceState(outState);
	}

	public void onLocationChanged(){
		this.updateWeather();
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String locationSetting = Utility.getPreferredLocation(getActivity());
		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
		return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mForecastCursorAdapter.swapCursor(data);
		if (mPosition != ListView.INVALID_POSITION) {
			mListView.smoothScrollToPosition(mPosition);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mForecastCursorAdapter.swapCursor(null);
	}

	private static final String[] FORECAST_COLUMNS = {
			// In this case the id needs to be fully qualified with a table name, since
			// the content provider joins the location & weather tables in the background
			// (both have an _id column)
			// On the one hand, that's annoying.  On the other, you can search the weather table
			// using the location set by the user, which is only in the Location table.
			// So the convenience is worth it.
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			WeatherContract.LocationEntry.COLUMN_COORD_LAT,
			WeatherContract.LocationEntry.COLUMN_COORD_LONG
	};

	/* These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these must change. */
	static final int COL_WEATHER_ID = 0;
	static final int COL_WEATHER_DATE = 1;
	static final int COL_WEATHER_DESC = 2;
	static final int COL_WEATHER_MAX_TEMP = 3;
	static final int COL_WEATHER_MIN_TEMP = 4;
	static final int COL_LOCATION_SETTING = 5;
	static final int COL_WEATHER_CONDITION_ID = 6;
	static final int COL_COORD_LAT = 7;
	static final int COL_COORD_LONG = 8;

	public void setUseTodayLayout(boolean useTodayLayout) {
		mUseTodayLayout = useTodayLayout;
		if (mForecastCursorAdapter!=null) {
			mForecastCursorAdapter.setmUseTodayLayout(mUseTodayLayout);
		}
	}

	/**
	 * A callback interface that all activities containing this fragment must implement.
	 * This mechanism allows activities to be notified of item selections.
	 */
	public interface Callback {
		/* DetailFragmentCallback for when an item has been selected. */
		public void onItemSelected (Uri uri);
	}

}
