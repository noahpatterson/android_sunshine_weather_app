package app.com.example.noahpatterson.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private String mLocation;
    private Boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("lifecycle", "activity onCreate");


        SharedPreferences shardPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mLocation = shardPrefs.getString(getString(R.string.location), getString(R.string.default_location_value));
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("lifecycle", "activity onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("lifecycle", "activity onResume");
        // update the location in our second pane using the fragment manager
        String location = Utility.getPreferredLocation( this );
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("lifecycle", "activity onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("lifecycle", "activity onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", "activity onDestroy");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            Intent settingsIntent = new Intent(this, SettingsActivity.class);
//            startActivity(settingsIntent);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            Bundle args = new Bundle();
//            args.putString("dateUri", dateUri.toString());
            args.putParcelable("dateUri", dateUri);
            detailActivityFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailActivityFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            //            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
//            if (cursor != null) {
//                String locationSetting = cursor.getString(COL_LOCATION_SETTING);
//                Intent intent = new Intent(getActivity(), DetailActivity.class)
//                        .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
//                startActivity(intent);
//            }
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }
}
