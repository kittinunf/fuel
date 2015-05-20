package com.example.kotlin.fueldemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import fuel.Fuel
import fuel.core.Either
import fuel.core.Manager
import fuel.core.Response
import kotlin.properties.Delegates

public class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val textView by Delegates.lazy { findViewById(R.id.main_result_text) as TextView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val goButton = findViewById(R.id.main_go_button) as Button
        val clearButton = findViewById(R.id.main_clear_button) as TextView

        clearButton.setOnClickListener {
            textView.setText("")
        }

        goButton.setOnClickListener {

            Manager.sharedInstance.additionalHeaders = mapOf("Device-Type" to "Android")

            Fuel.get("http://httpbin.org/get", mapOf("abc" to 1, "def" to "ghi")).responseString { request, response, either -> updateUI(response, either) }

            Fuel.post("http://httpbin.org/post", mapOf("jkl" to 3.3f)).responseString { request, response, either -> updateUI(response, either) }

            Fuel.put("http://httpbin.org/put", mapOf("mno" to "pqr")).responseString { request, response, either -> updateUI(response, either) }

            Fuel.delete("http://httpbin.org/delete", mapOf("stu" to "vwx", "yza" to "bcd")).responseString { request, response, either -> updateUI(response, either) }

        }
    }

    fun updateUI(response: Response, either: Either<Exception, String>) {
        either.fold({ err ->
            val text = "$response, ${err.getMessage()}"
            Log.e(TAG, text)
            runOnUiThread { textView.setText(text) }
        }, { data ->
            runOnUiThread {
                var text = textView.getText().toString()
                text += data
                textView.setText(text)
            }
        })
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