package edu.harvard.cs50.pokedex;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView pokemonDescription;
    private String url;
    private ImageView pokemonImage;
    private RequestQueue requestQueue;
    public static int pokemonId = 0;
    //Button
    Button catchButton;
    SharedPreferences pref ;
    SharedPreferences.Editor editor ;


    private String pokename = "name" ;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        pokemonImage = findViewById(R.id.img);
        pokemonDescription = findViewById(R.id.poke_description);
        //to catch and save the state
        catchButton = (Button) findViewById(R.id.Catch);


        catchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCatch(v);
            }
        });

        load();
        pref = getApplicationContext().getSharedPreferences(nameTextView.getText().toString(), 0); // 0 - for private mode
        editor = pref.edit();
    }

    private void saveData(String s){
        editor.putString(nameTextView.getText().toString(), s);
        editor.commit();
    }

    private void loadData(){
        catchButton.setText(pref.getString(nameTextView.getText().toString(),catchButton.getText().toString()));
    }


    public void toggleCatch(View view) {
        // gotta catch 'em all!
        if(catchButton.getText().toString().equals("Release")){
            catchButton.setText("Catch");
            saveData("Catch");
        }
        else{
            catchButton.setText("Release");
            saveData("Release");
        }
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {

                    pokemonId= response.getInt("id");
                    String url1 = "https://pokeapi.co/api/v2/pokemon-species/" + pokemonId + "/";
                    final JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray flavorEntries = response.getJSONArray("flavor_text_entries");
                                for (int i = 0; i < flavorEntries.length(); i++) {

                                    JSONObject description = flavorEntries.getJSONObject(i);

                                    String lang = description.getJSONObject("language").getString("name");

                                    if(lang.equals("en")){
                                        String flavorText = description.getString("flavor_text");
                                        pokemonDescription.setText(flavorText);
                                        break;
                                    }

                                }
                            } catch (JSONException e) {
                                Log.e("cs50", "Json error", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("cs50", "flavorText list error", error);
                        }
                    });
                    requestQueue.add(request1);

                    nameTextView.setText(response.getString("name"));
                    loadData();
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    String imgUrl = response.getJSONObject("sprites").getString("front_default");
                    new DownloadSpriteTask().execute(imgUrl);
                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        } else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);

    }
    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // load the bitmap into the ImageView!

            pokemonImage.setImageBitmap(bitmap);
        }
    }


}
