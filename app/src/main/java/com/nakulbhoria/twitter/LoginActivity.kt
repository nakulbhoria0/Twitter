package com.nakulbhoria.twitter

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buLogin.setOnClickListener {
            val url = "http://192.168.43.78/login.php?email=${etEmail.text}&password=${etPassword.text}"
            MyAsyncTask().execute(url)
        }
    }

    fun register(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }



    //http request

    inner class MyAsyncTask: AsyncTask<String, String, String>(){

        override fun onPreExecute() {
            //
        }
        override fun onPostExecute(result: String?) {
            //super.onPostExecute(result)
        }

        override fun onProgressUpdate(vararg values: String?) {
            try {


                val json = JSONObject(values[0]!!)
                val msg = json.getString("msg")
                if (msg == "pass login") {

                    val info = JSONArray(json.getString("info"))
                    val credential = info.getJSONObject(0)
                    val name = credential.getString("name")
                    //val name = tName.substringBeforeLast()
                    val userID = credential.getString("user_id")
                    Toast.makeText(applicationContext, name, Toast.LENGTH_LONG).show()

                    val saveData = SaveUserData(applicationContext)
                    saveData.saveData(userID)
                    finish()
                }
            } catch (ex: Exception) {
            }
        }

        override fun doInBackground(vararg params: String?): String{

            try{
                val url = URL(params[0])
                val urlConnect = url.openConnection() as HttpURLConnection
                urlConnect.connectTimeout = 7000
                val op = Operations()
                val inString = op.streamToString(urlConnect.inputStream)
                publishProgress(inString)
            }catch (ex:Exception){}

            return ""

        }
    }

}
