package com.example.fuel

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import fuel.Fuel
import fuel.core.*
import kotlinx.android.synthetic.activity_main.mainClearButton
import kotlinx.android.synthetic.activity_main.mainGoButton
import kotlinx.android.synthetic.activity_main.mainResultText
import java.io.File

public class MainActivity : AppCompatActivity() {

    val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainGoButton.setOnClickListener {
            execute()
        }

        mainClearButton.setOnClickListener {
            mainResultText.setText("")
        }
    }

    fun execute() {
//        Manager.sharedInstance.basePath = "http://httpbin.org"
//        Manager.sharedInstance.additionalHeaders = mapOf("Device" to "Android")
//        Manager.sharedInstance.additionalParams = mapOf("key" to "value")
//
//        Fuel.get("/get", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.post("/post", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.put("/put", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.delete("/delete", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.download("/bytes/1048").destination { response, url ->
//            val sd = Environment.getExternalStorageDirectory();
//            val location = File(sd.getAbsolutePath() + "/test")
//            location.mkdir()
//            File(location, "test.tmp")
//        }.progress { readBytes, totalBytes ->
//            Log.e(TAG, "download: ${readBytes.toFloat() / totalBytes.toFloat()}")
//        }.responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        val username = "username"
//        val password = "P@s\$vv0|2|)"
//        Fuel.get("/basic-auth/$username/$password").authenticate(username, password).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.upload("/post").source { request, url ->
//            val sd = Environment.getExternalStorageDirectory();
//            val location = File(sd.getAbsolutePath() + "/test")
//            location.mkdir()
//            File(location, "test.tmp")
//        }.progress { writtenBytes, totalBytes ->
//            Log.e(TAG, "upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
//        }.responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
        Fuel.post("http://jsonplaceholder.typicode.com/posts/").responseString { request, response, either ->
            Log.v(TAG, request.toString())
            updateUI(response, either)
        }

        Fuel.post("https://api.themoviedb.org/3/authentication/token/new", mapOf("a" to 5))
                .responseString { request, response, either ->
                    Log.v(TAG, request.toString())
                    updateUI(response, either)
        }

    }

    fun updateUI(response: Response, either: Either<FuelError, String>) {
        //when checking
        val e: FuelError? = when (either) {
            is Left -> either.get()
            else -> null
        }
        var d: String? = when (either) {
            is Right -> either.get()
            else -> null
        }

        //folding
        either.fold({ e ->
            //left
        }, { d ->
            //right
        })

        //multi-declaration
        val (error, data) = either
        val text = mainResultText.getText().toString()
        if (error != null) {
            Log.e(TAG, error.toString())
            mainResultText.setText(text + String(error.errorData))
        } else {
            Log.d(TAG, response.toString())
            mainResultText.setText(text + data)
        }
    }

}