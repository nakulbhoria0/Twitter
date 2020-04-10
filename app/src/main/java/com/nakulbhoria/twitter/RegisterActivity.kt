package com.nakulbhoria.twitter

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var mAuth:FirebaseAuth?=null
    var downloadUrl = ""


    //val url = "http://192.168.43.78/Register.php?name=ab&email=nakadl@yahoo.com&password=adfcd&picture_path=adfythafs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()

        ivPerson.setOnClickListener{
            checkPermission()
        }

    }
    val IMAGE_CODE = 123
    private fun checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),IMAGE_CODE)
                return
            }
        }

        getImageFromPhone()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){
            IMAGE_CODE ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getImageFromPhone()
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val PICK_IMAGE = 234
    private fun getImageFromPhone(){
        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data!=null){
            val selectedImage = data.data

           Toast.makeText(this, selectedImage.toString(), Toast.LENGTH_LONG).show()

            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            Toast.makeText(this, filePathColumn.toString(), Toast.LENGTH_LONG).show()

            val cursor = contentResolver.query(selectedImage!!, filePathColumn,null,null,null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0])

            Toast.makeText(this, columnIndex.toString(), Toast.LENGTH_LONG).show()

            val picturePath = cursor.getString(columnIndex)
            Toast.makeText(this, picturePath.toString(), Toast.LENGTH_LONG).show()
            cursor.close()

            ivPerson.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun signInAnonymously(view:View){

        //buRegister.isEnabled = false
        mAuth!!.signInAnonymously().addOnCompleteListener(this){task ->
            if(task.isSuccessful){
                updateImageToFirebase()
                Toast.makeText(this,"Login successful",Toast.LENGTH_LONG).show()
                Log.d("Login", "Login Successful")
            }else{
                Log.d("Login", "Login Unsuccessful")
                Toast.makeText(this,"Login unsuccessful",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateImageToFirebase(){
        var currentUser = mAuth!!.currentUser
        val email = etEmail.text.toString()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://twitter-3b99d.appspot.com")
        val dateFormat = SimpleDateFormat("ddMMyyHHmmSS")
        val date = Date()

        val imagePath = splitString(email)+"."+dateFormat.format(date)+".jpg"
        val imageRef = storageRef.child("Images/$imagePath")

        ivPerson.isDrawingCacheEnabled = true
        ivPerson.buildDrawingCache()

        val drawable = ivPerson.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)

        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener{

        }.addOnSuccessListener{taskSnapshot ->

            storageRef.child("Images/$imagePath").downloadUrl.addOnCompleteListener{task ->
                downloadUrl = task.result.toString()
                Toast.makeText(this, downloadUrl ,Toast.LENGTH_LONG).show()

                val name = URLEncoder.encode(etName.text.toString(), "utf-8")
                downloadUrl = URLEncoder.encode(downloadUrl, "utf-8")

                val url = "http://192.168.43.78/Register.php?name=${name}&email=$email&password=${etPassword.text.toString()}&picture_path=$downloadUrl"
                MyAsyncTask().execute(url)


            }

        }

    }

    private fun splitString(string:String):String{
        val data = string.split("@")
        return data[0]
    }

    //http request

    inner class MyAsyncTask:AsyncTask<String, String, String>(){

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
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                if (msg == "user is added") {

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
