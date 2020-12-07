package com.example.whobuiltthat

/*
    This Activity was developed to handle the first screen (adding/deleting/selecting companies)

    This code uses some libraries that are licensed under the Apache 2.0 License, the terms of
    which as well as the tools can be found in the NOTICES file that came with this application.

    The animation, UI, as well as other coding elements implemented by Alex Chill
 */

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition

class MainActivity : AppCompatActivity(), CompanyAdapter.CompanyClickedListener {

    private lateinit var mCompanyList: ArrayList<Company>
    protected lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mAdapter: CompanyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar!!.setDisplayShowTitleEnabled(false)

        mSharedPreferences = getSharedPreferences("companyPreferences", Context.MODE_PRIVATE)

        val previousCompaniesView = findViewById<RecyclerView>(R.id.prev_list)

        // gets references to the buttons
        val addByPictureButton = findViewById<Button>(R.id.add_by_picture_button)
        val addByTextButton = findViewById<Button>(R.id.add_new_button)

        // gets from local storage
        val tempList: ArrayList<Company>? = Gson().fromJson(mSharedPreferences.getString("companies", null), object: TypeToken<ArrayList<Company>>(){}.type)

        if (tempList == null || tempList.size == 0) {
            mCompanyList = ArrayList()
        } else {
            mCompanyList = tempList
        }

        newAdapter(previousCompaniesView)

        // starts the gradient animations
        val layout = findViewById<CoordinatorLayout>(R.id.back_layout)
        val aniDrawable = layout.background as AnimationDrawable

        aniDrawable.setEnterFadeDuration(2000)
        aniDrawable.setExitFadeDuration(3000)

        aniDrawable.start()

        // button listeners
        addByTextButton.setOnClickListener {
            val companyText = findViewById<EditText>(R.id.add_new_text)

            if (companyText.text.isNullOrEmpty()) {
                Toast.makeText(applicationContext, "Please enter a company!", Toast.LENGTH_LONG).show()
            } else {
                val newCompany = Company()
                newCompany.name = companyText.text.toString()
                mCompanyList.add(0, newCompany)

                // saves to local storage
                val edit = mSharedPreferences.edit()
                edit.putString("companies", Gson().toJson(mCompanyList)).apply()

                companyText.setText("")

                mAdapter.notifyDataSetChanged()
            }
        }

        addByPictureButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                // if permission is granted, run the camera preview
                Log.i("testing123", "permission granted!")
                handlePictureInput()
            } else {
                // requests permission for camera access
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        Log.i("testing", "permission granted")
                        handlePictureInput()
                    } else {
                        Toast.makeText(applicationContext, "Camera Permissions needed for this action!", Toast.LENGTH_LONG).show()
                    }
                }.launch(android.Manifest.permission.CAMERA)
            }
        }

    }

    private fun handlePictureInput() {
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            if (it != null) {
                Log.i("testing", "the image wasn't null")
                val inputImage = InputImage.fromBitmap(it, 0)

                val recognize = TextRecognition.getClient().process(inputImage)

                recognize.addOnSuccessListener {foundText ->
                    val newCompany = Company()
                    newCompany.name = foundText.text
                    mCompanyList.add(0, newCompany)

                    // saves to local storage
                    val edit = mSharedPreferences.edit()
                    edit.putString("companies", Gson().toJson(mCompanyList)).apply()

                    mAdapter.notifyDataSetChanged()
                }

                recognize.addOnFailureListener { _ ->
                    Toast.makeText(applicationContext, "Text couldn't be recognized", Toast.LENGTH_LONG).show()
                }
            }
        }.launch(null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete_all) {
            mCompanyList = ArrayList()

            // clears storage
            mSharedPreferences.edit().clear().apply()

            val previousCompaniesView = findViewById<RecyclerView>(R.id.prev_list)
            newAdapter(previousCompaniesView)

            return true
        } else if (item.itemId == R.id.action_delete_recent) {
            mCompanyList.removeAt(0)

            // clears storage
            mSharedPreferences.edit().clear().apply()
            mSharedPreferences.edit().putString("companies", Gson().toJson(mCompanyList)).apply()

            val previousCompaniesView = findViewById<RecyclerView>(R.id.prev_list)
            newAdapter(previousCompaniesView)

            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun newAdapter(view: RecyclerView) {
        mAdapter = CompanyAdapter(mCompanyList, this)
        view.adapter = mAdapter
        view.layoutManager = LinearLayoutManager(this)
    }

    override fun onCompanyClicked(pos: Int) {
        val intent = Intent(this, CurrentCompanyActivity::class.java)
        val companyClicked = mCompanyList[pos]
        intent.putExtra("name", companyClicked.name)

        startActivity(intent)
    }




}