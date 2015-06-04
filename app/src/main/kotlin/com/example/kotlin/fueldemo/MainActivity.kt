package com.example.kotlin.fueldemo

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.kotlin.util.MainThreadExecutor
import fuel.Fuel
import fuel.core.*
import kotlinx.android.synthetic.activity_main.main_clear_button
import kotlinx.android.synthetic.activity_main.main_go_button
import kotlinx.android.synthetic.activity_main.main_result_text
import java.io.File

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
        Manager.sharedInstance.basePath = "https://httpbin.org"
        Manager.callbackExecutor = MainThreadExecutor()

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

        Fuel.download("/bytes/1048576").destination { response, url ->
            val sd = Environment.getExternalStorageDirectory();
            val location = File(sd.getAbsolutePath() + "/test")
            location.mkdir()
            File(location, "test.tmp")
        }.progress { readBytes, totalBytes ->
            Log.e(TAG, (readBytes.toFloat() / totalBytes.toFloat()).toString());
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}