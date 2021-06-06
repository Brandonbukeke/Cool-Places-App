package com.example.coolplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coolplaces.R
import com.example.coolplaces.adapters.CoolPlacesAdapter

import com.example.coolplaces.database.DatabaseHandler
import com.example.coolplaces.models.CoolPlaceModel
import com.example.coolplaces.utils.SwipeToDeleteCallback
import com.example.coolplaces.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddCoolPlace.setOnClickListener {
            val intent = Intent(this, AddCoolPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getCoolPlacesListFromLocalDB()
    }
    private fun setupCoolPlacesRecyclerView(coolPlaceList: ArrayList<CoolPlaceModel>){
        rv_cool_places_list.layoutManager = LinearLayoutManager(this)
        rv_cool_places_list.setHasFixedSize(true)
        val placesAdapter = CoolPlacesAdapter(this,coolPlaceList)
        rv_cool_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object:CoolPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: CoolPlaceModel) {
                val intent = Intent(this@MainActivity,CoolPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_cool_places_list.adapter as CoolPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_cool_places_list)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_cool_places_list.adapter as CoolPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getCoolPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_cool_places_list)

    }

    private fun getCoolPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getCoolPlaceList: ArrayList<CoolPlaceModel> = dbHandler.getCoolPlacesList()

        if (getCoolPlaceList.size > 0){
            rv_cool_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setupCoolPlacesRecyclerView(getCoolPlaceList)

        }else{
            rv_cool_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getCoolPlacesListFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }

    companion object {
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS ="extra_place_details "
    }
}