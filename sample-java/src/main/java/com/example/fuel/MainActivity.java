package com.example.fuel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import fuel.Fuel;
import fuel.core.Either;
import fuel.core.FuelError;
import fuel.core.Request;
import fuel.core.Response;
import fuel.core.Right;
import kotlin.Unit;
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
                Fuel.Companion.get("http://httpbin.org/get", params).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                //put
                Fuel.Companion.put("http://httpbin.org/put", params).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                //post
                Fuel.Companion.post("http://httpbin.org/post", params).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
                        updateUI(fuelErrorStringEither);
                        return null;
                    }
                });

                //delete
                Fuel.Companion.delete("http://httpbin.org/delete", params).responseString(new Function3<Request, Response, Either<FuelError, String>, Unit>() {
                    @Override
                    public Unit invoke(Request request,
                                       Response response,
                                       Either<FuelError, String> fuelErrorStringEither) {
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
            String result = either.get();
            resultText.setText(resultText.getText() + result);
        } else {
            FuelError error = either.get();
            Log.e(TAG, error.toString());
            resultText.setText(resultText.getText() + error.getException().getMessage());
        }
    }

}
