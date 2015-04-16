package bignerdranch.socialmediahistory;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.fabric.sdk.android.Fabric;


public class SelectNetworkActivity extends ActionBarActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "zpNAAiSwBk7JuZRm1b7eqdVNP";
    private static final String TWITTER_SECRET = "BVJZSRztZawjnDyBDfZrmwKvok2RMNOaUDpK6jOlYC2IeTDc97";
    private static Context context;
    private static final String TAG = "SocialMediaHistory";
    private Button mShowTweetsButton;

    private TwitterLoginButton mLoginButton;

    public static Context getAppContext() {
        return SelectNetworkActivity.context;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "bignerdranch.socialmediahistory",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        SelectNetworkActivity.context = getApplicationContext();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_select_network);
        Log.d(TAG, "onCreate() called");

        ((TextView) findViewById(R.id.link_credit_icons)).setMovementMethod(LinkMovementMethod.getInstance());

        final TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();
        mLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                mLoginButton.setVisibility(View.GONE);
                mShowTweetsButton.setVisibility(View.VISIBLE);
                Toast.makeText(SelectNetworkActivity.this, R.string.login_toast, Toast.LENGTH_SHORT).show();
                showTweets(result);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(SelectNetworkActivity.this, "Failed to log in!", Toast.LENGTH_SHORT).show();
            }
        });

        mShowTweetsButton = (Button) findViewById(R.id.show_tweets_button);
        mShowTweetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTweets(new Result<>(twitterSession, null));
            }
        });

        if (twitterSession != null) {
            mLoginButton.setVisibility(View.GONE);

            mShowTweetsButton.setVisibility(View.VISIBLE);
            mShowTweetsButton.setText("Show @" + twitterSession.getUserName() + "'s tweets");
        }
    }

    private void showTweets(Result<TwitterSession> result) {
        Toast.makeText(SelectNetworkActivity.this, "Getting ready...", Toast.LENGTH_SHORT).show();
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
