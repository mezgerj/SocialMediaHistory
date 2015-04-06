package bignerdranch.socialmediahistory;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class TweetsActivity extends ActionBarActivity {

    private LinearLayout mLinearLayout;
    private Calendar mCalendar;
    public Calendar mDateWanted;
    public static final String USERNAME = "";
    private Boolean tweetsFound = false;
    private ProgressDialog mProgressDialog;
    private static final String FILENAME = "tweets.json";
    private JSONArray mArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets);

        mLinearLayout = (LinearLayout) findViewById(R.id.tweet_layout);
        mCalendar = Calendar.getInstance();
        mDateWanted = Calendar.getInstance();
        String userName = getIntent().getStringExtra(USERNAME);
        getSupportActionBar().setTitle("@" + userName + "'s tweets");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4099FF")));

        mDateWanted.set(2015, 2, 31);
        getSupportActionBar().setSubtitle(DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(mDateWanted.getTime()));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting ready...");
        mProgressDialog.isIndeterminate();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        TweetsJSONSerializer serializer = new TweetsJSONSerializer(getApplicationContext(), FILENAME);
        try {
            mArray = serializer.loadTweetIds();

            mProgressDialog.setMessage("Loading tweets...");
            showTweets();
        } catch (IOException e) {
            Toast.makeText(TweetsActivity.this, "No tweets have been loaded!", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Toast.makeText(TweetsActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
        } finally{
            mProgressDialog.dismiss();
        }
    }

    public void showTweets() {
        resetView();
        try {
            for (int i = 0; i < mArray.length(); i++) {
                mCalendar.setTime(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).parse((String) mArray.getJSONObject(i).get("date")));
                if (mCalendar.get(Calendar.YEAR) == mDateWanted.get(Calendar.YEAR) && mCalendar.get(Calendar.MONTH) == mDateWanted.get(Calendar.MONTH) && mCalendar.get(Calendar.DAY_OF_MONTH) == mDateWanted.get(Calendar.DAY_OF_MONTH)) {
                    tweetsFound = true;
                    TweetUtils.loadTweet((long) mArray.getJSONObject(i).get("id"), new LoadCallback<Tweet>() {
                        @Override
                        public void success(Tweet tweet) {
                            mLinearLayout.addView(new TweetView(TweetsActivity.this, tweet));
                            mLinearLayout.getChildAt(mLinearLayout.getChildCount() - 1).setPadding(0, 0, 0, 10);
                        }

                        @Override
                        public void failure(TwitterException e) {

                        }
                    });
                }
            }
        } catch (Exception e) {
            Toast.makeText(TweetsActivity.this, "Error showing tweets: " + e, Toast.LENGTH_LONG).show();
        }
        if (!tweetsFound)
            Toast.makeText(TweetsActivity.this, R.string.no_tweets_toast, Toast.LENGTH_LONG).show();

        mProgressDialog.dismiss();
    }

    public void resetView() {
        mLinearLayout.removeAllViews();
        tweetsFound = false;
        getSupportActionBar().setSubtitle(DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(mDateWanted.getTime()));
        mProgressDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_tweets, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_date:
                Bundle args = new Bundle();
                args.putSerializable(DatePickerFragment.EXTRA_DATE, mDateWanted);

                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.setArguments(args);
                dialogFragment.show(getFragmentManager(), "datePicker");
                break;
            case R.id.action_prev_day:
                mDateWanted.add(Calendar.DAY_OF_MONTH, -1);
                showTweets();
                break;
            case R.id.action_next_day:
                mDateWanted.add(Calendar.DAY_OF_MONTH, 1);
                showTweets();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
