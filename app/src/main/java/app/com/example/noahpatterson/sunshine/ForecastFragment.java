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
import android.widget.ListView;

import app.com.example.noahpatterson.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view. A modular container
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
        Log.d("lifecycle", "fragment onStart");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0,null,this);

    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // populate forecast adapater with cursor data
        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, null, null, null, sortOrder);
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

//   // ------Get data from the database using a cusor and provider ---------------

        // ------ make a new ForecastAdapter -------------------
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        //R.layout refers to the actual xml file. R.id refers to the id of an element

        // this finds the root view
        final View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView forecast_list_vew = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        forecast_list_vew.setAdapter(forecastAdapter);

        // ------ cursor adapter doesn't because getItem from a CursorAdapter doesn't return a string--------

        return fragmentView;
    }
}
