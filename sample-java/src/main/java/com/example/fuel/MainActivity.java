package com.example.fuel;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import fuel.Fuel;
import fuel.core.FuelError;
import fuel.core.Handler;
import fuel.core.Request;
import fuel.core.Response;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private Map<String, String> params = new HashMap<String, String>() {{
        put("foo1", "bar1");
        put("foo2", "bar2");
    }};

    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = (TextView) findViewById(R.id.main_result_text);

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
        clearButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                resultText.setText("");
            }

        });
    }

    private void updateUI(FuelError error, String result) {
        if (error == null) {
            resultText.setText(resultText.getText() + result);
        } else {
            Log.e(TAG, "error: " + error.getException().getMessage());
            resultText.setText(resultText.getText() + error.getException().getMessage());
        }
    }

}
