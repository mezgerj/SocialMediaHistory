package bignerdranch.socialmediahistory;

import android.content.Context;
import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by umang on 4/1/15.
 */
public class TweetsJSONSerializer {
    public Context mContext;
    public String mFilename;
    private Calendar mCalendar;
    public JSONArray mArray;
    private static final String TAG = "SocialMediaHistory";

    public TweetsJSONSerializer(Context c, String f) {
        //set up JSON file input/output
        mContext = c;
        mFilename = f;
        mCalendar = Calendar.getInstance();
        mArray = new JSONArray();
    }

    public JSONObject toJSON(Tweet tweet) throws JSONException {
        //convert tweet metadata to JSON
        JSONObject json = new JSONObject();
        try {
            mCalendar.setTime(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).parse(tweet.createdAt));
        } catch (ParseException e) {
            Log.d(TAG, tweet.createdAt);
        }
        //save tweet's time in JSONArray
        json.put("date", mCalendar.getTime());
        json.put("id", tweet.id);
        return json;
    }

    public void writeTweetsIds() throws IOException {
        //write JSONArray to file
        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(mArray.toString());
        } finally {
            if (writer != null) writer.close();
        }
    }

    public JSONArray loadTweetIds() throws IOException, JSONException {
        //load JSONArray from file
        BufferedReader reader = null;
        JSONArray array = null;
        try {
            InputStream in = mContext.openFileInput(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
        } finally {
            if (reader != null) reader.close();
        }

        return array;
    }
}
