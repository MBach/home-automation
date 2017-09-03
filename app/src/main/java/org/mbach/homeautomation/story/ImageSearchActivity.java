package org.mbach.homeautomation.story;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mbach.homeautomation.Constants;
import org.mbach.homeautomation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ImageSearchActivity class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public class ImageSearchActivity extends AppCompatActivity implements ImageAdapter.OnClickImageListener {

    private static final String TAG = "ImageSearchActivity";
    private static final String TEMPLATE = "https://www.googleapis.com/customsearch/v1";

    private ImageAdapter imageAdapter;
    private RequestQueue requestQueue = null;

    private Bitmap bitmap;

    //private Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        RecyclerView resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        resultsRecyclerView.setLayoutManager(linearLayoutManager);
        imageAdapter = new ImageAdapter(this);
        resultsRecyclerView.setAdapter(imageAdapter);

        //Intent i = getIntent();
        //i.putExtra("test2", bitmap);
        //setIntent(i);
        //i = new Intent();
        //i.putExtra("test2", bitmap);
        //setIntent(i);
        //setResult(RESULT_OK, i);

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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
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
    private Response.Listener<String> onResponse = new Response.Listener<String>() {
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
    private Response.ErrorListener onError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.toString());
        }
    };

    //private Bitmap bitmap;

    /*public void processBitmap(Bitmap bitmap) {
        Log.d(TAG, "processBitmap, null ? " + (bitmap == null));
        this.bitmap = bitmap;
        Intent i = new Intent();
        i.putExtra("test2", bitmap);
        setIntent(i);
        setResult(RESULT_OK);
        //setResult(RESULT_OK, new Intent().putExtra("test2", bitmap));
        finish(); // intent is null in StoryActivity
        //finishFromChild(this); // intent is null in StoryActivity
        //finishActivity(Constants.RQ_STORY_TO_IMAGE); // nothing happens
        //finishActivityFromChild(this, Constants.RQ_STORY_TO_IMAGE); // nothing happens
        //moveTaskToBack(true);
        //onBackPressed();
    }*/

    @Override
    public void onClick(Bitmap bitmap) {
        Log.d(TAG, "on click ?");
        this.bitmap = bitmap;
        Intent i = new Intent();
        i.putExtra("test2", bitmap);
        setIntent(i);
        setResult(RESULT_OK);
        finish();
    }
}
