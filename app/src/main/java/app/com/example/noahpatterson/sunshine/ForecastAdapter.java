package app.com.example.noahpatterson.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {
//    public ForecastAdapter(Context context, Cursor c, int flags) {
//        super(context, c, flags);
//    }
    private Cursor mCursor;
    private Context mContext;

    public ForecastAdapter(Context context) {
        mContext = context;
    }


     public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder

    {

         public TextView mDate;
         public TextView mForecast;
         public TextView mHighTemp;
         public TextView mLowTemp;
         public ImageView mConditionImage;

         public ForecastAdapterViewHolder(View view) {
             super(view);
             mDate      = (TextView)view.findViewById(R.id.list_item_date_textview);
             mForecast  = (TextView)view.findViewById(R.id.list_item_forecast_textview);
             mHighTemp  = (TextView)view.findViewById(R.id.list_item_high_textview);
             mLowTemp   = (TextView)view.findViewById(R.id.list_item_low_textview);
             mConditionImage = (ImageView)view.findViewById(R.id.list_item_icon);
         }
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (parent.findViewById(R.id.weather_detail_container) == null) {
            HashMap<Integer, Integer> viewMap = new HashMap<>(2);
            viewMap.put(0, R.layout.list_item_forecast);
            viewMap.put(1, R.layout.list_item_forecast_today);
            view = LayoutInflater.from(parent.getContext()).inflate(viewMap.get(viewType), parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
        }
        view.setFocusable(true);
        ForecastAdapterViewHolder viewHolder = new ForecastAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);

        viewHolder.mDate.setText(Utility.getFriendlyDayString(mContext, mCursor.getLong(ForecastFragment.COL_WEATHER_DATE)));

        viewHolder.mForecast.setText(mCursor.getString(ForecastFragment.COL_WEATHER_DESC));
        viewHolder.mForecast.setContentDescription(mContext.getString(R.string.a11y_forecast, mCursor.getString(ForecastFragment.COL_WEATHER_DESC)));

        viewHolder.mHighTemp.setText(Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(mContext)));
        viewHolder.mHighTemp.setContentDescription(mContext.getString(R.string.a11y_high_temp, Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(mContext))));

        viewHolder.mLowTemp.setText(Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(mContext)));
        viewHolder.mLowTemp.setContentDescription(mContext.getString(R.string.a11y_low_temp, Utility.formatTemperature(mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(mContext))));

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                .error(Utility.getIconResourceForWeatherCondition(mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                .crossFade()
                .into(viewHolder.mConditionImage);

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) { return 0; }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() { return mCursor; }

    /**
     * Prepare the weather high/lows for presentation.
     */
//    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature(mContext ,high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
//        return highLowStr;
//    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
//    private String convertCursorRowToUXFormat(Cursor cursor) {
//        String highAndLow = formatHighLows(
//                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
//                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));
//
//        return cursor.getString(ForecastFragment.COL_LOCATION_SETTING) + " - " + Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
//                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
//                " - " + highAndLow;
//    }


    private final int VIEW_REGULAR = 0;
    private final int VIEW_TODAY = 1;

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TODAY : VIEW_REGULAR;
    }

//    @Override
//    public int getViewTypeCount() {
//        return 2;
//    }




}