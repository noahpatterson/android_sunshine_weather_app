package app.com.example.noahpatterson.sunshine;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view. A modular container
 */
public class MainActivityFragment extends Fragment {

    private ArrayList<String> data = new ArrayList<String>();

    public MainActivityFragment() {
    }


    public void getWeatherData() {
        // call background task
        TextView textView = (TextView) getActivity().findViewById(R.id.editText_zip_code);
        String zip = textView.getText().toString();
        int zip_code = Integer.parseInt(zip);
        new GetWeatherData().execute(zip_code);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        String[] data = {
//                "Today - Sunny - 83/64",
//                "Tomorrow - Cloudy - 75/64",
//                "Wednesday - Cloudy - 75/64",
//                "Thursday - Cloudy - 75/64",
//                "Friday - Cloudy - 75/64",
//                "Saturday - Cloudy - 75/64",
//                "Sunday - Cloudy - 75/64"
//        };


//        List<String> weekForecast= new ArrayList<String>(Arrays.asList(data));
        //R.layout refers to the actual xml file. R.id refers to the id of an element
        ArrayAdapter<String> forecastArrayAdapter= new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_text_view, data);

        // this finds the root view
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView forecast_list_vew = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        forecast_list_vew.setAdapter(forecastArrayAdapter);


        final EditText edit_txt = (EditText) fragmentView.findViewById(R.id.editText_zip_code);
        final Button getWeatherDataButton = (Button) fragmentView.findViewById(R.id.set_zip_code_button);

        getWeatherDataButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // do something
                getWeatherData();
            }
        });


        edit_txt.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),
                            0);
                    getWeatherDataButton.performClick();
                    return true;
                }
                return false;
            }
        });


        return fragmentView;
    }
                                        // Params, Progress, Result
    private class GetWeatherData extends AsyncTask<Integer, Void, String> {
        protected String doInBackground(Integer... zip_code) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                Log.d("GetWeatherData", "doInBackground ");
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String zipString = Integer.toString(zip_code[0]);
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + zipString + "&mode=json&units=metric&cnt=7");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                Log.d("GetWeatherData", "stream not null ");
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return forecastJsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }



//        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
//        }

        protected void onPostExecute(String jsonWeatherData) {
            try {
                Log.d("GetWeatherData", "postExecute");
                parseJSONWeatherData(jsonWeatherData);
            } catch (JSONException e) {
                System.err.println("Caught JSONException: " + e.getMessage());
            }
        }
    }

    private Double toFahrenheit(double celsiusTemp) {
        return celsiusTemp * 1.8 + 32;
    }

    private void parseJSONWeatherData(String jsonWeatherString) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonWeatherString);
        JSONArray dailyArray = jsonObj.getJSONArray("list");
        final int dailyArrayLength = dailyArray.length();

        //find adapter
        ListView forecast_list_vew = (ListView) getActivity().findViewById(R.id.listview_forecast);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) forecast_list_vew.getAdapter();
        data.clear();
        for (int i = 0;i < dailyArrayLength; i++) {
            JSONObject day = dailyArray.getJSONObject(i);

            String dayString = null;
            //get date

            switch (i){
                case 0:
                    dayString = "Today";
                    break;
                case 1:
                    dayString = "Tomorrow";
                    break;
                default:
                    Long dateTimeSeconds = day.getLong("dt");
                    Date date = new Date((long)dateTimeSeconds*1000);
                    dayString = new SimpleDateFormat("EE").format(date);
                    break;
            }

            //get temps
            JSONObject temps = day.getJSONObject("temp");
            Long maxTemp = Math.round(toFahrenheit(temps.getDouble("max")));
            Long minTemp = Math.round(toFahrenheit(temps.getDouble("min")));

            //get weather type
            JSONArray weatherArray = day.getJSONArray("weather");
            JSONObject dayWeather = weatherArray.getJSONObject(0);
            String dayWeatherType = dayWeather.getString("main");

            String forecast = dayString + " - " + dayWeatherType + " - " + maxTemp + "/" + minTemp;

            data.add(forecast);
            adapter.notifyDataSetChanged();
        }
    }
}
