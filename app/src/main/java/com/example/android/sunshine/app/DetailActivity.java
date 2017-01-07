/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

import static com.example.android.sunshine.app.R.id.container;

public class DetailActivity extends AppCompatActivity {

	private static final String LOG_TAG = DetailActivity.class.getName();

	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(container, new DetailFragment())
					.commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	// Call to update the share intent
	private void setShareIntent(Intent shareIntent) {
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(shareIntent);
		}
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
		return super.onOptionsItemSelected(item);
	}

	/* A placeholder fragment containing a simple view. */
	public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final String LOG_TAG = DetailFragment.class.getName();
		ShareActionProvider shareActionProvider;
		private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
		private String mForecastStr;

		private static final int DETAIL_LOADER = 0;

		private static final String[] FORECAST_COLUMNS = {
				WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
				WeatherEntry.COLUMN_DATE,
				WeatherEntry.COLUMN_SHORT_DESC,
				WeatherEntry.COLUMN_MAX_TEMP,
				WeatherEntry.COLUMN_MIN_TEMP
		};

		private static final int COL_WEATHER_ID = 0;
		private static final int COL_WEATHER_DATE = 1;
		private static final int COL_WEATHER_DESC = 2;
		private static final int COL_WEATHER_MAX_TEMP = 3;
		private static final int COL_WEATHER_MIN_TEMP = 4;

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
			return inflater.inflate(R.layout.fragment_detail, container, false);
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
			Log.v(LOG_TAG, "In onCreateLoader");
			Intent intent = getActivity().getIntent();
			if (intent == null) {
				return null;
			}
			// Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
			return new CursorLoader(getActivity(), intent.getData(), FORECAST_COLUMNS, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			Log.v(LOG_TAG, "In onLoadFinished");
			if (data.moveToFirst()) {
				String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
				String weatherDesc = data.getString(COL_WEATHER_DESC);
				boolean isMetric = Utility.isMetric(getActivity());
				String high = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
				String low = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
				mForecastStr = String.format("%s - %s - %s/%s", dateString, weatherDesc, high, low);
				TextView detailTextView = (TextView)getView().findViewById(R.id.text_detail);
				detailTextView.setText(mForecastStr);
				/* If onCreateOptionsMenu has already happened, we need to update the share intent now. */
				if (shareActionProvider!=null) {
					shareActionProvider.setShareIntent(createShareForecastIntent());
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) { }
	}
}