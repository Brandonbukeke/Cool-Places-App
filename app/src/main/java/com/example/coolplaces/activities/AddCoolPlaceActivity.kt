package com.example.coolplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.coolplaces.R
import com.example.coolplaces.database.DatabaseHandler
import com.example.coolplaces.models.CoolPlaceModel
import com.example.coolplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_cool_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddCoolPlaceActivity : AppCompatActivity(), View.OnClickListener{
    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mCoolPlaceDetails: CoolPlaceModel? = null

    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cool_place)
        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()){
             Places.initialize(this@AddCoolPlaceActivity,resources.getString(R.string.google_maps_api_key) )
        }
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mCoolPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as CoolPlaceModel?
        }


        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if (mCoolPlaceDetails != null){
            supportActionBar?.title ="Edit Cool Place"

            et_title.setText(mCoolPlaceDetails!!.title)
            et_description.setText(mCoolPlaceDetails!!.description)
            et_date.setText(mCoolPlaceDetails!!.date)
            et_location.setText(mCoolPlaceDetails!!.location)
            mLatitude = mCoolPlaceDetails!!.latitude
            mLongitude = mCoolPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mCoolPlaceDetails!!.image)

            iv_place_image.setImageURI(saveImageToInternalStorage)

            btn_save.text = "UPDATE"
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)

        tv_select_current_location.setOnClickListener(this)
    }
    private fun isLocationEnabled(): Boolean{
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallBack, Looper.myLooper())
    }
    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation: Location =locationResult!!.lastLocation
            mLatitude = mLastLocation.latitude
            Log.i("Current Latitude","$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.i("Current Longitude","$mLongitude")

            val addressTask = GetAddressFromLatLng(this@AddCoolPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    et_location.setText(address)
                }

                override fun onError() {
                   Log.e("Get Address:: ", "Something went wrong")
                }

            })
            addressTask.getAddress()
        }
    }



    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(
                    this@AddCoolPlaceActivity,
                    dateSetListener,cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery","Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){
                   _, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save ->{
                when{
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter title",Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter a description",Toast.LENGTH_SHORT).show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter location",Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage ==null ->{
                        Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show()
                    }else ->{

                    val coolPlaceModel = CoolPlaceModel(

                           if (mCoolPlaceDetails == null) 0 else mCoolPlaceDetails!!.id,
                           et_title.text.toString(),
                           saveImageToInternalStorage.toString(),
                           et_description.text.toString(),
                           et_date.text.toString(),
                           et_location.text.toString(),
                           mLatitude,
                           mLongitude

                    )
                    val dbHandler = DatabaseHandler(this)
                    if (mCoolPlaceDetails == null){
                        val addCoolPlace = dbHandler.addCoolPlace(coolPlaceModel)
                        if (addCoolPlace > 0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                    }else{
                        val updateCoolPlace = dbHandler.updateCoolPlace(coolPlaceModel)
                        if (updateCoolPlace > 0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }


                    }
                }

            }
            R.id.et_location ->{
                try {

                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )

                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddCoolPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location ->{
                if (!isLocationEnabled()){
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {

                                requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread()
                    .check()



                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (requestCode == GALLERY){
                if (data != null){
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)
                        saveImageToInternalStorage =
                            saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                        iv_place_image.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddCoolPlaceActivity, "Failed to load image from Gallery",Toast.LENGTH_SHORT).show()
                    }
                }
            }else if (requestCode == CAMERA){
                val thumbNail : Bitmap =data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage =
                    saveImageToInternalStorage(thumbNail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                iv_place_image.setImageBitmap(thumbNail)
            }else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                et_location.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report : MultiplePermissionsReport?)
            {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest> , token : PermissionToken)
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }


    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report : MultiplePermissionsReport?)
            {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest> , token : PermissionToken)
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()

    }
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("" +
                "It looks like you have turned off permissions required for this feature." +
                "It can be enabled under applications settings.")
            .setPositiveButton("GO TO SETTINGS")
            {_, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat,Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream)
            stream.flush()
            stream.close()

        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "CoolPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}