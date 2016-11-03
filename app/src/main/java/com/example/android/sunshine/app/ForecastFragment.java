package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/** A placeholder fragment containing a simple view. */
public class ForecastFragment extends Fragment {

	private ArrayAdapter<String> forecastAdapter;

	public ForecastFragment() { }

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
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateWeather() {
		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String defaultLocation = getString(R.string.pref_location_default);
		String location = sharedPref.getString(getString(R.string.pref_location_key), defaultLocation );
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
		forecastAdapter = new ArrayAdapter<String>(
				getActivity(),
				R.layout.list_item_forecast,
				R.id.list_item_forecast_textview,
				new ArrayList<String>());
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
		listView.setAdapter(forecastAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String forecast = forecastAdapter.getItem(position);
				Intent intent = new Intent (getActivity(), DetailActivity.class);
				intent.putExtra(Intent.EXTRA_TEXT, forecast);
				startActivity(intent);
			}
		});
		return rootView;
	}

	public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

		final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
		final String QUERY_PARAM = "q";
		final String FORMAT_PARAM = "mode";
		final String UNITS_PARAM = "units";
		final String DAYS_PARAM = "cnt";
		final String APPID_PARAM = "APPID";

		private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

		@Override
		protected String[] doInBackground(String... params) {
			String[] forecastArray = null;
			String format = "json";
			String unit = "metric";
			int numDays = 7;

			// These two need to be declared outside the try/catch
			// so that they can be closed in the finally block.
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;

			// Will contain the raw JSON response as a string.
			String forecastJsonStr = null;
			try {
				// Construct the URL for the OpenWeatherMap query
				// Possible parameters are available at OWM's forecast API page, at
				// http://openweathermap.org/API#forecast
				Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
						.appendQueryParameter(QUERY_PARAM, params[0])
						.appendQueryParameter(FORMAT_PARAM, format)
						.appendQueryParameter(UNITS_PARAM, params[1])
						.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
						.appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
						.build();

				URL url = new URL(builtUri.toString());
				Log.v(LOG_TAG, "Build URL " +  builtUri.toString());
				// Create the request to OpenWeatherMap, and open the connection
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
				InputStream inputStream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				if (inputStream == null) {
					// Nothing to do.
					forecastJsonStr = null;
				}
				reader = new BufferedReader(new InputStreamReader(inputStream));

				String line;
				while ((line = reader.readLine()) != null) {
					// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
					// But it does make debugging a *lot* easier if you print out the completed
					// buffer for debugging.
					buffer.append(line + "\n");
				}
				if (buffer.length() == 0) {
					// Stream was empty.  No point in parsing.
					forecastJsonStr = null;
				}
				forecastJsonStr = buffer.toString();
				forecastArray = JsonUtilities.getWeatherDataFromJson(forecastJsonStr, 7);
			} catch (IOException | JSONException e) {
				Log.e(LOG_TAG, "Error ", e);
				// If the code didn't successfully get the weather data, there's no point in attempting to parse it.
				forecastJsonStr = null;
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						Log.e(LOG_TAG, "Error closing stream", e);
					}
				}
			}
			return forecastArray;
		}

		@Override
		protected void onPostExecute(String[] strings) {
			super.onPostExecute(strings);
			if (strings!=null){
				forecastAdapter.clear();
				forecastAdapter.addAll(strings);
			}
		}
	}
}
