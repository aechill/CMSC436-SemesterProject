package com.example.whobuiltthat

/*
    This Activity was developed to show information about a Company.

    This code uses some libraries that are licensed under the Apache 2.0 License, the terms of
    which as well as the tools can be found in the NOTICES file that came with this application.

    The animation, logo-fetching and displaying functionality, as well as UI for this were implemented by Alex Chill
    The name parser and other backend elements, as well as the bridge between front-end and back-end
    were implemented by Greg Newbold
 */

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.pixplicity.sharp.Sharp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.apache.commons.codec.digest.DigestUtils
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.MediaInfoDocument
import org.wikidata.wdtk.datamodel.interfaces.Statement
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import kotlin.concurrent.thread

class CurrentCompanyActivity : Activity() {
    protected lateinit var data: ArrayList<String>
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_company)

        // starts the gradient animations
        val layout = findViewById<ConstraintLayout>(R.id.back_layout_company)
        val aniDrawable = layout.background as AnimationDrawable

        aniDrawable.setEnterFadeDuration(2000)
        aniDrawable.setExitFadeDuration(3000)

        aniDrawable.start()

        data = getFile()
        var name = intent.getStringExtra("name")
        var found = search(data,name.toString())
        val scrolling = ScrollingMovementMethod()
        if(found[0]==""){
            findViewById<TextView>(R.id.company_name_text).text = "no company"
        } else {
            findViewById<TextView>(R.id.company_name_text).text = found[0]
            val companySuffixes = listOf(" Inc.", " Incorporated", " Co.", " Company", " & Co.", " & Company", " Ltd.", " Limited",
            " and Company", " and Co.", " Corporation", " Corp", " Corp.", " (brand)")

            name = found[0]
            thread(start = true) {
                val dataFetcher = WikibaseDataFetcher.getWikidataDataFetcher()
                var wasFound = -1
                //Log.i("test", dataFetcher.getEntityDocumentByTitle("enwiki", name!!.capitalize()).entityId.id)
                //val qName = dataFetcher.getEntityDocumentByTitle("enwiki", name!!.capitalize()).entityId.id
                var currCompanyWiki = dataFetcher.getEntityDocumentByTitle("enwiki", name!!.capitalize())
                if (currCompanyWiki == null) {
                    var i = 0
                    while (i < companySuffixes.size) {
                        currCompanyWiki = dataFetcher.getEntityDocumentByTitle("enwiki", (name!!.capitalize() + companySuffixes[i]))

                        if (currCompanyWiki != null) {
                            val toStatementGroup = currCompanyWiki as ItemDocumentImpl
                            val picName = toStatementGroup.findStatementGroup("P154")
                            if (picName != null) {
                                val picNameStr = picName.statements[0].value.toString().removeSurrounding("\"").replace(' ', '_')

                                val hash = DigestUtils.md5Hex(picNameStr)

                                val imgUrl = ("https://upload.wikimedia.org/wikipedia/commons/" + hash.get(0) + "/" + hash.substring(0, 2) + "/" + picNameStr)
                                //val imgUrl = ("https://www.wikidata.org/wiki/" + qName + "#/media/File:" + picName).replace(' ', '_')

                                val currUrl = URL(imgUrl)
                                val stream = currUrl.openConnection().getInputStream()

                                if (picNameStr.contains(".svg")) {
                                    Sharp.loadInputStream(stream).into(findViewById<ImageView>(R.id.logoView))
                                } else if (picNameStr.contains(".png") ||
                                    picNameStr.contains(".jpg") ||
                                    picNameStr.contains(".jpeg")) {

                                    Handler(Looper.getMainLooper()).post {
                                        Picasso.get().load(imgUrl).into(findViewById<ImageView>(R.id.logoView))
                                    }
                                }  else {
                                    Handler(Looper.getMainLooper()).post {
                                        findViewById<ImageView>(R.id.logoView).setImageBitmap(BitmapFactory.decodeStream(applicationContext.assets.open("NoLogoFound.PNG")))
                                        Toast.makeText(applicationContext, "This logo is currently unsupported", Toast.LENGTH_LONG).show()
                                    }

                                }
                                // ends the while loop
                                i = companySuffixes.size + 2
                            }
                        }
                        i += 1
                    }

                    if (i != companySuffixes.size + 3) {
                        Handler(Looper.getMainLooper()).post {
                            findViewById<ImageView>(R.id.logoView).setImageBitmap(BitmapFactory.decodeStream(applicationContext.assets.open("NoLogoFound.PNG")))
                            Toast.makeText(applicationContext, "Logo couldn't be found", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    var i = 0
                    val toStatementGroup = currCompanyWiki as ItemDocumentImpl
                    val picName = toStatementGroup.findStatementGroup("P154")
                    if (picName != null) {
                        val picNameStr = picName.statements[0].value.toString().removeSurrounding("\"").replace(' ', '_')

                        val hash = DigestUtils.md5Hex(picNameStr)

                        val imgUrl = ("https://upload.wikimedia.org/wikipedia/commons/" + hash.get(0) + "/" + hash.substring(0, 2) + "/" + picNameStr)
                        //val imgUrl = ("https://www.wikidata.org/wiki/" + qName + "#/media/File:" + picName).replace(' ', '_')

                        val currUrl = URL(imgUrl)
                        val stream = currUrl.openConnection().getInputStream()

                        if (picNameStr.contains(".svg")) {
                            Sharp.loadInputStream(stream).into(findViewById<ImageView>(R.id.logoView))
                        } else if (picNameStr.contains(".png") ||
                            picNameStr.contains(".jpg") ||
                            picNameStr.contains(".jpeg")) {

                            Handler(Looper.getMainLooper()).post {
                                Picasso.get().load(imgUrl).into(findViewById<ImageView>(R.id.logoView))
                            }
                        }  else {
                            Handler(Looper.getMainLooper()).post {
                                findViewById<ImageView>(R.id.logoView).setImageBitmap(BitmapFactory.decodeStream(applicationContext.assets.open("NoLogoFound.PNG")))
                                Toast.makeText(applicationContext, "This logo is currently unsupported", Toast.LENGTH_LONG).show()
                            }

                        }

                        // ends the while loop
                        i = companySuffixes.size + 2
                    } else {
                        while (i < companySuffixes.size) {
                            currCompanyWiki = dataFetcher.getEntityDocumentByTitle("enwiki", (name!!.capitalize() + companySuffixes[i]))

                            if (currCompanyWiki != null) {
                                val toStatementGroup = currCompanyWiki as ItemDocumentImpl
                                val picName = toStatementGroup.findStatementGroup("P154")
                                if (picName != null) {
                                    val picNameStr = picName.statements[0].value.toString().removeSurrounding("\"").replace(' ', '_')

                                    val hash = DigestUtils.md5Hex(picNameStr)

                                    val imgUrl = ("https://upload.wikimedia.org/wikipedia/commons/" + hash.get(0) + "/" + hash.substring(0, 2) + "/" + picNameStr)
                                    //val imgUrl = ("https://www.wikidata.org/wiki/" + qName + "#/media/File:" + picName).replace(' ', '_')

                                    val currUrl = URL(imgUrl)
                                    val stream = currUrl.openConnection().getInputStream()

                                    if (picNameStr.contains(".svg")) {
                                        Sharp.loadInputStream(stream).into(findViewById<ImageView>(R.id.logoView))
                                    } else if (picNameStr.contains(".png") ||
                                        picNameStr.contains(".jpg") ||
                                        picNameStr.contains(".jpeg")) {

                                        Handler(Looper.getMainLooper()).post {
                                            Picasso.get().load(imgUrl).into(findViewById<ImageView>(R.id.logoView))
                                        }
                                    }  else {
                                        Handler(Looper.getMainLooper()).post {
                                            findViewById<ImageView>(R.id.logoView).setImageBitmap(BitmapFactory.decodeStream(applicationContext.assets.open("NoLogoFound.PNG")))
                                            Toast.makeText(applicationContext, "This logo is currently unsupported", Toast.LENGTH_LONG).show()
                                        }

                                    }
                                    // ends the while loop
                                    i = companySuffixes.size + 2
                                }
                            }
                            i += 1
                        }

                        if (i != companySuffixes.size + 3) {
                            Handler(Looper.getMainLooper()).post {
                                findViewById<ImageView>(R.id.logoView).setImageBitmap(BitmapFactory.decodeStream(applicationContext.assets.open("NoLogoFound.PNG")))
                                Toast.makeText(applicationContext, "Logo couldn't be found", Toast.LENGTH_LONG).show()
                            }
                        }
                    }


                }
            }

            findViewById<ImageView>(R.id.logoView).visibility = View.VISIBLE
            //added by Greg
            val arr = data[found[1].toInt()].split("/")
            findViewById<TextView>(R.id.about_company_text).text = arr[3]
            val scrolling = ScrollingMovementMethod()
            findViewById<TextView>(R.id.about_company_text).movementMethod = scrolling
            if (found[2].toBoolean() == true) {
                findViewById<TextView>(R.id.parentcompany_text).text = arr[1]
            }

            val splitByComma = arr[2].split(", ").toMutableList()
            if (!splitByComma.isNullOrEmpty()) {
                splitByComma.sort()
                splitByComma[0] = "•\t" + splitByComma[0]
            }
            var joined = TextUtils.join("\n•\t", splitByComma)
            joined.dropLast(3)
            findViewById<TextView>(R.id.affiliatecompany_text).text = joined
            findViewById<TextView>(R.id.affiliatecompany_text).movementMethod = scrolling
            findViewById<TextView>(R.id.rank_text).text = arr[0]
            findViewById<TextView>(R.id.rank_text).visibility = View.VISIBLE
        }
    }

    //Code added by Greg
    fun didYouMean(s1: String, s2: String): Boolean {
        var count = 0
        var bool = true
        for (i in 0 until s1.length) {
            if (i < s2.length) {
                if (s2[i] == s1[i] && bool) {
                    count++
                    bool = false
                }
                if (i + 1 < s2.length) {
                    if (s2[i + 1] == s1[i] && bool) {
                        count++
                        bool = false
                    }
                }
                if (i - 1 >= 0) {
                    if (s2[i - 1] == s1[i]&& bool) {
                        count++
                        bool = false
                    }
                }
                if (i + 2 < s2.length) {
                    if (s2[i + 2] == s1[i]&& bool) {
                        count++
                        bool = false
                    }
                }
                if (i - 2 >= 0) {
                    if (s2[i - 2] == s1[i]&& bool) {
                        count++
                        bool = false
                    }
                }
            }
            bool = true
        }
        var len = s2.length.toFloat()
        var len2 = s1.length.toFloat()
        var l = 0.0.toFloat()
        if (len2 > len) {
            l = len2
        } else {
            l = len
        }
        var c = count.toFloat()
        if (c / l >= .75) {
            return true
        } else {
            return false
        }
    }

    fun getFile(): kotlin.collections.ArrayList<String> {
        var list = ArrayList<String>()
        var inputStream: InputStream? = null
        inputStream = getApplicationContext().getAssets().open("fortune500.txt")
        var br = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var sb = StringBuilder()
        while (br.readLine().also { line = it } != null) {
            if (line == " ") {
                break
            }
            if (line == "") {
                break
            }
            list.add(line)
        }
        return list
    }

    fun parse(target: String): kotlin.collections.ArrayList<String> {
        var list = ArrayList<String>()
        list.add(target)
        var a = target
        while (a!=""&& a[a.length - 1] == ' ') {
            a = a.substring(0, a.length - 1)
        }
        while (a!="" && a[0] == ' '){
            a = a.substring(1, a.length)
        }
        list.add(a)
        val indSpace = a.indexOf(" ")
        if (indSpace != -1) {
            var i = a.indexOf("Inc")
            var j = a.indexOf("inc")

            if (i != -1) {
                if (a.length > i + 3) {
                    if (!((a[i + 3].toInt() >= 65 && a[i + 3].toInt() <= 90) || (a[i + 3].toInt() >= 97 && a[i + 3].toInt() <= 122))) {
                        a = a.substring(0, i)
                    }
                } else {
                    a = a.substring(0, i)
                }
            } else if(j!=-1){
                if (a.length > i + 3) {
                    if (!((a[i + 3].toInt() >= 65 && a[i + 3].toInt() <= 90) || (a[i + 3].toInt() >= 97 && a[i + 3].toInt() <= 122))) {
                        a = a.substring(0, i)
                    }
                } else {
                    a = a.substring(0, i)
                }
            }
        }
        list.add(a)
        if (indSpace != -1) {
            var i = a.indexOf("com")
            var j = a.indexOf("Com")

            if (i != -1) {
                if (a.length > i + 3) {
                    if (!((a[i + 3].toInt() >= 65 && a[i + 3].toInt() <= 90) || (a[i + 3].toInt() >= 97 && a[i + 3].toInt() <= 122))) {
                        a = a.substring(0, i)
                    }
                } else {
                    a = a.substring(0, i)
                }
            } else if(j!=-1){
                if (a.length > i + 3) {
                    if (!((a[i + 3].toInt() >= 65 && a[i + 3].toInt() <= 90) || (a[i + 3].toInt() >= 97 && a[i + 3].toInt() <= 122))) {
                        a = a.substring(0, i)
                    }
                } else {
                    a = a.substring(0, i)
                }
            }
        }
        list.add(a)
        var ind = a.indexOf(".")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf(".")

        }
        ind = a.indexOf(",")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf(",")

        }
        ind = a.indexOf("/")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("/")

        }
        ind = a.indexOf("@")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("@")

        }
        ind = a.indexOf("#")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("#")

        }
        ind = a.indexOf("$")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("$")

        }
        ind = a.indexOf("%")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("%")
        }
        ind = a.indexOf("^")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("^")
        }
        /*ind = a.indexOf("&")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("&")
        }*/
        ind = a.indexOf("*")
        while (ind != -1) {
            a = a.substring(0, ind) + " " + a.substring(ind + 1, a.length)
            ind = a.indexOf("*")
        }
        while (a!="" && a[a.length - 1] == ' ') {
            a = a.substring(0, a.length - 1)
            if(a.length==0){
                break
            }
        }
        while (a!="" && a[0] == ' ') {
            a = a.substring(1, a.length)
        }

        var targets = ArrayList<String>()
        targets.add(a)
        //targets.add(a)
        var arr = a.split(" ")
        var str = ""
        for (i in 0 until arr.size) {
            if (arr[i] != "") {
                //targets.add(arr[i])
                if (i == 0) {
                    str = arr[i]
                } else {
                    str = str + " " + arr[i]
                    //targets.add(str)
                }
            }
        }
        targets.add(str)
        var d = str
        var e = ""
        for(i in 0 until d.length){
            var b = d[i].toUpperCase()
            e = e + b
        }
        list.add(e)
        for(i in 0 until d.length){
            var b = d[i].toLowerCase()
            e = e + b
        }
        list.add(e)
        for (i in 0 until targets.size) {
            a = targets.get(i)
            while (a!="" && a[a.length - 1] == ' ') {
                a = a.substring(0, a.length - 1)
                if(a.length==0){
                    break
                }
            }
            while (a!="" && a[0] == ' ') {
                a = a.substring(1, a.length)
            }
            var c = ""
            if (a!="" &&!(a[0].toInt() >= 65 && a[0].toInt() <= 90)) {
                var b = a[0].toUpperCase()
                c = b + a.substring(1, a.length)
            } else if (a!="" &&(a[0].toInt() >= 65 && a[0].toInt() <= 90)) {
                var b = a[0].toLowerCase()
                c = b + a.substring(1, a.length)
            }
            list.add(c)
            list.add(a)
            for (i in 0 until a.length) {
                if (i > 0 && (a[i].toInt() >= 65 && a[i].toInt() <= 90)) {
                    var b = a[i].toLowerCase()
                    a = a.substring(0, i) + b + a.substring(i + 1, a.length)
                }
            }
            for (i in 0 until c.length) {
                if (i > 0 && (c[i].toInt() >= 65 && c[i].toInt() <= 90)) {
                    var b = a[i].toLowerCase()
                    c = c.substring(0, i) + b + c.substring(i + 1, c.length)
                }
            }
            list.add(c)
            list.add(a)
            if(indSpace !=- 1) {
                var start = indSpace
                for (i in indSpace + 1 until a.length) {
                    var b = a[i].toUpperCase()
                    a = a.substring(0, i) + b + a.substring(i + 1, a.length)
                }
                for (i in indSpace + 1 until c.length) {
                    var b = c[i].toUpperCase()
                    a = a.substring(0, i) + b + a.substring(i + 1, a.length)
                }
                list.add(c)
                list.add(a)

            }
        }

        return list
    }

    fun search(list: ArrayList<String>, target: String): Array<String> {
        var str = parse(target)
        for (i in 0 until str.size) {
            if(str.get(i)!=""){
                var a = searchHelper(list, str.get(i))
                if (a[0] != "") {
                    return a
                }
            }
        }

        for (i in 0 until str.size) {
            if(str.get(i)!=""){
                var a = didYouMeanSearch(list, str.get(i))
                if (a[0] != "") {
                    return a
                }
            }
        }
        var last = lastResort(str)
        for (i in 0 until last.size) {

            var a = arrayOf("","","")
            if(last.get(i).length > 1) {
                var a = searchHelper(list,last.get(i))
            }else{
                a[0] = ""
            }

            if (a[0] != "") {
                return a
            }
        }
        for (i in 0 until last.size) {
            var a = arrayOf("","","")
            if(last.get(i).length>1) {
                a = didYouMeanSearch(list, last.get(i))
            }else{
                a[0]=""
            }

            if (a[0] != "") {
                return a
            }
        }
        for (i in 0 until str.size) {
            var a = arrayOf("","","")
            if(str.get(i).length>1){
                a = dataReductionSearch(list, str.get(i))
            }else{
                a[0]=""
            }
            if (a[0] != "") {
                return a
            }
        }
        for (i in 0 until last.size) {
            var a = arrayOf("","","")
            if(last.get(i).length>1) {
                a = dataReductionSearch(list, last.get(i))
            }else{
                a[0]=""
            }
            if (a[0] != "") {
                return a
            }
        }
        return arrayOf("","","")
    }
    fun dataReduction(target:String, data:String ): Boolean{
        var arr = data.split(" ")
        var str = ""
        var comp = ArrayList<String>()
        var bool = false
        for (i in 0 until arr.size) {
            if(str == ""){
                str = arr[i]
            }else{
                str = str + " " + arr[i]
            }
            comp.add(str)
        }
        for (i in 0 until comp.size){
            if(comp.get(i) == target || didYouMean(target,comp.get(i)) == true){
                bool = true
            }
        }
        return bool
    }
    fun lastResort(str:ArrayList<String>):ArrayList<String>{
        var list = ArrayList<String>()
        for (i in 0 until str.size){
            var s = ArrayList<String>()
            var arr = str.get(str.size - i - 1).split(" ")
            var t = ""
            var q = ""
            for (i in 0 until arr.size) {
                if(t == ""){
                    t = arr[i]
                }else{
                    t = t + " " + arr[i]
                }
                if(t!="" && t!=" "){
                    s.add(t)
                }
            }
            for (i in 0 until s.size) {
                list.add(s.get(s.size - i - 1))
            }
            for (i in 0 until arr.size) {
                if(arr[i]!="" && arr[i]!=" "){
                    list.add(arr[i])
                }
            }
        }
        return list
    }
    fun dataReductionSearch(list: ArrayList<String>, target: String): Array<String> {
        val sz = list.size
        var iterate = 0
        for (i in 0 until sz) {
            iterate = i
            val l = list.get(i)
            var parts = l.split("/")
            /*if(parts[1] == target){
                return "Fortune 500 ranking is: " + parts[0] + "\n" +
                        "Affiliated brands/products: " + parts[2] + "\n" + parts[3]
            }*/
            var check = dataReduction(target, parts[1])
            if (check == true) {
                val arr = arrayOf(parts[1],iterate.toString(),"false")
                return arr
                /*return "Did you mean: " + parts[1] + "?" + "\n" +
                        "Fortune 500 ranking is: " + parts[0] + "\n" +
                        "Affiliated brands/products: " + parts[2] + "\n" + parts[3]*/
            }
            val sp = parts[2]
            var brands = sp.split(",")
            val len = brands.size
            for (i in 0 until len) {
                var str = brands[i]
                if (str[0] == ' ') {
                    str = str.substring(1, str.length)
                }
                /*if(str==target){
                    return str + " is owned by " + parts[1] + "\n" +
                            parts[1] + " is number " + parts[0] + " in the fortune 500 rankings " + "\n" +
                            "here is a description of the company " + "\n" + parts[3]
                }*/
                var check = dataReduction(target, str)
                if (check == true) {
                    val arr = arrayOf(str,iterate.toString(),"true")
                    return arr
                    /*return "Did you mean: " + str + "?" + "\n" +
                            str + " is owned by " + parts[1] + "\n" +
                            parts[1] + " is number " + parts[0] + " in the fortune 500 rankings " + "\n" +
                            "here is a description of the company " + "\n" + parts[3] */
                }
            }
        }

        return arrayOf("","","")

    }
    fun didYouMeanSearch(list: ArrayList<String>, target: String): Array<String> {
        val sz = list.size
        var iterate = 0
        for (i in 0 until sz) {
            iterate = i
            val l = list.get(i)
            var parts = l.split("/")
            /*if(parts[1] == target){
                return "Fortune 500 ranking is: " + parts[0] + "\n" +
                        "Affiliated brands/products: " + parts[2] + "\n" + parts[3]
            }*/
            var check = didYouMean(target, parts[1])
            if (check == true) {
                val arr = arrayOf(parts[1],iterate.toString(),"false")
                return arr
                /*return "Did you mean: " + parts[1] + "?" + "\n" +
                        "Fortune 500 ranking is: " + parts[0] + "\n" +
                        "Affiliated brands/products: " + parts[2] + "\n" + parts[3]*/
            }
            val sp = parts[2]
            var brands = sp.split(",")
            val len = brands.size
            for (i in 0 until len) {
                var str = brands[i]
                if (str[0] == ' ') {
                    str = str.substring(1, str.length)
                }
                /*if(str==target){
                    return str + " is owned by " + parts[1] + "\n" +
                            parts[1] + " is number " + parts[0] + " in the fortune 500 rankings " + "\n" +
                            "here is a description of the company " + "\n" + parts[3]
                }*/
                var check = didYouMean(target, str)
                if (check == true) {
                    val arr = arrayOf(str,iterate.toString(),"true")
                    return arr
                    /*return "Did you mean: " + str + "?" + "\n" +
                            str + " is owned by " + parts[1] + "\n" +
                            parts[1] + " is number " + parts[0] + " in the fortune 500 rankings " + "\n" +
                            "here is a description of the company " + "\n" + parts[3] */
                }
            }
        }

        return arrayOf("","","")

    }

    fun searchHelper(list: ArrayList<String>, target: String): Array<String> {
        val sz = list.size
        var iterate = 0
        for (i in 0 until sz) {
            iterate = i
            val l = list.get(i)
            var parts = l.split("/")
            if (parts[1] == target) {
                val arr = arrayOf(parts[1],iterate.toString(),"false")
                return arr
            }
            val sp = parts[2]
            var brands = sp.split(",")
            val len = brands.size
            for (i in 0 until len) {
                var str = brands[i]
                if (str[0] == ' ') {
                    str = str.substring(1, str.length)
                }
                if (str == target) {
                    val arr = arrayOf(str,iterate.toString(), "true")
                    return arr

                }

            }
        }
        return arrayOf("","","")
    }

}