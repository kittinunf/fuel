package com.example.fuel;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import fuel.Fuel;
import fuel.core.Either;
import fuel.core.FuelError;
import fuel.core.Request;
import fuel.core.Response;
import fuel.core.Right;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private Map<String, String> params = new HashMap<String, String>() {{
        put("foo1", "bar1");
        put("foo2", "bar2");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button goButton = (Button) findViewById(R.id.main_go_button);
        goButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //get
                Fuel.Http.get("http://httpbin.org/get", params).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                //put
                Fuel.Http.put("http://httpbin.org/put").responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                //post
                Fuel.Http.post("http://httpbin.org/post", params).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                //delete
                Fuel.Http.delete("http://httpbin.org/delete").responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                Fuel.Http.download("http://httpbin.org/bytes/1048").destination(new Function2<Response, URL, File>() {
                    @Override
                    public File invoke(Response response, URL url) {
                        File sd = Environment.getExternalStorageDirectory();
                        File location = new File(sd.getAbsolutePath() + "/test");
                        location.mkdir();
                        return new File(location, "test-java.tmp");
                    }
                }).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request, Response response, Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                String username = "username";
                String password = "P@s$vv0|2|)";
                Fuel.Http.get("http://httpbin.org/basic-auth/" + username + "/" + password).authenticate(username, password).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request, Response response, Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });
            }
        });
    }

    private void updateUI(Either<FuelError, String> either) {
        TextView resultText = (TextView) findViewById(R.id.main_result_text);
        if (either instanceof Right) {
            String result = (String) either.get();
            resultText.setText(resultText.getText() + result);
        } else {
            FuelError error = (FuelError) either.get();
            Log.e(TAG, error.getMessage());
            resultText.setText(resultText.getText() + error.getException().getMessage());
        }
    }

}
