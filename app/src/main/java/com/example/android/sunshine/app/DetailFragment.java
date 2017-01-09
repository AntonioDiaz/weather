package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/* A placeholder fragment containing a simple view. */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String LOG_TAG = DetailFragment.class.getName();
	ShareActionProvider shareActionProvider;
	private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
	public static final String DETAIL_URI = "DETAIL_URI";
	private String mForecastStr;

	private static final int DETAIL_LOADER = 0;

	private static final String[] FORECAST_COLUMNS = {
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
			WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
			WeatherContract.WeatherEntry.COLUMN_DEGREES,
			WeatherContract.WeatherEntry.COLUMN_PRESSURE,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
	};

	private static final int COL_WEATHER_ID = 0;
	private static final int COL_WEATHER_DATE = 1;
	private static final int COL_WEATHER_DESC = 2;
	private static final int COL_WEATHER_MAX_TEMP = 3;
	private static final int COL_WEATHER_MIN_TEMP = 4;
	private static final int COL_WEATHER_HUMIDITY = 5;
	private static final int COL_WEATHER_WIND_SPEED = 6;
	private static final int COL_WEATHER_DEGREES = 7;
	private static final int COL_WEATHER_PRESSURE = 8;
	private static final int COL_WEATHER_CONDITION_ID = 9;
	private TextView mTextViewDate;
	private TextView mTextViewHigh;
	private TextView mTextViewLow;
	private TextView mTextViewHumidity;
	private TextView mTextViewWindSpeed;
	private TextView mTextViewPressure;
	private TextView mTestViewForecast;
	private ImageView mImageView;
	private Uri mUri;

	public DetailFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			/* inflate the menu; this adds items to the action bar if it is present. */
		inflater.inflate(R.menu.detail, menu);
			/* retrive the share menu item. */
		MenuItem menuItem = menu.findItem(R.id.menu_item_share);
			/* get the provide and hold onto it to set/change the share intent. */
		shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
		if (mForecastStr != null) {
			shareActionProvider.setShareIntent(createShareForecastIntent());
		} else {
			Log.d(LOG_TAG, "ShareActonProvider is null???");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		if (bundle != null) {
			mUri = getArguments().getParcelable(DETAIL_URI);
		}
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
		mTextViewDate = (TextView) rootView.findViewById(R.id.list_item_date_textview);
		mTextViewHigh = (TextView) rootView.findViewById(R.id.list_item_high_textview);
		mTextViewLow = (TextView) rootView.findViewById(R.id.list_item_low_textview);
		mTextViewHumidity = (TextView) rootView.findViewById(R.id.list_item_humidity_textview);
		mTextViewWindSpeed = (TextView) rootView.findViewById(R.id.list_item_wind_speed_textview);
		mTextViewPressure = (TextView) rootView.findViewById(R.id.list_item_pressure_textview);
		mTestViewForecast = (TextView) rootView.findViewById(R.id.list_item_forecast_textview);
		mImageView = (ImageView) rootView.findViewById(R.id.list_item_icon);
		return rootView;
	}

	private Intent createShareForecastIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		shareIntent.setType("text/mime");
		shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
		return shareIntent;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		getLoaderManager().initLoader(DETAIL_LOADER, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cursorLoader = null;
		if (mUri != null) {
			/* Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed. */
			cursorLoader = new CursorLoader(getActivity(), mUri, FORECAST_COLUMNS, null, null, null);
		}
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.v(LOG_TAG, "In onLoadFinished");
		if (data.moveToFirst()) {
			boolean isMetric = Utility.isMetric(getActivity());

			/* setting icon */
			mImageView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));

			/* setting date */
			String dateString = Utility.getFriendlyDayString(getContext(), data.getLong(COL_WEATHER_DATE));
			mTextViewDate.setText(dateString);

			/* setting max temp */
			String high = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
			mTextViewHigh.setText(high);

			/* setting min temp */
			String low = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
			mTextViewLow.setText(low);

			/* setting humidity */
			float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
			mTextViewHumidity.setText(getString(R.string.format_humidity, humidity));

			/* setting humidity */
			float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
			float degrees = data.getFloat(COL_WEATHER_DEGREES);
			mTextViewWindSpeed.setText(Utility.getFormattedWind(getContext(), windSpeed, degrees));

			/* setting humidity */
			float pressure = data.getFloat(COL_WEATHER_PRESSURE);
			mTextViewPressure.setText(getString(R.string.format_pressure, pressure));

			/* setting forecast */
			String weatherDesc = data.getString(COL_WEATHER_DESC);
			mTestViewForecast.setText(weatherDesc);

			/* If onCreateOptionsMenu has already happened, we need to update the share intent now. */
			if (shareActionProvider != null) {
				shareActionProvider.setShareIntent(createShareForecastIntent());
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public void onLocationChanged(String newLocation) {
		if (mUri != null) {
			long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
			mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
			getLoaderManager().restartLoader(DETAIL_LOADER, null, this);

		}
	}
}