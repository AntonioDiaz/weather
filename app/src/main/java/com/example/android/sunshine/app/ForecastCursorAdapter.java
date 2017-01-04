package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastCursorAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastCursorAdapter extends CursorAdapter {

	public ForecastCursorAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
	}

	/** Prepare the weather high/lows for presentation. */
	private String formatHighLows(double high, double low) {
		boolean isMetric = Utility.isMetric(mContext);
		String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
		return highLowStr;
	}

	/** This is ported from FetchWeatherTask --- but now we go straight from the cursor to the string.*/
	private String convertCursorRowToUXFormat(Cursor cursor) {
		// get row indices for our cursor
		int idxMaxTemp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
		int idxMinTemp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
		int idxDate = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
		int idxShortDesc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
		String highAndLow = formatHighLows(	cursor.getDouble(idxMaxTemp), cursor.getDouble(idxMinTemp));
		String uxFormat = Utility.formatDate(cursor.getLong(idxDate));
		uxFormat += " - " + cursor.getString(idxShortDesc);
		uxFormat +=	" - " + highAndLow;
		return uxFormat;
	}

	/** Remember that these views are reused as needed. */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
	}

	/** This is where we fill-in the views with the contents of the cursor. */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// our view is pretty simple here --- just a text view
		// we'll keep the UI functional with a simple (and slow!) binding.
		TextView tv = (TextView)view;
		tv.setText(convertCursorRowToUXFormat(cursor));
	}
}