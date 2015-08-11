package com.example.fuel

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import fuel.*
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

        Manager.instance.basePath = "http://httpbin.org"
        Manager.instance.baseHeaders = mapOf("Device" to "Android")
        Manager.instance.baseParams = mapOf("key" to "value")

        mainGoButton.setOnClickListener {
            execute()
        }

        mainClearButton.setOnClickListener {
            mainResultText.setText("")
        }
    }

    fun execute() {
        httpGet()
        httpPut()
        httpPost()
        httpDelete()
        httpDownload()
        httpUpload()
        httpBasicAuthentication()
    }

    fun httpGet() {
        Fuel.get("/get", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/get".httpGet().responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpPut() {
        Fuel.put("/put", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/put".httpPut(mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

    }

    fun httpPost() {
        Fuel.post("/post", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/post".httpPost(mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

    }

    fun httpDelete() {
        Fuel.delete("/delete", mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/delete".httpDelete(mapOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

    }

    fun httpDownload() {
        Fuel.download("/bytes/1048").destination { response, url ->
            File(getFilesDir(), "test.tmp")
        }.progress { readBytes, totalBytes ->
            Log.v(TAG, "Download: ${readBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpUpload() {
        Fuel.upload("/post").source { request, url ->
            File(getFilesDir(), "test.tmp")
        }.progress { writtenBytes, totalBytes ->
            Log.v(TAG, "Upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpBasicAuthentication() {
        val username = "username"
        val password = "P@s\$vv0|2|)"
        Fuel.get("/basic-auth/$username/$password").authenticate(username, password).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

        "/basic-auth/$username/$password".httpGet().authenticate(username, password).responseString { request, response, either ->
            Log.d(TAG, request.toString())
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