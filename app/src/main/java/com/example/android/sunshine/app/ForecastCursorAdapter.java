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

	private final int VIEW_TYPE_TODAY = 0;
	private final int VIEW_TYPE_FUTURE_DAY = 1;

	public ForecastCursorAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
	}

/*
	*/
/* Prepare the weather high/lows for presentation. *//*

	private String formatHighLows(double high, double low) {
		boolean isMetric = Utility.isMetric(mContext);
		String highLowStr = Utility.formatTemperature(context, high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
		return highLowStr;
	}

	*/
/**
	 * This is ported from FetchWeatherTask --- but now we go straight from the cursor to the string.
	 *//*

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
*/

	/* Remember that these views are reused as needed. */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int itemViewType = getItemViewType(cursor.getPosition());
		int listItemForecast = R.layout.list_item_forecast;
		if (VIEW_TYPE_TODAY == itemViewType) {
			listItemForecast = R.layout.list_item_forecast_today;
		}
		View view = LayoutInflater.from(context).inflate(listItemForecast, parent, false);
		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);
		return view;
	}

	/* This is where we fill-in the views with the contents of the cursor. */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder viewHolder = (ViewHolder) view.getTag();

		/* Read weather icon ID from cursor */
		int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

		viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

		String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
		viewHolder.descriptionView.setText(description);

		boolean isMetric = Utility.isMetric(mContext);
		double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
		String maxTempStr = Utility.formatTemperature(context, maxTemp, isMetric);
		viewHolder.highTempView.setText(maxTempStr);

		double minTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
		String minTempStr = Utility.formatTemperature(context, minTemp, isMetric);
		viewHolder.lowTempView.setText(minTempStr);

		long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
		viewHolder.dateView.setText(Utility.getFriendlyDayString(context, date));
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/* cache of the children views for a forecast view */
	public static class ViewHolder {
		public final ImageView iconView;
		public final TextView dateView;
		public final TextView descriptionView;
		public final TextView highTempView;
		public final TextView lowTempView;

		public ViewHolder(View view) {
			iconView = (ImageView)view.findViewById(R.id.list_item_icon);
			dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
			descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
			highTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
			lowTempView = (TextView)view.findViewById(R.id.list_item_low_textview);
		}
	}
}