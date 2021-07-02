package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimeLineActivity extends AppCompatActivity {

    public static final String TAG = "TimeLineActivity";
    private final int REQUEST_CODE = 20;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    Button logOut_btn;
    private SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;

    //New tweet window
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        client = TwitterApp.getRestClient(this);

        //Find the recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Init the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        //Recycler view setup: layout manager and the adpater
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(manager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                //populateHomeTimeLine(page);
                Log.i("Scroll", "Se agrego contenido");
                //scrollListener.resetState();
                loadMoreData();
            }
        };

        rvTweets.addOnScrollListener(scrollListener);
        populateHomeTimeLine(1);
        logOut_btn = findViewById(R.id.logOut_btn);
        logOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogoutButton();
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Refresh the list
                fetchTimelineAsync(0);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    //Load more data for continue scrolling
    private void loadMoreData() {
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: loadMoreData" + response, throwable);
            }
        }, tweets.get(tweets.size() - 1).id);
    }

    //Function for populate my TimeLine
    private void populateHomeTimeLine(int index){
        client.getHomeTimeLine(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    Log.e(TAG, json.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
                Log.i(TAG, "OnSuccess!!");
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"onFailure" + response, throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the actions bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose){
            //Compose icon has been selected, navigate to the compose activity if we want to create a new activity
            //Intent intent = new Intent(this, ComposeActivity.class);
            //startActivityForResult(intent, REQUEST_CODE);

            //Call a function to start a Custom View
            newTweet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void newTweet(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View infoMovieView = getLayoutInflater().inflate(R.layout.activity_compose, null);

        dialogBuilder.setView(infoMovieView);
        dialog = dialogBuilder.create();
        dialog.show();
        client = TwitterApp.getRestClient(this);

        final EditText etCompose;
        Button btnTweet;
        etCompose = infoMovieView.findViewById(R.id.etCompose);
        btnTweet = infoMovieView.findViewById(R.id.btnTweet);

        //Set click Listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                if(tweetContent.isEmpty()){
                    Toast.makeText(TimeLineActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(tweetContent.length() > 280){
                    Toast.makeText(TimeLineActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                //Make an API call to Twitter to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            tweets.add(0, tweet);
                            //Update the adapter
                            adapter.notifyItemInserted(0);
                            //We go back to the beginning of the rv
                            rvTweets.smoothScrollToPosition(0);
                            //End compose window
                            Toast.makeText(TimeLineActivity.this, "Tweet published", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
            }
        });
    }

    //This function its called when we use a startActivityForResult, but we are using a custom view so it's unnecessary
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        Code here...
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    // TimelineActivity.java
    private void onLogoutButton() {
        // forget who's logged in
        client.clearAccessToken();
        // navigate backwards to Login screen
        finish();
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        client.getHomeTimeLine(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                adapter.clear();
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.addAll(tweets);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.d("DEBUG", "Json exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + throwable.toString());
            }
        });

    }
}