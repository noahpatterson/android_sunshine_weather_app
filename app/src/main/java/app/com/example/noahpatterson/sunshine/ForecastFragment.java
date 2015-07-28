package app.com.example.noahpatterson.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import app.com.example.noahpatterson.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view. A modular container
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    private ForecastAdapter forecastAdapter;

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;
    public static final int COL_HUMIDITY = 9;
    public static final int COL_WIND = 10;
    public static final int COL_PRESSURE = 11;
    public static final int COL_WIND_DEGREES = 12;

    public ForecastFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateWeather();
        Log.d("lifecycle", "fragment onStart");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);

    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // populate forecast adapater with cursor data
        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

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

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Log.d("lifecycle", "fragment onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("lifecycle", "fragment onResume");
//        getLoaderManager().restartLoader(0, null, this);


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("lifecycle", "fragment onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("lifecycle", "fragment onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", "fragment onDestroy");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        if (id == R.id.location_map) {
            SharedPreferences shardPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = shardPrefs.getString(getString(R.string.location), getString(R.string.default_location_value));

            Uri formatGeo = Uri.parse("geo:0,0?q=" + location);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(formatGeo);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }


        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // ------ make a new ForecastAdapter -------------------
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        //R.layout refers to the actual xml file. R.id refers to the id of an element

        // this finds the root view
        final View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView forecast_list_vew = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        forecast_list_vew.setAdapter(forecastAdapter);

        // ------ cursor adapter doesn't because getItem from a CursorAdapter doesn't return a string--------
        forecast_list_vew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String locationSetting = Utility.getPreferredLocation(getActivity());
                if (cursor != null) {
                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE));
//                    String locationSetting = cursor.getString(COL_LOCATION_SETTING);
//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
//                    startActivity(intent);
                    ((Callback) getActivity())
                            .onItemSelected(dateUri);
                }
            }
        });
        return fragmentView;
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(0, null, this);
    }
}
