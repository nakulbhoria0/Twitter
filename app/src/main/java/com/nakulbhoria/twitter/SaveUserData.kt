package com.nakulbhoria.twitter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class SaveUserData {

    //{'msg':'pass login', 'info':'[{"user_id":"6","name":"NAKUL BHORIA\r\n",
    // "email":"nakulbhoria0@gmail.com\r\n",
    // "password":"2hsis7hs\r\n",
    // "picture_path":"https:\/\/firebasestorage.googleapis.com\/v0\/b\/twitter-3b99d.appspot.com\/o\/Images%2Fnakulbhoria0.050420092250.jpg?alt=media&token=0aaaf29d-3656-4325-a4c0-055e322cea33\r\n"}]'}

    private var mContext: Context?=null
    private var sharedPref:SharedPreferences?=null
    constructor(context: Context){
        mContext = context
        sharedPref = mContext!!.getSharedPreferences("myPref", Context.MODE_PRIVATE)
    }

    fun saveData(userID:String){
        val editor = sharedPref!!.edit()
        editor.putString("userID",userID.toString())
        editor.apply()
    }

    fun loadData(){
        userID  = sharedPref!!.getString("userID","0")!!

        if(userID == "0"){
            val intent = Intent(mContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mContext!!.startActivity(intent)
        }
    }

    companion object{
        var userID = ""
    }
}