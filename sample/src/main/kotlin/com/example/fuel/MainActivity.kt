package com.example.fuel

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.livedata.liveDataObject
import com.github.kittinunf.fuel.rx.rx_object
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.mainAuxText
import kotlinx.android.synthetic.main.activity_main.mainClearButton
import kotlinx.android.synthetic.main.activity_main.mainGoButton
import kotlinx.android.synthetic.main.activity_main.mainResultText
import java.io.File
import java.io.Reader

class MainActivity : AppCompatActivity() {

    private val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FuelManager.instance.apply {
            basePath = "http://httpbin.org"
            baseHeaders = mapOf("Device" to "Android")
            baseParams = listOf("key" to "value")
//            addResponseInterceptor { loggingResponseInterceptor() }
        }

        mainGoButton.setOnClickListener {
            execute()
        }

        mainClearButton.setOnClickListener {
            mainResultText.text = ""
            mainAuxText.text = ""
        }
    }

    private fun execute() {
        httpGet()
        httpPut()
        httpPost()
        httpDelete()
        httpDownload()
        httpUpload()
        httpBasicAuthentication()
        httpListResponseObject()
        httpResponseObject()
        httpGsonResponseObject()
        httpCancel()
        httpRxSupport()
        httpLiveDataSupport()
    }

    private fun httpCancel() {
        val request = Fuel.get("/delay/10").interrupt {
            Log.d(TAG, it.url.toString() + " is interrupted")
        }.responseString { _, _, result ->
            update(result)
        }

        Handler().postDelayed({
            request.cancel()
        }, 1000)
    }

    private fun httpResponseObject() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1".httpGet()
                .responseObject(Issue.Deserializer()) { request, _, result ->
                    Log.d(TAG, request.toString())
                    update(result)
                }
    }


    private fun httpListResponseObject() {
        "https://api.github.com/repos/kittinunf/Fuel/issues".httpGet()
                .responseObject(Issue.ListDeserializer()) { _, _, result ->
                    update(result)
                }
    }

    private fun httpGsonResponseObject() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1".httpGet()
                .responseObject<Issue> { request, _, result ->
                    Log.d(TAG, request.toString())
                    update(result)
                }
    }

    private fun httpGet() {
        Fuel.get("/get", listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.cUrlString())
            update(result)
        }

        "/get".httpGet().responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }
    }

    private fun httpPut() {
        Fuel.put("/put", listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.cUrlString())
            update(result)
        }

        "/put".httpPut(listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }

    }

    private fun httpPost() {
        Fuel.post("/post", listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.cUrlString())
            update(result)
        }

        "/post".httpPost(listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }

    }

    private fun httpDelete() {
        Fuel.delete("/delete", listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.cUrlString())
            update(result)
        }

        "/delete".httpDelete(listOf("foo" to "foo", "bar" to "bar")).responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }

    }

    private fun httpDownload() {
        val n = 100
        Fuel.download("/bytes/${1024 * n}").destination { _, _ ->
            File(filesDir, "test.tmp")
        }.progress { readBytes, totalBytes ->
            val progress = "$readBytes / $totalBytes"
            runOnUiThread {
                mainAuxText.text = progress
            }
            Log.v(TAG, progress)
        }.responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }
    }

    private fun httpUpload() {
        Fuel.upload("/post").source { _, _ ->
            //create random file with some non-sense string
            File(filesDir, "out.tmp").apply {
                while (length() < 100) {
                    writeText("abcdefghijklmnopqrstuvwxyz")
                }
            }
        }.progress { writtenBytes, totalBytes ->
            Log.v(TAG, "Upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
        }.responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }
    }

    private fun httpBasicAuthentication() {
        val username = "U$3|2|\\|@me"
        val password = "P@$\$vv0|2|)"
        Fuel.get("/basic-auth/$username/$password").authenticate(username, password).responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }

        "/basic-auth/$username/$password".httpGet().authenticate(username, password).responseString { request, _, result ->
            Log.d(TAG, request.toString())
            update(result)
        }
    }

    private fun httpRxSupport() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1".httpGet()
                .rx_object(Issue.Deserializer())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    Log.d(TAG, result.toString())
                }
    }

    private fun httpLiveDataSupport() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1".httpGet()
                .liveDataObject(Issue.Deserializer())
                .observeForever { result ->
                    Log.d(TAG, result.toString())
                }
    }

    private fun <T : Any> update(result: Result<T, FuelError>) {
        result.fold(success = {
            mainResultText.append(it.toString())
        }, failure = {
            mainResultText.append(String(it.errorData))
        })
    }

    data class Issue(
            val id: Int = 0,
            val title: String = "",
            val url: String = ""
    ) {
        class Deserializer : ResponseDeserializable<Issue> {
            override fun deserialize(reader: Reader) = Gson().fromJson(reader, Issue::class.java)
        }

        class ListDeserializer : ResponseDeserializable<List<Issue>> {
            override fun deserialize(reader: Reader): List<Issue> {
                val type = object : TypeToken<List<Issue>>() {}.type
                return Gson().fromJson(reader, type)
            }
        }
    }

}