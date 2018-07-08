package com.example.fuel;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.fuel.databinding.ActivityMainBinding;
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

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.mainGoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                execute();
            }

        });

        binding.mainClearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                binding.mainResultText.setText("");
            }

        });
    }

    private void execute() {
        httpGet();
        httpPut();
        httpPost();
        httpDelete();
        httpDownload();
        httpUpload();
        httpBasicAuthentication();
    }

    private void httpGet() {
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
    }

    private void httpPut() {
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
    }

    private void httpPost() {
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
    }

    private void httpDelete() {
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
    }

    private void httpDownload() {
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
    }

    private void httpUpload() {
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

    private void httpBasicAuthentication() {
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
    }

    private void updateUI(final FuelError error, final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (error == null) {
                    binding.mainResultText.append(result);
                } else {
                    Log.e(TAG, "error: " + error.getException().getMessage());
                    binding.mainResultText.append(error.getException().getMessage());
                }
            }
        });
    }
}