package com.example.kotlin.fueldemo

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import com.example.kotlin.rx.widget.textChanges
import fuel.core.*
import fuel.toolbox.HttpClient
import fuel.util.build
import org.jetbrains.anko.*
import rx.Observable
import java.util.regex.Pattern
import kotlin.properties.Delegates

public class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}")

    var emailTextChanges: Observable<CharSequence> by Delegates.notNull()
    var passwordTextChanges: Observable<CharSequence> by Delegates.notNull()

    //widgets
    var emailEditText: EditText by Delegates.notNull()
    var passwordEditText: EditText by Delegates.notNull()
    var resultTextView: TextView by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        verticalLayout {
            padding = dip(30)

            emailEditText = editText {
                hint = "Email"
                textSize = 18f
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

                emailTextChanges = textChanges
            }

            passwordEditText = editText {
                hint = "Password"
                textSize = 18f
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

                passwordTextChanges = textChanges
            }

            imageView {
                imageURI = Uri.parse("android.resource://${getPackageName()}/" + R.mipmap.ic_launcher)
            }

            linearLayout {
                button("Login") {
                    textSize = 20f
                    enabled = false

                    Observable.combineLatest(emailTextChanges, passwordTextChanges, { emailText, passwordText ->
                        isValidEmailAddress(emailText) && isValidPassword(passwordText)
                    }).subscribe { valid -> enabled = valid }

                    onClick {
                        logIn(emailEditText.getText().toString(), passwordEditText.getText().toString())
                    }
                }

                button("Clear") {
                    textSize = 20f

                    onClick {
                        resultTextView.setText("")
                    }
                }

            }.layoutParams(wrapContent) {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            scrollView {
                resultTextView = textView {
                    text = "Result : "
                }
            }.layoutParams(width = matchParent, height = matchParent)
        }
    }

    fun isValidEmailAddress(text: CharSequence): Boolean {
        return EMAIL_PATTERN.matcher(text).matches()
    }

    fun isValidPassword(text: CharSequence): Boolean {
        return text.length() >= 6
    }

    fun logIn(emailText: String, passwordText: String) {

//        Manager.sharedInstance.additionalHeaders = mapOf("Device" to "Android")
//        Manager.sharedInstance.basePath = "https://httpbin.org"
//
//        Fuel.get("/get", mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.post("/post", mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.put("/put", mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        Fuel.delete("/delete", mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }

        val manager = build(Manager()) {
            client = HttpClient()
        }

//        manager.request(Method.GET, "http://httpbin.org/get", mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }

        manager.request(Method.GET, "/get").responseString { request, response, either ->
            updateUI(response, either)
        }

//        manager.request(Method.PUT, "http://httpbin.org/put", mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        manager.request(Method.POST, "http://httpbin.org/post",mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }
//
//        manager.request(Method.DELETE, "http://httpbin.org/delete",mapOf("email" to emailText, "password" to passwordText)).responseString { request, response, either ->
//            updateUI(response, either)
//        }

//        Fuel.get("/basic-auth/$emailText/$passwordText").authenticate(emailText, passwordText).responseString { request, response, either ->
//            updateUI(response, either)
//                }

    }

    fun updateUI(response: Response, either: Either<FuelError, String>) {
        val (error, data) = either
        runOnUiThread {
            val text = resultTextView.getText().toString()
            if (error != null) {
                Log.e(TAG, "${error}, ${response}")
                resultTextView.setText(text + String(error.errorData))
            } else {
                resultTextView.setText(text + data)
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
            emailEditText.setText("octto@taskworld.com")
            passwordEditText.setText("123456")
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}