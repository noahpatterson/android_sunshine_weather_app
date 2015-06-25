package app.com.example.noahpatterson.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.textView);
        Intent intent = getActivity().getIntent();
        text.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        return rootView;
    }
}
