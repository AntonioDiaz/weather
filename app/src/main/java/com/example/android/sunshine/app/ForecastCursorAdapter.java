package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastCursorAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastCursorAdapter extends CursorAdapter {

	public ForecastCursorAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
	}

	/* Prepare the weather high/lows for presentation. */
	private String formatHighLows(double high, double low) {
		boolean isMetric = Utility.isMetric(mContext);
		String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
		return highLowStr;
	}

	/** This is ported from FetchWeatherTask --- but now we go straight from the cursor to the string. */
	private String convertCursorRowToUXFormat(Cursor cursor) {
		// get row indices for our cursor
		double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
		double minTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
		String highAndLow = formatHighLows(maxTemp, minTemp);
		String uxFormat = Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
		uxFormat += " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC);
		uxFormat += " - " + highAndLow;
		return uxFormat;
	}

	/* Remember that these views are reused as needed. */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
	}

	/* This is where we fill-in the views with the contents of the cursor. */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		/* Read weather icon ID from cursor */
		int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

		ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
		iconView.setImageResource(R.drawable.ic_launcher);

		TextView textViewDescription = (TextView) view.findViewById(R.id.list_item_forecast_textview);
		String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
		textViewDescription.setText(description);

		boolean isMetric = Utility.isMetric(mContext);
		TextView textViewMaxTemp = (TextView) view.findViewById(R.id.list_item_high_textview);
		double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
		String maxTempStr = Utility.formatTemperature(maxTemp, isMetric);
		textViewMaxTemp.setText(maxTempStr);

		TextView textViewMinTemp = (TextView) view.findViewById(R.id.list_item_low_textview);
		double minTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
		String minTempStr = Utility.formatTemperature(minTemp, isMetric);
		textViewMinTemp.setText(minTempStr);

		TextView textViewDate = (TextView) view.findViewById(R.id.list_item_date_textview);
		long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
		textViewDate.setText(Utility.getFriendlyDayString(context, date));

	}
}