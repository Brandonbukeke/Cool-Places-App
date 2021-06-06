package com.example.coolplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.coolplaces.R
import com.example.coolplaces.models.CoolPlaceModel
import kotlinx.android.synthetic.main.activity_cool_place_details.*
import kotlinx.android.synthetic.main.activity_cool_place_details.iv_place_image
import kotlinx.android.synthetic.main.item_cool_place.*

class CoolPlaceDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cool_place_details)

        var coolPlaceDetailsModel : CoolPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            coolPlaceDetailsModel =
                intent.getParcelableExtra(
                    MainActivity.EXTRA_PLACE_DETAILS) as CoolPlaceModel?
        }

        if (coolPlaceDetailsModel != null){
            setSupportActionBar(toolbar_cool_place_details)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = coolPlaceDetailsModel.title

            toolbar_cool_place_details.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_place_image.setImageURI(Uri.parse(coolPlaceDetailsModel.image))
            tv_description.text = coolPlaceDetailsModel.description
            tv_location.text = coolPlaceDetailsModel.location

            btn_view_on_map.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, coolPlaceDetailsModel)
                startActivity(intent)
            }


        }
    }
}