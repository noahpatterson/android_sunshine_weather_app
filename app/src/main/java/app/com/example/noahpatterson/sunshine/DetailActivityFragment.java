package app.com.example.noahpatterson.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import app.com.example.noahpatterson.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mForecastString;
    private ShareActionProvider mShareActionProvider;
    private TextView mtextDate;
    private TextView mtextHigh;
    private TextView mtextLow;
    private TextView mtextHumidity;
    private TextView mtextWind;
    private TextView mtextPressure;
    private TextView mtextForecast;
    private ImageView mConditionIcon;
    private Uri mUri;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] FORECAST_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_DEGREES
        };

        if (mUri != null) {
            return new CursorLoader(getActivity(), mUri, FORECAST_COLUMNS, null, null, null);
        }
        return null;

        }

    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();

        String highFormatted = Utility.formatTemperature(getActivity(), data.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity()));
        String lowFormatted = Utility.formatTemperature(getActivity(), data.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(getActivity()));

        String formattedWeather = data.getString(ForecastFragment.COL_LOCATION_SETTING) + " - " + Utility.formatDate(data.getLong(ForecastFragment.COL_WEATHER_DATE)) + " - " + data.getString(ForecastFragment.COL_WEATHER_DESC) + " - " + highFormatted + "/" + lowFormatted;
        mForecastString = formattedWeather;

        mtextDate.setText(Utility.getFriendlyDayString(getActivity(),data.getLong(ForecastFragment.COL_WEATHER_DATE)));

        mtextHigh.setText(highFormatted);
        mtextHigh.setContentDescription(getString(R.string.a11y_high_temp, highFormatted));

        mtextLow.setText(lowFormatted);
        mtextLow.setContentDescription(getString(R.string.a11y_low_temp, lowFormatted));

        mtextHumidity.setText(getActivity().getString(R.string.format_humidity, data.getFloat(ForecastFragment.COL_WIND)));
        mtextHumidity.setContentDescription(mtextHumidity.getText());

        mtextWind.setText(Utility.getFormattedWind(getActivity(), data.getInt(ForecastFragment.COL_WIND), data.getInt(ForecastFragment.COL_WIND_DEGREES)));
        mtextWind.setContentDescription(mtextWind.getText());

        mtextPressure.setText(getActivity().getString(R.string.format_pressure, data.getFloat(ForecastFragment.COL_PRESSURE)));
        mtextPressure.setContentDescription(mtextPressure.getText());

        mtextForecast.setText(data.getString(ForecastFragment.COL_WEATHER_DESC));
        mtextForecast.setContentDescription(getString(R.string.a11y_forecast, data.getString(ForecastFragment.COL_WEATHER_DESC)));

//        mConditionIcon.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
        Glide.with(getActivity())
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), data.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                .error(Utility.getIconResourceForWeatherCondition(data.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                .crossFade()
                .into(mConditionIcon);
        mConditionIcon.setContentDescription(getString(R.string.a11y_forecast_icon, data.getString(ForecastFragment.COL_WEATHER_DESC)));

//        text.setText(formattedWeather);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        // Get the menu item.
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        if (args != null) {
            mUri = args.getParcelable("dateUri");
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mtextDate = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mtextHigh = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mtextLow = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mtextHumidity = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mtextWind = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mtextPressure = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        mtextForecast = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mConditionIcon = (ImageView) rootView.findViewById(R.id.detail_icon);
        return rootView;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecastString);
        return intent;
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(0, null, this);
        }
    }
}
