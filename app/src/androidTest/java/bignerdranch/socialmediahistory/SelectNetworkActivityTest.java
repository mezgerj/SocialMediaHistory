package bignerdranch.socialmediahistory;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

public class SelectNetworkActivityTest extends ActivityInstrumentationTestCase2<SelectNetworkActivity> {

    private Solo solo;

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public SelectNetworkActivityTest() {
        super(SelectNetworkActivity.class);
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testIfTweetsAreDeleted() throws Exception {
        solo.clickOnView(solo.getView(R.id.show_tweets_button));
        solo.waitForActivity(TweetsActivity.class);
        solo.waitForDialogToClose(45000);

        TweetsJSONSerializer mSerializer = new TweetsJSONSerializer(getActivity().getApplicationContext(), "tweets.json");
        if (!mSerializer.mContext.getFileStreamPath(mSerializer.mFilename).exists()) {
            mSerializer.writeTweetsIds();
        }

        solo.sleep(3000);

        solo.sendKey(Solo.MENU);
        solo.waitForText("Delete loaded tweets");
        solo.clickOnText("Delete loaded tweets");
        solo.sleep(3000);

        assertFalse(mSerializer.mContext.getFileStreamPath(mSerializer.mFilename).exists());


    }

    public void testForWhenNoTweetsExist() throws Exception {

    }

    public void testSaveTweets() throws Exception {

    }
}