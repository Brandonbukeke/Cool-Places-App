package com.example.coolplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.coolplaces.R
import com.example.coolplaces.models.CoolPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback{
    private var mCoolPlaceDetails: CoolPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mCoolPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as CoolPlaceModel?
        }
        if (mCoolPlaceDetails != null){
            setSupportActionBar(toolbar_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mCoolPlaceDetails!!.title

            toolbar_map.setNavigationOnClickListener {
                onBackPressed()
            }
            val supportMapFragment : SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)


        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(mCoolPlaceDetails!!.latitude, mCoolPlaceDetails!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(mCoolPlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(newLatLngZoom)
    }
}