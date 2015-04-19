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
        solo.clickOnText("Show @umangs94's tweets");
        solo.waitForActivity(TweetsActivity.class);
        solo.waitForDialogToClose();
        solo.clickOnActionBarItem(R.id.delete_loaded_tweets);

        TweetsJSONSerializer mSerializer = new TweetsJSONSerializer(getActivity().getApplicationContext(), "tweets.json");
        assertFalse(mSerializer.mContext.getFileStreamPath("tweets.json").exists());
    }

    public void testForWhenNoTweetsExist() throws Exception {

    }

    public void testSaveTweets() throws Exception {

    }
}