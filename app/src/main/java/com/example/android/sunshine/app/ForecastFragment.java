package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
		} else if (itemId == R.id.action_location) {
			Uri gmmIntentUri = Uri.parse("geo:0,0?q=leganes");
			Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
			if (intent.resolveActivity(getActivity().getPackageManager())!=null) {
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
		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(this.getContext(), forecastAdapter);
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
}
