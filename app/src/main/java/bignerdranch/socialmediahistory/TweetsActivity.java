package bignerdranch.socialmediahistory;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class TweetsActivity extends ActionBarActivity {

    private LinearLayout mLinearLayout;
    private Calendar mCalendar;
    public Calendar mDateWanted;
    public static final String USERNAME = "";
    private static final String TAG = "SocialMediaHistory";
    private long mLastId,mFirstId;
    private String mUserName;
    private StatusesService statusesService;
    private Toast mToast;
    private Boolean tweetsFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets);

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();

        mLinearLayout = (LinearLayout) findViewById(R.id.tweet_layout);
        mCalendar = Calendar.getInstance();
        mDateWanted = Calendar.getInstance();
        mUserName = getIntent().getStringExtra(USERNAME);
        getSupportActionBar().setTitle("@" + mUserName + "'s tweets");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4099FF")));

        mDateWanted.set(2015, 2, 9);
        getSupportActionBar().setSubtitle(DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(mDateWanted.getTime()));
        loadTweetsFirstTime();
    }

    public void loadTweetsFirstTime() {
        //First time checking for tweets - max_id = null
        statusesService.userTimeline(null, mUserName, null, 200L, null, true, false, false, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                goThroughTweets(listResult);
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }

    private void loadTweetsAfterFirstTime() {
        statusesService.userTimeline(null, mUserName, null, 200L, mLastId - 1, true, false, false, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                goThroughTweets(listResult);
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }

    private void goThroughTweets(Result<List<Tweet>> listResult) {
        if (listResult.data.size() > 0) {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(TweetsActivity.this, R.string.loading_toast, Toast.LENGTH_SHORT);
            mToast.show();

            mFirstId = listResult.data.get(0).id;
            mLastId = listResult.data.get(listResult.data.size() - 1).id;

            for (Tweet tweet : listResult.data) {
                try {
                    mCalendar.setTime(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).parse(tweet.createdAt));
                    if (mCalendar.get(Calendar.YEAR) == mDateWanted.get(Calendar.YEAR) && mCalendar.get(Calendar.MONTH) == mDateWanted.get(Calendar.MONTH) && mCalendar.get(Calendar.DAY_OF_MONTH) == mDateWanted.get(Calendar.DAY_OF_MONTH)) {
                        tweetsFound = true;
                        TweetUtils.loadTweet(tweet.id, new LoadCallback<Tweet>() {
                            @Override
                            public void success(Tweet tweet) {
                                mLinearLayout.addView(new TweetView(TweetsActivity.this, tweet));
                                mLinearLayout.getChildAt(mLinearLayout.getChildCount() - 1).setPadding(0, 0, 0, 10);
                            }

                            @Override
                            public void failure(TwitterException e) {

                            }
                        });
                    } else if (mCalendar.getTime().before(mDateWanted.getTime())) {
                        if (mToast != null) mToast.cancel();
                        if (!tweetsFound) {
                            mToast = Toast.makeText(TweetsActivity.this, R.string.no_tweets_toast, Toast.LENGTH_LONG);
                            mToast.show();
                        }
                        break;
                    }
                } catch (Exception e) {
                    Log.d(TAG, tweet.createdAt);
                }
            }

            if (mCalendar.getTime().after(mDateWanted.getTime())) {
                loadTweetsAfterFirstTime();
            }
        }
    }

    public void resetView() {
        mLinearLayout.removeAllViews();
        tweetsFound = false;
        getSupportActionBar().setSubtitle(DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(mDateWanted.getTime()));
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
                resetView();
                mLastId=mFirstId+1;
                loadTweetsAfterFirstTime();
                break;
            case R.id.action_next_day:
                mDateWanted.add(Calendar.DAY_OF_MONTH, 1);
                resetView();
                loadTweetsFirstTime();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
