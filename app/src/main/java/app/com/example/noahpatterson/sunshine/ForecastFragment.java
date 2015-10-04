package app.com.example.noahpatterson.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import app.com.example.noahpatterson.sunshine.data.WeatherContract;
import app.com.example.noahpatterson.sunshine.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view. A modular container
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, ForecastAdapter.ForecastAdapterViewHolder vh);
    }

    private ForecastAdapter forecastAdapter;
    private int itemSelectedPosition;
//    private ListView fragmentListView;
    private RecyclerView fragmentRecyclerView;

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

    private boolean mHoldForTranistions;

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
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
        if ( mHoldForTranistions ) {
            getActivity().supportPostponeEnterTransition();
        }
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

        // scroll to position if if exists
        if (itemSelectedPosition != ListView.INVALID_POSITION) {
            fragmentRecyclerView.smoothScrollToPosition(itemSelectedPosition);
        }
        updateEmptyView();
        getActivity().supportStartPostponedEnterTransition();
    }

    private void updateEmptyView() {
        // check for data
        if (forecastAdapter.getItemCount() == 0) {
            int message = R.string.no_data;
//            View emptyView = fragmentRecyclerView.getEmptyView();
            TextView emptyTextView = (TextView) getView().findViewById(R.id.empty_view);
            @SunshineSyncAdapter.LocationStatus int location_sync_status = Utility.getLocationSyncStatus(getActivity());
            switch (location_sync_status) {
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                    message = R.string.no_server;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                    message = R.string.bad_url;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                    message = R.string.unknown_location;
                    break;
                default:
                    if (!Utility.isNetworkAvailable(getActivity())) {
                        message = R.string.no_connection;
                    }
            }
            emptyTextView.setText(message);
        }
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.registerOnSharedPreferenceChangeListener(this);
//        getLoaderManager().restartLoader(0, null, this);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("lifecycle", "fragment onPause");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.unregisterOnSharedPreferenceChangeListener(this);
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
        if (fragmentRecyclerView != null) {
            fragmentRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (itemSelectedPosition != RecyclerView.NO_POSITION) {
            outState.putInt("itemSelectedPosition", itemSelectedPosition);
        }
        super.onSaveInstanceState(outState);
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

            Cursor locationLookup = getActivity().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,new String[] { WeatherContract.LocationEntry.COLUMN_COORD_LAT, WeatherContract.LocationEntry.COLUMN_COORD_LONG }, WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ", new String[] { location }, null);

            locationLookup.moveToFirst();

            Uri formatGeo = Uri.parse("geo:" + locationLookup.getLong(0) + "," + locationLookup.getLong(1));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(formatGeo);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }


        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
//        Context context = getActivity();
//
//        String location = Utility.getPreferredLocation(context);
//
//        // create an alarm manager from the system service
//        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//
//        // create an explicit intent to call the services alarmReciever
//        Intent intent = new Intent(context, SunshineService.AlarmReciever.class);
//
//        // add our location to the explicit intent
//        intent.putExtra("location_to_fetch", location);
//
//        // wrap the explicit intent in a pending intent
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        // set the alarm passing in the pending intent. When this alarm fires it will fire the explicit intent
//        alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5 * 1000, alarmIntent);

    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForcastFragment, 0, 0);
        mHoldForTranistions = a.getBoolean(R.styleable.ForcastFragment_sharedElementTransitions, false);
        a.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // read selectedItemPosition if exists, otherwise select first
        if (savedInstanceState != null) {
            int readSelectedItemPosition = savedInstanceState.getInt("itemSelectedPosition", 0);
            itemSelectedPosition = readSelectedItemPosition;
        }



        // ------ make a new ForecastAdapter -------------------


        //R.layout refers to the actual xml file. R.id refers to the id of an element

        // this finds the root view
        final View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

//        final ListView forecast_list_vew = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        View empty_view = fragmentView.findViewById(R.id.empty_view);
        final RecyclerView forecast_recycler_view = (RecyclerView) fragmentView.findViewById(R.id.recyclerView_forecast);
        forecastAdapter = new ForecastAdapter(getContext(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ForecastAdapterViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date);
                ((Callback) getActivity())
                        .onItemSelected(dateUri, vh);
                itemSelectedPosition = vh.getAdapterPosition();
            }
        }, empty_view);



        fragmentRecyclerView = forecast_recycler_view;

        //allow for parallax on supported devices in landscape
        final View parallaxView = fragmentView.findViewById(R.id.parallax_bar);
        if (parallaxView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                fragmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }
        forecast_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        forecast_recycler_view.setAdapter(forecastAdapter);

        return fragmentView;
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(0, null, this);
    }
}
