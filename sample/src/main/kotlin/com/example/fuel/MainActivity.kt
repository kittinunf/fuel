package com.example.fuel

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.rx.rx_object
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.Reader

class MainActivity : AppCompatActivity() {

    val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FuelManager.instance.basePath = "http://httpbin.org"
        FuelManager.instance.baseHeaders = mapOf("Device" to "Android")
        FuelManager.instance.baseParams = listOf("key" to "value")

        mainGoButton.setOnClickListener {
            execute()
        }

        mainClearButton.setOnClickListener {
            mainResultText.text = ""
            mainAuxText.text = ""
        }
    }

    fun execute() {
//        httpGet()
//        httpPut()
//        httpPost()
//        httpDelete()
        httpDownload()
//        httpUpload()
//        httpBasicAuthentication()
//        httpResponseObject()
//        httpCancel()
//        httpRxSupport()
    }

    fun httpCancel() {
        val request = Fuel.get("/delay/10").interrupt {
            Log.d(TAG, it.url.toString() + " is interrupted")
        }.responseString { request, response, result ->
            updateUI(response, result)
        }

        Handler().postDelayed({
            request.cancel()
        }, 1000)
    }

    fun httpResponseObject() {
        "http://jsonplaceholder.typicode.com/photos/1".httpGet().responseObject(Photo.Deserializer()) { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }
    }

    fun httpGet() {
        Fuel.get("/get", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, result)
        }

        "/get".httpGet().responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }
    }

    fun httpPut() {
        Fuel.put("/put", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, result)
        }

        "/put".httpPut(listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }

    }

    fun httpPost() {
        Fuel.post("/post", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, result)
        }

        "/post".httpPost(listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }

    }

    fun httpDelete() {
        Fuel.delete("/delete", listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.cUrlString())
            updateUI(response, result)
        }

        "/delete".httpDelete(listOf("foo" to "foo", "bar" to "bar")).responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }

    }

    fun httpDownload() {
        Fuel.download("/bytes/104800000").destination { response, url ->
            File(filesDir, "test.tmp")
        }.progress { readBytes, totalBytes ->
            val progress = "$readBytes / $totalBytes"
            runOnUiThread {
                mainAuxText.text = progress
            }
            Log.v(TAG, progress)
        }.responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }
    }

    fun httpUpload() {
        Fuel.upload("/post").source { request, url ->
            File(filesDir, "test.tmp")
        }.progress { writtenBytes, totalBytes ->
            Log.v(TAG, "Upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }
    }

    fun httpBasicAuthentication() {
        val username = "U$3|2|\\|@me"
        val password = "P@$\$vv0|2|)"
        Fuel.get("/basic-auth/$username/$password").authenticate(username, password).responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }

        "/basic-auth/$username/$password".httpGet().authenticate(username, password).responseString { request, response, result ->
            Log.d(TAG, request.toString())
            updateUI(response, result)
        }
    }

    fun httpRxSupport() {
        "http://jsonplaceholder.typicode.com/photos/1".httpGet().rx_object(Photo.Deserializer()).subscribe {
            Log.d(TAG, it.toString())
        }
    }

    fun <T : Any> updateUI(response: Response, result: Result<T, FuelError>) {
        //multi-declaration
        val (data, error) = result
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