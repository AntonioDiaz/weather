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

	private static final String LOG_TAG = ForecastCursorAdapter.class.getSimpleName();
	private final int VIEW_TYPE_TODAY = 0;
	private final int VIEW_TYPE_FUTURE_DAY = 1;

	public ForecastCursorAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
	}

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

		/*int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);*/

		/* Read weather icon ID from cursor */
		int weatherConditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
		int itemViewType = getItemViewType(cursor.getPosition());
		int iconId;
		if (VIEW_TYPE_TODAY == itemViewType){
			iconId = Utility.getArtResourceForWeatherCondition(weatherConditionId);
		} else {
			iconId = Utility.getIconResourceForWeatherCondition(weatherConditionId);
		}
		viewHolder.iconView.setImageResource(iconId);

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
		String dateStr = Utility.getFriendlyDayString(context, date);
		if (VIEW_TYPE_TODAY==itemViewType) {
			dateStr += " in " + Utility.getPreferredLocation(context);
		}
		viewHolder.dateView.setText(dateStr);
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