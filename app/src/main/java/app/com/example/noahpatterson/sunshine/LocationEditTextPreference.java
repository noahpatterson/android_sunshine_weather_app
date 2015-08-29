package app.com.example.noahpatterson.sunshine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by noahpatterson on 8/29/15.
 */
public class LocationEditTextPreference extends EditTextPreference {
    private static final int DEFAUT_MIN_LOCATION_LENGTH = 2;
    private int mMinLength;


    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0, 0);

        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAUT_MIN_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button buttonPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (s.length() < mMinLength) {
                        //disable OK button
                        buttonPositive.setEnabled(false);
                    } else {
                        buttonPositive.setEnabled(true);
                    }

                }
            }
        });

    }
}
