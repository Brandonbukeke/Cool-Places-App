package com.example.coolplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.coolplaces.models.CoolPlaceModel

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CoolPlacesDatabase"
        private const val TABLE_COOL_PLACE = "CoolPlacesTable"


        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_COOL_PLACE_TABLE = ("CREATE TABLE " + TABLE_COOL_PLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_COOL_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_COOL_PLACE")
        onCreate(db)
    }


    fun addCoolPlace(coolPlace: CoolPlaceModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, coolPlace.title)
        contentValues.put(KEY_IMAGE, coolPlace.image)
        contentValues.put(
            KEY_DESCRIPTION,
            coolPlace.description
        )
        contentValues.put(KEY_DATE, coolPlace.date)
        contentValues.put(KEY_LOCATION, coolPlace.location)
        contentValues.put(KEY_LATITUDE, coolPlace.latitude)
        contentValues.put(KEY_LONGITUDE, coolPlace.longitude)


        val result = db.insert(TABLE_COOL_PLACE, null, contentValues)


        db.close()
        return result
    }
    fun updateCoolPlace(coolPlace: CoolPlaceModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, coolPlace.title)
        contentValues.put(KEY_IMAGE, coolPlace.image)
        contentValues.put(
            KEY_DESCRIPTION,
            coolPlace.description
        )
        contentValues.put(KEY_DATE, coolPlace.date)
        contentValues.put(KEY_LOCATION, coolPlace.location)
        contentValues.put(KEY_LATITUDE, coolPlace.latitude)
        contentValues.put(KEY_LONGITUDE, coolPlace.longitude)


        val success = db.update(TABLE_COOL_PLACE, contentValues, KEY_ID + "=" +coolPlace.id, null)


        db.close()
        return success
    }

    fun deleteCoolPlace(coolPlace: CoolPlaceModel) : Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_COOL_PLACE, KEY_ID + "=" + coolPlace.id, null)
        db.close()
        return success
    }

    fun getCoolPlacesList():ArrayList<CoolPlaceModel>{
        val coolPlaceList = ArrayList<CoolPlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_COOL_PLACE"
        val db =this.readableDatabase
        try {
            val cursor : Cursor = db.rawQuery(selectQuery,null)
            if (cursor.moveToFirst()){
                do {
                    val place = CoolPlaceModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    coolPlaceList.add(place)

                }while (cursor.moveToNext())
            }
            cursor.close()

        }catch (e:SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return coolPlaceList
    }

}