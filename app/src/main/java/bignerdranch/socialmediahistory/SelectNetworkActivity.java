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

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.Facebook;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.facebook.Session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import io.fabric.sdk.android.Fabric;



public class SelectNetworkActivity extends ActionBarActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "zpNAAiSwBk7JuZRm1b7eqdVNP";
    private static final String TWITTER_SECRET = "BVJZSRztZawjnDyBDfZrmwKvok2RMNOaUDpK6jOlYC2IeTDc97";
    private static Context context;
    private static final String TAG = "SocialMediaHistory";
    private LoginButton mFacebookButton;
    private View otherView;
    private UiLifecycleHelper uiHelper;
    private Button mShowTweetsButton;
    private Button mLogoutTwitterButton;
    private TextView mUser;
    private TwitterLoginButton mLoginButton;

    public static Context getAppContext() {
        return SelectNetworkActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_network);
        otherView = (View) findViewById(R.id.other_views);
        otherView.setVisibility(View.GONE);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        boolean result = isLoggedIn();
        Log.d(TAG,"Result is " + result);
        Session session = Session.getActiveSession();
        Request.newMeRequest(session,
                new Request.GraphUserCallback() {

                    // callback after Graph API response with user
                    // object
                    @Override
                    public void onCompleted(GraphUser user,
                                            Response response) {
                        // when user is not null
                        if (user != null) {
                            Log.d(TAG,"User is " + user.getFirstName());

                        }
                    }
                }).executeAsync();

        Bundle params = new Bundle();
        params.putString("message", "This is a test message");
/* make the API call */
        new Request(
                session,
                "/{status-id}",
                params,
                HttpMethod.POST,
                new Request.Callback() {
                    public void onCompleted(Response response) {
            /* handle the result */
                    }
                }
        ).executeAsync();






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

        SelectNetworkActivity.context = getApplicationContext();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));        setContentView(R.layout.activity_select_network);
        Log.d(TAG, "onCreate() called");

        ((TextView) findViewById(R.id.link_credit_icons)).setMovementMethod(LinkMovementMethod.getInstance());

        final int messageResIdFacebook = R.string.facebook_login_toast;
        mUser = (TextView) findViewById(R.id.user_show);
        mFacebookButton = (LoginButton) findViewById(R.id.facebook_button);


        final TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();
        mLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                mLoginButton.setVisibility(View.GONE);
                mLogoutTwitterButton.setVisibility(View.VISIBLE);
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

        mLogoutTwitterButton = (Button) findViewById(R.id.logout_twitter);
        mLogoutTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Twitter.getSessionManager().clearActiveSession();

                mLoginButton.setVisibility(View.VISIBLE);
                mLogoutTwitterButton.setVisibility(View.GONE);
                mShowTweetsButton.setVisibility(View.GONE);
                Toast.makeText(SelectNetworkActivity.this, R.string.logout_toast, Toast.LENGTH_SHORT).show();
            }
        });

        if (twitterSession != null) {
            mLoginButton.setVisibility(View.GONE);

            mShowTweetsButton.setVisibility(View.VISIBLE);
            mShowTweetsButton.setText("Show " + twitterSession.getUserName() + "'s tweets");

            mLogoutTwitterButton.setVisibility(View.VISIBLE);
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void showTweets(Result<TwitterSession> result) {
        Intent i = new Intent(SelectNetworkActivity.this, TweetsActivity.class);
        i.putExtra(TweetsActivity.USERNAME, result.data.getUserName());
        startActivity(i);
    }

    private void onSessionStateChange(Session session, SessionState state,
                                      Exception exception) {
        final TextView name = (TextView) findViewById(R.id.name);
        final TextView gender = (TextView) findViewById(R.id.gender);
        final TextView location = (TextView) findViewById(R.id.location);
        // When Session is successfully opened (User logged-in)
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            // make request to the /me API to get Graph user
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            otherView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginButton.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "OnActivityResult...");
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

    public boolean isLoggedIn() {
        Session session = Session.getActiveSession();
        return (session != null && session.isOpened());
    }



}
