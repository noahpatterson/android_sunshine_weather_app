package app.com.example.noahpatterson.sunshine.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import app.com.example.noahpatterson.sunshine.MainActivity;
import app.com.example.noahpatterson.sunshine.R;
import app.com.example.noahpatterson.sunshine.Utility;
import app.com.example.noahpatterson.sunshine.data.WeatherContract;
import app.com.example.noahpatterson.sunshine.widget.TodayWidgetProvider;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TodayWidgetIntentService extends IntentService {
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEM = 3;
    private  AppWidgetManager mAppWidgetManager;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        // Get today's data from the ContentProvider
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());
        Cursor data = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        double minTemp = data.getDouble(INDEX_MIN_TEM);
        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp, false);
        String formattedMinTemperature = Utility.formatTemperature(this, minTemp, false);
        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = selectWidgetLayout(appWidgetId);
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
            views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);
            views.setTextViewText(R.id.widget_condition, description);


            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            mAppWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }

    private int getWidgetMinWidth(int appWidgetId) {
        Bundle options = mAppWidgetManager.getAppWidgetOptions(appWidgetId);
        return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
    }

    private int selectWidgetLayout(int appWidgetId) {
        int widgetMinWidth = getWidgetMinWidth(appWidgetId);
        if (widgetMinWidth < 110) {
            return R.layout.widget_today_small;
        } else if (widgetMinWidth >= 110 && widgetMinWidth < 220) {
            return R.layout.widget_today;
        } else {
            return R.layout.widget_today_large;
        }
    }
}