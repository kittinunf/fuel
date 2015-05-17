package com.example.kotlin.fueldemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import fuel.Fuel

public class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById(R.id.main_result_text) as TextView
        val goButton = findViewById(R.id.main_go_button) as Button
        val clearButton = findViewById(R.id.main_clear_button) as TextView

        clearButton.setOnClickListener {
            textView.setText("")
        }

        goButton.setOnClickListener {
            Fuel.get("http://httpbin.org/get").responseString { request, response, either ->
//                either.fold({ err ->
//                    Log.e(TAG, err.getMessage())
//                }, { data ->
//                    runOnUiThread { textView.setText(data) }
//                })

                val (exception, data) = either
                runOnUiThread { textView.setText(data) }

            }

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