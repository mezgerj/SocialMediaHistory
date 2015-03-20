package bignerdranch.socialmediahistory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;


public class SelectNetworkActivity extends ActionBarActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "zpNAAiSwBk7JuZRm1b7eqdVNP";
    private static final String TWITTER_SECRET = "BVJZSRztZawjnDyBDfZrmwKvok2RMNOaUDpK6jOlYC2IeTDc97";

    private static final String TAG = "SocialMediaHistory";
    private Button mFacebookButton;

    private TwitterLoginButton mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_select_network);
        Log.d(TAG, "onCreate() called");

        final int messageResIdFacebook = R.string.facebook_login_toast;

        mFacebookButton = (Button) findViewById(R.id.facebook_button);
        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), messageResIdFacebook, Toast.LENGTH_SHORT).show();
            }
        });

        checkIfLoggedIn();

        mLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                showTweets(result);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(SelectNetworkActivity.this, "Failed to log in!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfLoggedIn() {
        TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();
        if (twitterSession != null) {
            showTweets(new Result<>(twitterSession,null));
        }
    }

    private void showTweets(Result<TwitterSession> result) {
        Toast.makeText(SelectNetworkActivity.this, R.string.login_toast, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(SelectNetworkActivity.this, TweetsActivity.class);
        i.putExtra(TweetsActivity.USERNAME, result.data.getUserName());
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
}
