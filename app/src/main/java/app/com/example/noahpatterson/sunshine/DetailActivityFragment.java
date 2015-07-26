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
import android.widget.TextView;

import app.com.example.noahpatterson.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mForecastString;
    private ShareActionProvider mShareActionProvider;
//    private Uri detailURI;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0,null,this);
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
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        };
        Intent intent = getActivity().getIntent();
        Uri detailURI = intent.getData();
        return new CursorLoader(getActivity(), detailURI,FORECAST_COLUMNS, null, null, null);
    }

    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        TextView text = (TextView) getActivity().findViewById(R.id.textView);
        String highFormatted = Utility.formatTemperature(getActivity(), data.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity()));
        String lowFormatted = Utility.formatTemperature(getActivity(), data.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), Utility.isMetric(getActivity()));

        String formattedWeather = data.getString(ForecastFragment.COL_LOCATION_SETTING) + " - " + Utility.formatDate(data.getLong(ForecastFragment.COL_WEATHER_DATE)) + " - " + data.getString(ForecastFragment.COL_WEATHER_DESC) + " - " + highFormatted + "/" + lowFormatted;
        mForecastString = formattedWeather;
        text.setText(formattedWeather);
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
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.textView);
//        Intent intent = getActivity().getIntent();
//        mForecastString = intent.getDataString();
//        detailURI = intent.getData();
//        text.setText(mForecastString);
        return rootView;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecastString);
        return intent;
    }
}
