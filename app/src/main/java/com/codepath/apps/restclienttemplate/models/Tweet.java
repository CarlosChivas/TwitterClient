package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String image = "";


    //Empty constructor needed by the parcerler library
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        //JSONObject ejemplo = jsonObject.getJSONObject("entities");
        Log.i("Json", jsonObject.toString());
        try {
            tweet.image = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
            /*try {
                JSONArray ejemplo2 = ejemplo.getJSONArray("media");
                Log.i("Tweet", "Si tiene media...." + ejemplo2);
                try{
                    Log.i("FotodeTweet", "Foto: "+ejemplo2.getJSONObject(0).getString("media_url_https"));
                }catch (JSONException e){
                    Log.e("Tweet", "Algo malo");
                }
            }
            catch (JSONException e){
                Log.e("Tweet", "No tiene media");
            }*/
            Log.i("Losquetienenentities", "Si tiene entites: " + tweet.image);
        }
        catch (JSONException e){
            Log.e("Tweet", "No tiene entities");
        }

        try{
            //JSONObject ejemplo3 = jsonObject.getJSONObject("extended_entities");
            tweet.image = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
            Log.i("extended_entities", "Tambien tiene el extended_entities" + tweet.image);
        }
        catch (JSONException e){
            Log.e("extended_entities", "Este no tiene el extende_entities");
        }

        //if(ejemplo.getJSONArray("media"))

        //Log.d("Tweet", "fromJson: " + jsonObject.getJSONArray("media").toString());

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for(int i=0; i<jsonArray.length(); i++){
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
