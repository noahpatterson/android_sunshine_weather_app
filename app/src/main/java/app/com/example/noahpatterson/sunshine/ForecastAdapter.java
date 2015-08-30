package app.com.example.noahpatterson.sunshine;

        import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.util.HashMap;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

     private class ViewHolder {

         public TextView date;
         public TextView forecast;
         public TextView highTemp;
         public TextView lowTemp;
         public ImageView conditionImage;

         public ViewHolder(View view) {
             date      = (TextView)view.findViewById(R.id.list_item_date_textview);
             forecast  = (TextView)view.findViewById(R.id.list_item_forecast_textview);
             highTemp  = (TextView)view.findViewById(R.id.list_item_high_textview);
             lowTemp   = (TextView)view.findViewById(R.id.list_item_low_textview);
             conditionImage = (ImageView)view.findViewById(R.id.list_item_icon);
         }
    }
    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext ,high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {




        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return cursor.getString(ForecastFragment.COL_LOCATION_SETTING) + " - " + Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

//        if (cursor.isFirst()) {
//            return LayoutInflater.from(context).inflate(R.layout.list_item_forecast_today, parent, false);
//        } else {
        int viewType = getItemViewType(cursor.getPosition());
        HashMap<Integer, Integer> viewMap = new HashMap<>(2);
        viewMap.put(0,R.layout.list_item_forecast);
        viewMap.put(1, R.layout.list_item_forecast_today);
        View view = LayoutInflater.from(context).inflate(viewMap.get(viewType), parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
//        }

    }

    private final int VIEW_REGULAR = 0;
    private final int VIEW_TODAY = 1;

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TODAY : VIEW_REGULAR;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
            This is where we fill-in the views with the contents of the cursor.
         */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

//        TextView tv = (TextView)view;
//        tv.setText(convertCursorRowToUXFormat(cursor));

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.date.setText(Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));

        viewHolder.forecast.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));
        viewHolder.forecast.setContentDescription(context.getString(R.string.a11y_forecast, cursor.getString(ForecastFragment.COL_WEATHER_DESC)));

        viewHolder.highTemp.setText(Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(context)));
        viewHolder.highTemp.setContentDescription(context.getString(R.string.a11y_high_temp, Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(context))));

        viewHolder.lowTemp.setText(Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(context)));
        viewHolder.lowTemp.setContentDescription(context.getString(R.string.a11y_low_temp, Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(context))));

        if (getItemViewType(cursor.getPosition()) == 0 ) {
            viewHolder.conditionImage.setImageResource(Utility.getIconResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
        } else {
            viewHolder.conditionImage.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
        }
    }
}