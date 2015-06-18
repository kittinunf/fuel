package com.example.fuel

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import fuel.Fuel
import fuel.core.*
import java.io.File

import kotlinx.android.synthetic.activity_main.*

public class MainActivity : AppCompatActivity() {

    val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_go_button.setOnClickListener {
            execute()
        }

        main_clear_button.setOnClickListener {
            main_result_text.setText("")
        }
    }

    fun execute() {
        Manager.sharedInstance.additionalHeaders = mapOf("Device" to "Android")
        Manager.sharedInstance.basePath = "http://httpbin.org"

        Fuel.get("/get", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            updateUI(response, either)
        }

        Fuel.post("/post", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            updateUI(response, either)
        }

        Fuel.put("/put", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            updateUI(response, either)
        }

        Fuel.delete("/delete", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            updateUI(response, either)
        }

        Fuel.download("/bytes/1048").destination { response, url ->
            val sd = Environment.getExternalStorageDirectory();
            val location = File(sd.getAbsolutePath() + "/test")
            location.mkdir()
            File(location, "test.tmp")
        }.progress { readBytes, totalBytes ->
            Log.e(TAG, "download: ${readBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, either ->
            updateUI(response, either)
        }

        val username = "username"
        val password = "P@s\$vv0|2|)"
        Fuel.get("/basic-auth/$username/$password").authenticate(username, password).responseString { request, response, either ->
            updateUI(response, either)
        }

        Fuel.upload("/post").source { request, url ->
            val sd = Environment.getExternalStorageDirectory();
            val location = File(sd.getAbsolutePath() + "/test")
            location.mkdir()
            File(location, "test.tmp")
        }.progress { writtenBytes, totalBytes ->
            Log.e(TAG, "upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, either ->
            updateUI(response, either)
        }

    }

    fun updateUI(response: Response, either: Either<FuelError, String>) {
        val (error, data) = either
        val e: FuelError? = when(either) {
            is Left -> either.get()
            else -> null
        }
        var d: String? = when(either) {
            is Right -> either.get()
            else -> null
        }

        val text = main_result_text.getText().toString()
        if (error != null) {
            Log.e(TAG, "${error}, ${response}, ${error.exception}")
            main_result_text.setText(text + String(error.errorData))
        } else {
            main_result_text.setText(text + data)
        }
    }

}