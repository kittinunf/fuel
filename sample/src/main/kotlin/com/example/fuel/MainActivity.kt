package com.example.fuel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.*
import com.google.gson.Gson
import kotlinx.android.synthetic.activity_main.mainClearButton
import kotlinx.android.synthetic.activity_main.mainGoButton
import kotlinx.android.synthetic.activity_main.mainResultText
import java.io.File
import java.io.Reader
import java.net.URLEncoder

public class MainActivity : AppCompatActivity() {

    val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Manager.instance.basePath = "http://httpbin.org"
        Manager.instance.baseHeaders = mapOf("Device" to "Android")
        Manager.instance.baseParams = listOf("key" to "value")

        mainGoButton.setOnClickListener {
            execute()
        }

        mainClearButton.setOnClickListener {
            mainResultText.text = ""
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

        httpResponseObject()
    }

    fun httpResponseObject() {
        "http://jsonplaceholder.typicode.com/photos/1".httpGet().responseObject(Photo.Deserializer()) { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpGet() {
        Fuel.get("/get", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/get".httpGet().responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpPut() {
        Fuel.put("/put", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/put".httpPut(listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

    }

    fun httpPost() {
        Fuel.post("/post", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/post".httpPost(listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

    }

    fun httpDelete() {
        Fuel.delete("/delete", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, either)
        }

        "/delete".httpDelete(listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

    }

    fun httpDownload() {
        Fuel.download("/bytes/1048").destination { response, url ->
            File(filesDir, "test.tmp")
        }.progress { readBytes, totalBytes ->
            Log.v(TAG, "Download: ${readBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpUpload() {
        Fuel.upload("/post").source { request, url ->
            File(filesDir, "test.tmp")
        }.progress { writtenBytes, totalBytes ->
            Log.v(TAG, "Upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun httpBasicAuthentication() {
        val username = URLEncoder.encode("username", "UTF-8")
        val password = URLEncoder.encode("1234567890", "UTF-8")
        Fuel.get("/basic-auth/$username/$password").authenticate(username, password).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }

        "/basic-auth/$username/$password".httpGet().authenticate(username, password).responseString { request, response, either ->
            Log.d(TAG, request.toString())
            updateUI(response, either)
        }
    }

    fun <T> updateUI(response: Response, either: Either<FuelError, T>) {
        //multi-declaration
        val (error, data) = either
        if (error != null) {
            Log.e(TAG, response.toString())
            Log.e(TAG, error.toString())
            mainResultText.text = mainResultText.text.toString() + String(error.errorData)
        } else {
            Log.d(TAG, response.toString())
            mainResultText.text = mainResultText.text.toString() + data.toString()
        }
    }

    data class Photo(
            val albumId: Int = 0,
            val id: Int = 0,
            val title: String = "",
            val url: String = "",
            val thumbnailUrl: String = ""
    ) {

        class Deserializer : ResponseDeserializable<Photo> {
            override fun deserialize(reader: Reader) = Gson().fromJson(reader, Photo::class.java)
        }

    }


}