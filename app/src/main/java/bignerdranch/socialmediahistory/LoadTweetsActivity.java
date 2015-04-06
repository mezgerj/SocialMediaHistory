package bignerdranch.socialmediahistory;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;


public class LoadTweetsActivity extends ActionBarActivity {
    private StatusesService statusesService;
    private String mUserName;
    public static final String USERNAME = "";
    private TweetsJSONSerializer mSerializer;
    private static final String FILENAME = "tweets.json";
    private long mLastId;
    private ProgressDialog mProgressDialog;
    private Button mDeleteLoadedTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_tweets);
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();
        mUserName = getIntent().getStringExtra(USERNAME);
        mSerializer = new TweetsJSONSerializer(getApplicationContext(), FILENAME);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading tweets...");
        mProgressDialog.isIndeterminate();
        mProgressDialog.show();

        mDeleteLoadedTweets = (Button) findViewById(R.id.delete_loaded_tweets);
        mDeleteLoadedTweets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSerializer.mContext.deleteFile(mSerializer.mFilename))
                    Toast.makeText(LoadTweetsActivity.this, "All loaded tweets were deleted.", Toast.LENGTH_SHORT).show();
            }
        });

        statusesService.userTimeline(null, mUserName, null, 200L, null, true, false, false, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                saveTweets(listResult);
            }

            @Override
            public void failure(TwitterException e) {

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

            }
        });
    }

    public void saveTweets(Result<List<Tweet>> listResult) {
        try {
            if (listResult.data.size() > 0) {
                for (Tweet tweet : listResult.data)
                    mSerializer.mArray.put(mSerializer.toJSON(tweet));

                mLastId = listResult.data.get(listResult.data.size() - 1).id;
                mProgressDialog.setMessage(mSerializer.mArray.length() + " tweets have been loaded!");
                mProgressDialog.show();
                loadMoreTweets();
            } else {
                mSerializer.writeTweetsIds();
                mProgressDialog.dismiss();
                Toast.makeText(LoadTweetsActivity.this, mSerializer.mArray.length() + " tweets were loaded!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(LoadTweetsActivity.this, "Error saving tweets: " + e, Toast.LENGTH_SHORT).show();
        }
    }

}
