package app.com.example.noahpatterson.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.stetho.Stetho;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import app.com.example.noahpatterson.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private String mLocation;
    private Boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private GoogleCloudMessaging mGcm;

    // id from GCM web console
    public static final String PROJECT_NUMBER = "193030073480";

    public static final String PROPERTY_REG_ID = "registration_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Stetho is a tool created by facebook to view your database in chrome inspect.
        // The code below integrates Stetho into your app. More information here:
        // http://facebook.github.io/stetho/
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        ///////////////

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
            getSupportActionBar().setElevation(0f);
        }
        SunshineSyncAdapter.initializeSyncAdapter(this);

        if (checkPlayService()) {
            mGcm = GoogleCloudMessaging.getInstance(this);
            String regID = getRegistrationId(getApplicationContext());

            if (regID.isEmpty()) {
                registerGCMInBackground(getApplicationContext());
            }

        } else {
            Log.i("main activity", "No valid play service apk. Weather alerts will be disabled");
            storeGCMRegistrationId(getApplicationContext(), null);
        }
    }

    private SharedPreferences getGCMPreferences() {
        // Sunshine persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you. Just make sure
        // that it is private!
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private void storeGCMRegistrationId(Context applicationContext, String regId) {
        int appVersion = getAppVersion(applicationContext);
        SharedPreferences sharedPreferences = getGCMPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        Log.d("main activity", "gcm registration id: " + regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private void registerGCMInBackground(final Context applicationContext) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String msg = "";
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(applicationContext);
                    }
                    String regId = mGcm.register(PROJECT_NUMBER);
                    msg = "Device registered. Registration ID = " + regId;
                    Log.d("main acivity", msg);
                    // persist the ID no need to register again
                    storeGCMRegistrationId(applicationContext, regId);
                } catch (IOException e) {
                    msg = "Error: " + e.getMessage();
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private String getRegistrationId(Context applicationContext) {
        SharedPreferences sharedPreferences = getGCMPreferences();
        String regId = sharedPreferences.getString(PROPERTY_REG_ID, "");
        if (regId.isEmpty()) {
            Log.i("main activity", "GCM Registration not found.");
            return "";
        }

        int registeredVersion = sharedPreferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion    = getAppVersion(applicationContext);
        if (registeredVersion != currentVersion) {
            Log.i("main activity", "app version changed.");
            return "";
        }
        return regId;
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen. WHAT DID YOU DO?!?!
            throw new RuntimeException("Could not get package name: " + e);
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

        if (!checkPlayService()) {

        }
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

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Main Activity", "This device is not supported");
            }
            return false;
        }
        return true;
    }
}
