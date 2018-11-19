package com.example.fuel

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.livedata.liveDataObject
import com.github.kittinunf.fuel.rx.rxObject
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.Reader
import com.github.kittinunf.fuel.stetho.StethoHook

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val bag by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FuelManager.instance.apply {
            basePath = "http://httpbin.org"
            baseHeaders = mapOf("Device" to "Android")
            baseParams = listOf("key" to "value")
            stethoHook = StethoHook("Fuel Sample App")
//            addResponseInterceptor { loggingResponseInterceptor() }
        }

        mainGoCoroutineButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                executeCoroutine()
            }
        }

        mainGoButton.setOnClickListener {
            execute()
        }

        mainClearButton.setOnClickListener {
            mainResultText.text = ""
            mainAuxText.text = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bag.clear()
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

    private suspend fun executeCoroutine() {
        httpGetCoroutine()
    }

    private suspend fun httpGetCoroutine() {
        val (request, response, result) = Fuel.get("/get", listOf("userId" to "123")).awaitStringResponseResult()
        Log.d(TAG, response.toString())
        Log.d(TAG, request.toString())
        update(result)
    }

    private fun httpCancel() {
        val request = Fuel.get("/delay/10")
            .interrupt { Log.d(TAG, it.url.toString() + " is interrupted") }
            .responseString { _, _, _ -> /* noop */ }

        Handler().postDelayed({ request.cancel() }, 1000)
    }

    private fun httpResponseObject() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1"
            .httpGet()
            .also { Log.d(TAG, it.cUrlString()) }
            .responseObject(Issue.Deserializer()) { _, _, result -> update(result) }
    }

    private fun httpListResponseObject() {
        "https://api.github.com/repos/kittinunf/Fuel/issues"
            .httpGet()
            .also { Log.d(TAG, it.cUrlString()) }
            .responseObject(Issue.ListDeserializer()) { _, _, result -> update(result) }
    }

    private fun httpGsonResponseObject() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1"
            .httpGet()
            .also { Log.d(TAG, it.cUrlString()) }
            .responseObject<Issue> { _, _, result -> update(result) }
    }

    private fun httpGet() {
        Fuel.get("/get", listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }

        "/get"
            .httpGet()
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }
    }

    private fun httpPut() {
        Fuel.put("/put", listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }

        "/put"
            .httpPut(listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }
    }

    private fun httpPost() {
        Fuel.post("/post", listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }

        "/post"
            .httpPost(listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }
    }

    private fun httpDelete() {
        Fuel.delete("/delete", listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }

        "/delete"
            .httpDelete(listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { _, _, result -> update(result) }
    }

    private fun httpDownload() {
        val n = 100
        Fuel.download("/bytes/${1024 * n}")
            .destination { _, _ -> File(filesDir, "test.tmp") }
            .progress { readBytes, totalBytes ->
                val progress = "$readBytes / $totalBytes"
                runOnUiThread { mainAuxText.text = progress }
                Log.v(TAG, progress)
            }
            .also { Log.d(TAG, it.toString()) }
            .responseString { _, _, result -> update(result) }
    }

    private fun httpUpload() {
        Fuel.upload("/post")
            .source { _, _ ->
                // create random file with some non-sense string
                val file = File(filesDir, "out.tmp")
                file.writer().use { writer ->
                    repeat(100) {
                        writer.appendln("abcdefghijklmnopqrstuvwxyz")
                    }
                }
                file
            }
            .progress { writtenBytes, totalBytes ->
                Log.v(TAG, "Upload: ${writtenBytes.toFloat() / totalBytes.toFloat()}")
            }
            .also { Log.d(TAG, it.toString()) }
            .responseString { _, _, result -> update(result) }
    }

    private fun httpBasicAuthentication() {
        val username = "U$3|2|\\|@me"
        val password = "P@$\$vv0|2|)"

        Fuel.get("/basic-auth/$username/$password")
            .authentication()
            .basic(username, password)
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { request, _, result -> update(result) }

        "/basic-auth/$username/$password".httpGet()
            .authentication()
            .basic(username, password)
            .also { Log.d(TAG, it.cUrlString()) }
            .responseString { request, _, result -> update(result) }
    }

    private fun httpRxSupport() {
        val disposable = "https://api.github.com/repos/kittinunf/Fuel/issues/1"
            .httpGet()
            .rxObject(Issue.Deserializer())
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result -> Log.d(TAG, result.toString()) }
        bag.add(disposable)
    }

    private fun httpLiveDataSupport() {
        "https://api.github.com/repos/kittinunf/Fuel/issues/1"
            .httpGet()
            .liveDataObject(Issue.Deserializer())
            .observeForever { result -> Log.d(TAG, result.toString()) }
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
            override fun deserialize(reader: Reader) = Gson().fromJson(reader, Issue::class.java)!!
        }

        class ListDeserializer : ResponseDeserializable<List<Issue>> {
            override fun deserialize(reader: Reader): List<Issue> {
                val type = object : TypeToken<List<Issue>>() {}.type
                return Gson().fromJson(reader, type)
            }
        }
    }
}
