package bignerdranch.socialmediahistory;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterApiException;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TweetsActivity extends ActionBarActivity {

    private LinearLayout mLinearLayout;
    private Calendar mCalendar;
    public Calendar mDateWanted;
    public static final String USERNAME = "";
    private Boolean tweetsFound = false;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mLoadingDialog;
    private static final String FILENAME = "tweets.json";
    private JSONArray mArray;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    private static final String KEY_INDEX = "index";
    private StatusesService statusesService;
    private String mUserName;
    private TweetsJSONSerializer mSerializer;
    private long mLastId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets);

        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString(KEY_INDEX);
        }

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

        mSerializer = new TweetsJSONSerializer(getApplicationContext(), FILENAME);

        try {
            mArray = mSerializer.loadTweetIds();
            mProgressDialog.setMessage("Loading tweets...");
            showTweets();
        } catch (IOException e) {
            //Toast.makeText(TweetsActivity.this, "No tweets have been loaded!", Toast.LENGTH_LONG).show();
            loadTweets();
        } catch (JSONException e) {
            Toast.makeText(TweetsActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
        } finally {
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
                            if (((TwitterApiException) e).getErrorCode() == 88) {
                                Toast.makeText(TweetsActivity.this, "Slow down! Twitter limits the rate at which you can access the API. Please try the request again in some time!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(TweetsActivity.this, "Error loading tweets from Twitter: " + e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            if (!tweetsFound)
                Toast.makeText(TweetsActivity.this, R.string.no_tweets_toast, Toast.LENGTH_LONG).show();

        } catch (NullPointerException e) {
            Toast.makeText(TweetsActivity.this, "No tweets have been loaded!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(TweetsActivity.this, "Error showing tweets: " + e, Toast.LENGTH_LONG).show();
        }
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
            case R.id.tweet:
                TweetComposer.Builder builder = new TweetComposer.Builder(this);
                builder.show();
                break;
            case R.id.photo_tweet:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Toast.makeText(TweetsActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }

                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                break;
            case R.id.load_tweets:
                loadTweets();
                break;
            case R.id.delete_loaded_tweets:
                if (mSerializer.mContext.deleteFile(mSerializer.mFilename)) {
                    Toast.makeText(TweetsActivity.this, "All loaded tweets were deleted.", Toast.LENGTH_SHORT).show();
                    mArray = new JSONArray();
                    showTweets();
                }
                break;
            case R.id.logout:
                Twitter.getSessionManager().clearActiveSession();
                Toast.makeText(TweetsActivity.this, R.string.logout_toast, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TweetsActivity.this, SelectNetworkActivity.class));
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(KEY_INDEX, mCurrentPhotoPath);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            TweetComposer.Builder builder = new TweetComposer.Builder(this).text("Check out this image!").image(Uri.fromFile(new File(mCurrentPhotoPath)));
            builder.show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadTweets() {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();
        mUserName = getIntent().getStringExtra(USERNAME);
        mSerializer = new TweetsJSONSerializer(getApplicationContext(), FILENAME);

        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.isIndeterminate();
        mLoadingDialog.setTitle("Loading tweets...");
        mLoadingDialog.show();

        statusesService.userTimeline(null, mUserName, null, 200L, null, true, false, false, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                saveTweets(listResult);
            }

            @Override
            public void failure(TwitterException e) {
                mLoadingDialog.dismiss();
                if (((TwitterApiException) e).getErrorCode() == 88) {
                    Toast.makeText(TweetsActivity.this, "Slow down! Twitter limits the rate at which you can access the API. Please try the request again in some time!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TweetsActivity.this, "Error loading tweets from Twitter: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreTweets() {
        statusesService.userTimeline(null, mUserName, null, 200L, mLastId - 1, true, false, false, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                saveTweets(listResult);
            }

            @Override
            public void failure(TwitterException e) {
                mLoadingDialog.dismiss();
                if (((TwitterApiException) e).getErrorCode() == 88) {
                    Toast.makeText(TweetsActivity.this, "Slow down! Twitter limits the rate at which you can access the API. Please try the request again in some time!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TweetsActivity.this, "Error loading tweets from Twitter: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void saveTweets(Result<List<Tweet>> listResult) {
        try {
            if (listResult.data.size() > 0) {
                for (Tweet tweet : listResult.data)
                    mSerializer.mArray.put(mSerializer.toJSON(tweet));

                mLastId = listResult.data.get(listResult.data.size() - 1).id;
                mLoadingDialog.setMessage(mSerializer.mArray.length() + " tweets have been loaded!");
                mLoadingDialog.show();
                loadMoreTweets();
            } else {
                mSerializer.writeTweetsIds();
                mLoadingDialog.dismiss();
                Toast.makeText(TweetsActivity.this, mSerializer.mArray.length() + " tweets were loaded!", Toast.LENGTH_LONG).show();
                mArray = mSerializer.loadTweetIds();
                showTweets();
            }

        } catch (Exception e) {
            mLoadingDialog.dismiss();
            Toast.makeText(TweetsActivity.this, "Error loading tweets from Twitter: " + e, Toast.LENGTH_SHORT).show();
        }
    }
}