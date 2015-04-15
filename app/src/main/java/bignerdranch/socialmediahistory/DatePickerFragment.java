package bignerdranch.socialmediahistory;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String EXTRA_DATE = "DATE";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar mCalendar = (Calendar) getArguments().getSerializable(EXTRA_DATE);

        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker v, int year, int month, int date) {
        ((TweetsActivity) getActivity()).mDateWanted.set(year, month, date);
        ((TweetsActivity) getActivity()).showTweets();
    }
}
