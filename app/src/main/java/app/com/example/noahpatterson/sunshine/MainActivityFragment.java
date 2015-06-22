package app.com.example.noahpatterson.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view. A modular container
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] data = {
                "Today - Sunny - 83/64",
                "Tomorrow - Cloudy - 75/64",
                "Wednesday - Cloudy - 75/64",
                "Thursday - Cloudy - 75/64",
                "Friday - Cloudy - 75/64",
                "Saturday - Cloudy - 75/64",
                "Sunday - Cloudy - 75/64"
        };

        List<String> weekForecast= new ArrayList<String>(Arrays.asList(data));
        //R.layout refers to the actual xml file. R.id refers to the id of an element
        ArrayAdapter<String> forecastArrayAdapter= new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_text_view, weekForecast);

        // this finds the root view
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView forecast_list_vew = (ListView) fragmentView.findViewById(R.id.listview_forecast);
        forecast_list_vew.setAdapter(forecastArrayAdapter);

        return fragmentView;
    }
                                                // Params, Progress, Result


}
