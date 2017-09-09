package org.mbach.homeautomation.story;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mbach.homeautomation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ImageSearchActivity class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ImageSearchActivity extends AppCompatActivity {

    private static final String TAG = "ImageSearchActivity";
    private static final String TEMPLATE = "https://www.googleapis.com/customsearch/v1";

    private ImageAdapter imageAdapter;
    private RequestQueue requestQueue = null;

    public interface OnItemClickListener {
        void onItemClick(Model item);
    }

    private Model previous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        final RecyclerView resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        resultsRecyclerView.setLayoutManager(linearLayoutManager);
        final FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        imageAdapter = new ImageAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(Model item) {
                if (previous == item && floatingActionButton.getVisibility() == View.VISIBLE) {
                    floatingActionButton.hide();
                } else {
                    floatingActionButton.show();
                }
                previous = item;
            }
        });
        resultsRecyclerView.setAdapter(imageAdapter);

        requestQueue = Volley.newRequestQueue(this);

        Toolbar toolbar = findViewById(R.id.toolbarImageSearch);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String apiKey = getString(R.string.app_api_key);
                String uri = Uri.parse(TEMPLATE)
                        .buildUpon()
                        .appendQueryParameter("q", s)
                        .appendQueryParameter("cx", "013541193078514217105:i3rswedexvy")
                        .appendQueryParameter("fileType", "jpg,png,gif")
                        .appendQueryParameter("searchType", "image")
                        .appendQueryParameter("key", apiKey)
                        .build().toString();
                StringRequest stringRequest = new StringRequest(Request.Method.GET, uri, onResponse, onError);
                requestQueue.add(stringRequest);
                /// DEBUG
                /*List<Model> modelList = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    modelList.add(new Model("http://lorempixel.com/500/500/animals/" + i, i));
                }
                imageAdapter.insertItems(modelList);*/
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("file", previous.getTheContent());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     *
     */
    private final Response.Listener<String> onResponse = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                List<Model> modelList = new ArrayList<>();
                JSONObject json = new JSONObject(response);
                JSONArray items = json.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String link = item.getString("link");
                    modelList.add(new Model(link, i));
                }
                imageAdapter.insertItems(modelList);
            } catch (Throwable t) {
                Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *
     */
    private final Response.ErrorListener onError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.toString());
        }
    };
}
