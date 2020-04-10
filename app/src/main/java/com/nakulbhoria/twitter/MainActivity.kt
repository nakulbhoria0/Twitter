package com.nakulbhoria.twitter

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_tweet.*
import kotlinx.android.synthetic.main.add_tweet.view.*
import kotlinx.android.synthetic.main.follow.view.*
import kotlinx.android.synthetic.main.tweets_item.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var tweets = ArrayList<Tweet>()
    var adapter:TweetAdapter?=null
    var downloadURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val saveUserData = SaveUserData(applicationContext)
        saveUserData.loadData()

        adapter = TweetAdapter(this)
        listView.adapter = adapter


        searchInDatabase("%", 0)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)

        val sv = menu!!.findItem(R.id.search).actionView as SearchView
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(sm.getSearchableInfo(componentName))
        sv.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchInDatabase(query!!, 0)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchInDatabase(newText!!, 0)
                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }
    var following = false;

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.home ->{
                searchInDatabase("%", 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchInDatabase(SearchText:String, startFrom:Int){
        val searchText = URLEncoder.encode(SearchText, "utf-8")
        val url = "http://192.168.43.78/TweetList.php?op=3&query=$searchText&StartFrom=$startFrom"
        MyAsyncTask().execute(url)
    }
    var followerName=""
    var followerImage=""
    inner class TweetAdapter: BaseAdapter {
        var context: Context?=null
        constructor(context:Context){
            this.context = context
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val currentTweet = tweets[position]
            return when {
                currentTweet.tweetDate.equals("add") -> {
                    val myView = layoutInflater.inflate(R.layout.add_tweet, null)
                    myView.buttonAttachment.setOnClickListener {
                        loadImage()
                    }
                    myView.buttonSend.setOnClickListener {
                        tweets.add(0, Tweet("0","him","url","loading","","",""))
                        adapter!!.notifyDataSetChanged()
                        val textPost = URLEncoder.encode(myView.textPost.text.toString(),"utf-8")
                        downloadURL = URLEncoder.encode(downloadURL, "utf-8")
                        val url = "http://192.168.43.78/AddTweets.php?user_id=${SaveUserData.userID}&tweet_text=$textPost&tweet_picture=$downloadURL"
                        MyAsyncTask().execute(url)
                        myView.textPost.setText("")
                    }
                    myView
                }
                currentTweet.tweetDate.equals("follow") ->{
                    val myView = layoutInflater.inflate(R.layout.follow, null)
                    val personId = currentTweet.personID
                    myView.nameText.text = followerName
                    Picasso.get().load(followerImage).into(myView.followImage)

                    val url = "http://192.168.43.78/ISFollowing.php?user_id=${SaveUserData.userID}&following_user_id=$personId"
                    MyAsyncTask().execute(url)
                    myView.buFollow.setOnClickListener {

                        if(following){
                            myView.buFollow.text=="unfollow"
                            //http://192.168.43.78/UserFollowing.php?op=2&user_id=1&following_user_id=2
                            val followURL = "http://192.168.43.78/UserFollowing.php?op=2&user_id=${SaveUserData.userID}&following_user_id=$personId"
                            MyAsyncTask().execute(followURL)
                        }else{
                            myView.buFollow.text=="follow"
                            //http://192.168.43.78/UserFollowing.php?op=1&user_id=1&following_user_id=2
                            val followURL = "http://192.168.43.78/UserFollowing.php?op=1&user_id=${SaveUserData.userID}&following_user_id=$personId"
                            MyAsyncTask().execute(followURL)
                        }

                    }
                    myView
                }
                currentTweet.tweetDate.equals("loading") -> {
                    return layoutInflater.inflate(R.layout.loading, null)
                }
                else -> {
                    val myView = layoutInflater.inflate(R.layout.tweets_item,null)
                    Picasso.get().load(currentTweet.tweetImageUrl).into(myView.imagePost)
                    Picasso.get().load(currentTweet.personImage).into(myView.imageProfile)
                    myView.textViewPost.text=currentTweet.tweetText
                    myView.textDate.text = currentTweet.tweetDate

                    myView.textUser.text = currentTweet.personName

                    myView.textUser.setOnClickListener {

                        followerName = currentTweet.personName!!
                        followerImage = currentTweet.personImage!!
                        val url = "http://192.168.43.78/FollowersTweet.php?&user_id=${currentTweet.personID}&StartFrom=0"
                        MyAsyncTask().execute(url)
                    }


                    myView
                }
            }
        }

        override fun getItem(position: Int): Any {
            return tweets[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return tweets.size
        }
    }


    private fun loadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE)
    }
    val PICK_IMAGE = 123


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==PICK_IMAGE && data!=null && resultCode == Activity.RESULT_OK){
            val selectedImage = data.data
            val filePath = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!,filePath,null,null,null)

            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePath[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            uploadImage(BitmapFactory.decodeFile(picturePath))


        }
    }

    private fun uploadImage(bitmap: Bitmap?) {
        tweets.add(0, Tweet("0","him","url","loading","","",""))
        adapter!!.notifyDataSetChanged()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://twitter-3b99d.appspot.com")
        val dateFormat = SimpleDateFormat("ddMMyyHHmmSS")
        val dateObject = Date()
        val imagePath = SaveUserData.userID +"."+dateFormat.format(dateObject)+".jpg"
        val imageRef = storageRef.child("ImagesPost/$imagePath")

        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)


        uploadTask.addOnFailureListener{
            Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener {taskSnapshot ->

            storageRef.child("ImagesPost/$imagePath").downloadUrl.addOnCompleteListener { task ->
                downloadURL = task.result.toString()
                tweets.removeAt(0)
                adapter!!.notifyDataSetChanged()


            }
        }
    }
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
                if (msg == "tweet is added") {
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    downloadURL=""
                    tweets.removeAt(0)
                    adapter!!.notifyDataSetChanged()
                }else if(msg == "has tweet"){
                    tweets.clear()
                    tweets.add(Tweet("0","Hello", "url","add","","",""))

                    val tweetsArray = JSONArray(json.getString("info"))
                    for(i in 0 until tweetsArray.length()){
                        val currentTweet = tweetsArray.getJSONObject(i)
                        tweets.add(Tweet(currentTweet.getString("tweet_id"),currentTweet.getString("tweet_text"),
                            currentTweet.getString("tweet_picture"),currentTweet.getString("tweet_date"),
                            currentTweet.getString("name"),currentTweet.getString("picture_path"),
                            currentTweet.getString("user_id")))
                    }


                }else if(msg == "follower has tweet"){

                    val userId = json.getString("user_id")
                    Toast.makeText(applicationContext, "user id: $userId", Toast.LENGTH_LONG).show()
                    tweets.clear()
                    tweets.add(Tweet("0","Hello", "url","follow","","",userId))

                    val tweetsArray = JSONArray(json.getString("info"))
                    for(i in 0 until tweetsArray.length()){
                        val currentTweet = tweetsArray.getJSONObject(i)
                        tweets.add(Tweet(currentTweet.getString("tweet_id"),currentTweet.getString("tweet_text"),
                            currentTweet.getString("tweet_picture"),currentTweet.getString("tweet_date"),
                            currentTweet.getString("name"),currentTweet.getString("picture_path"),
                            currentTweet.getString("user_id")))
                    }
                }
                else if(msg =="no tweet"){
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    tweets.clear()
                    tweets.add(Tweet("0","Hello", "url","add","","",""))
                }else if(msg =="following"){
                    following=true
                }else if(msg == "not following"){
                    following=false
                }

                adapter!!.notifyDataSetChanged()


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
