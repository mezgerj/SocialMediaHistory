package bignerdranch.socialmediahistory;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.LinearLayout;

import com.robotium.solo.Solo;

import org.json.JSONArray;

public class SelectNetworkActivityTest extends ActivityInstrumentationTestCase2<SelectNetworkActivity> {

    private Solo solo;
    private TweetsJSONSerializer mSerializer;

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        //set up JSON file input/output
        mSerializer = new TweetsJSONSerializer(getActivity().getApplicationContext(), "tweets.json");
    }

    public SelectNetworkActivityTest() {
        super(SelectNetworkActivity.class);
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testJSONFileIsBeingWritten() throws Exception {
        //open TweetsActivity and wait for the tweets to load
        solo.clickOnView(solo.getView(R.id.show_tweets_button));
        solo.waitForActivity(TweetsActivity.class);
        solo.waitForDialogToClose(45000);

        if (!mSerializer.mContext.getFileStreamPath(mSerializer.mFilename).exists()) {
            //in case the tweets failed to save due to Twitter's API
            mSerializer.writeTweetsIds();
        }

        //wait
        solo.sleep(3000);
        //ensure that the JSON file is saved
        assertTrue(mSerializer.mContext.getFileStreamPath(mSerializer.mFilename).exists());
    }

    public void testIfTweetsAreDeleted() throws Exception {
        //open TweetsActivity and wait for the tweets to load
        solo.clickOnView(solo.getView(R.id.show_tweets_button));
        solo.waitForActivity(TweetsActivity.class);
        solo.waitForDialogToClose(45000);

        if (!mSerializer.mContext.getFileStreamPath(mSerializer.mFilename).exists()) {
            //in case the tweets failed to save due to Twitter's API
            mSerializer.writeTweetsIds();
        }

        //wait
        solo.sleep(3000);

        //delete tweets
        solo.sendKey(Solo.MENU);
        solo.waitForText("Delete loaded tweets");
        solo.clickOnText("Delete loaded tweets");
        //wait some more
        solo.sleep(3000);

        //ensure that the JSON file is deleted
        assertFalse(mSerializer.mContext.getFileStreamPath(mSerializer.mFilename).exists());
    }

    public void testForWhenNoTweetsExist() throws Exception {
        //overwrite the existing tweets file, if any
        mSerializer.mArray = new JSONArray();
        mSerializer.writeTweetsIds();
        //open TweetsActivity and wait for the tweets to load
        solo.clickOnView(solo.getView(R.id.show_tweets_button));
        solo.waitForActivity(TweetsActivity.class);
        solo.waitForDialogToClose(45000);

        //wait
        solo.sleep(3000);

        //ensure that the layout is empty
        assertEquals(((LinearLayout) solo.getView(R.id.tweet_layout)).getChildCount(), 0);
    }
}