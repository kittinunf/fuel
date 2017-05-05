package com.example.fuel;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private final List<Pair<String, String>> params = new ArrayList<Pair<String, String>>() {{
        add(new Pair<String, String>("foo1", "bar1"));
        add(new Pair<String, String>("foo2", "bar2"));
    }};

    private TextView resultText;
    private TextView auxText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = (TextView) findViewById(R.id.main_result_text);
        auxText = (TextView) findViewById(R.id.main_aux_text);

        Button goButton = (Button) findViewById(R.id.main_go_button);
        goButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //get
                Fuel.get("http://httpbin.org/get", params).responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });

                Fuel.get("http://httpbin.org/get", params).responseString();

                //put
                Fuel.put("http://httpbin.org/put").responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });

                //post
                Fuel.post("http://httpbin.org/post", params).responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });

                //delete
                Fuel.delete("http://httpbin.org/delete").responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });

                Fuel.download("http://httpbin.org/bytes/1048").destination(new Function2<Response, URL, File>() {
                    @Override
                    public File invoke(Response response, URL url) {
                        File sd = Environment.getExternalStorageDirectory();
                        File location = new File(sd.getAbsolutePath() + "/test");
                        location.mkdir();
                        return new File(location, "test-java.tmp");
                    }
                }).responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });

                String username = "username";
                String password = "P@s$vv0|2|)";
                Fuel.get("http://httpbin.org/basic-auth/" + username + "/" + password).authenticate(username, password).responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });

                Fuel.upload("http://httpbin.org/post").source(new Function2<Request, URL, File>() {
                    @Override
                    public File invoke(Request request, URL url) {
                        File sd = Environment.getExternalStorageDirectory();
                        File location = new File(sd.getAbsolutePath() + "/test");
                        location.mkdir();
                        return new File(location, "test-java.tmp");
                    }
                }).responseString(new Handler<String>() {
                    @Override
                    public void failure(@NotNull Request request, @NotNull Response response, @NotNull FuelError error) {
                        updateUI(error, null);
                    }

                    @Override
                    public void success(@NotNull Request request, @NotNull Response response, String data) {
                        updateUI(null, data);
                    }
                });
            }

        });

        Button clearButton = (Button) findViewById(R.id.main_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resultText.setText("");
            }

        });
    }

    private void updateUI(final FuelError error, final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (error == null) {
                    resultText.setText(resultText.getText() + result);
                } else {
                    Log.e(TAG, "error: " + error.getException().getMessage());
                    resultText.setText(resultText.getText() + error.getException().getMessage());
                }
            }
        });
    }
}
