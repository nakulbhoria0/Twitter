package com.nakulbhoria.twitter

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Operations {

     fun streamToString(inputStream: InputStream?): String? {

        val bufferedReader  = BufferedReader(InputStreamReader(inputStream!!))
        var line:String
        var string = ""

        try {
            do{
                line = bufferedReader.readLine()
                if(line!=null){
                    string += line
                }
            }while(line!=null)
            inputStream.close()
        }catch (ex:Exception){}

        return string
    }
}