package com.example.mp3player

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

class DBHelper(val context: Context, val dbName: String, val version: Int): SQLiteOpenHelper(context, dbName, null, version) {
    //테이블명
    companion object{
        val TABLE_NAME = "musicTBL"
    }

    //DBHelper 생성시 최초 한번만 실행: DB명 부여 시점
    override fun onCreate(db: SQLiteDatabase?) {
        //테이블 설계
        val createQuery ="create table $TABLE_NAME (id TEXT primary key, title TEXT, artist TEXT, albumId TEXT, album TEXT, duration INTEGER, favorite TEXT)"
        db?.execSQL(createQuery)
    }

    //DB 최초 생성 후 버전 변경시 실행
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //테이블 제거
        val dropQuery = "drop table $TABLE_NAME"
        db?.execSQL(dropQuery)
        this.onCreate(db)
    }

    //데이터 삽입
    fun insertMusic(music: Music): Boolean {
        var insertFlag = false
        val insertQuery = "insert into $TABLE_NAME(id, title, artist, albumId, album, duration, favorite) " +
                "values('${music.id}', '${music.title}', '${music.artist}', '${music.albumId}', '${music.album}', ${music.duration}, '${music.favorite}')"

        //db 가져오기: 읽기권한(readableDatabase), 쓰기권한(writableDatabase)
        val db = this.writableDatabase
        try{
            db.execSQL(insertQuery)
            insertFlag = true
        }catch (e: Exception){
            Log.d("Hwang", "insertMusic Error ${e.printStackTrace()}")
        }finally {
            db.close()
        }
        return insertFlag
    }

    //전체 데이터 가져오기
    fun sellectAll(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf()
        val selectQuery = "select* from $TABLE_NAME"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try{
            cursor = db.rawQuery(selectQuery, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val album = cursor.getString(4)
                    val duration = cursor.getLong(5)
                    val favorite = cursor.getString(6).toBoolean()

                    musicList?.add(Music(id, title, artist, albumId, album, duration, favorite))
                }
            }else musicList = null
        }catch (e: Exception){
            Log.d("Hwang", "selectAll Error ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    //검색결과 가져오기
    fun selectSearch(query: String?): MutableList<Music>?{
        var musicList: MutableList<Music>? = mutableListOf()
        var selectQuery = ""
        val db = this.readableDatabase
        var cursor: Cursor? = null

        selectQuery = "select * from $TABLE_NAME where artist like '$query%' or title like '$query%'"

        try {
            cursor = db.rawQuery(selectQuery, null)
            if(cursor.count > 0){
                //db 조회 결과값과 Music 객체 매핑
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist= cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val album = cursor.getString(4)
                    val duration = cursor.getLong(5)
                    val favorite = cursor.getString(6).toBoolean()

                    //리스트에 추가
                    musicList?.add(Music(id, title, artist, albumId, album, duration, favorite))
                }
            }
        }catch (e: Exception){
            Log.d("Hwang", "selectSort error ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    //그룹별 분류 가져오기: 아티스트별, 앨범명별
    fun selectSort(option: Int, query: String?): MutableList<Music>?{
        var sortList: MutableList<Music>? = mutableListOf()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val FAVORITE = 0
        val ARTIST = 1
        val ALBUM = 2

        val selectQuery = when(option){
            FAVORITE->"select * from $TABLE_NAME where favorite = '$query'"
            ARTIST -> "select * from $TABLE_NAME where artist = '$query'"
            ALBUM -> "select * from $TABLE_NAME where album = '$query'"
            else -> {
                Toast.makeText(context, "다시 선택해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("Hwang", "selectSort option error")
                return sortList
            }
        }

        try {
            cursor = db.rawQuery(selectQuery, null)
            if(cursor.count > 0){
                //db 조회 결과값과 Music 객체 매핑
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist= cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val album = cursor.getString(4)
                    val duration = cursor.getLong(5)
                    val favorite = cursor.getString(6).toBoolean()

                    //리스트에 추가
                    sortList?.add(Music(id, title, artist, albumId, album, duration, favorite))
                }
            }
        }catch (e: Exception){
            Log.d("Hwang", "selectSort error ${e.printStackTrace()}")
            sortList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return sortList
    }

    //선호항목 업데이트
    fun updateFavorite (id: String, favorite: Boolean): Boolean {
        var updateFlag = false
        val updateQuery = "update $TABLE_NAME set favorite = '$favorite' where id = '$id'"
        val db = this.writableDatabase

        try {
            db.execSQL(updateQuery)
            updateFlag = true
        }catch (e: Exception){
            Log.d("Hwang", "updateFavorite error ${e.printStackTrace()}")
        }finally {
            db.close()
        }
        return updateFlag
    }

    //곡 삭제
    fun deleteMusic(id: String): Boolean {
        var deleteFlag = false
        val deleteQuery = "delete from $TABLE_NAME where id = '$id'"
        val db = this.writableDatabase
        try {
            db.execSQL(deleteQuery)
            deleteFlag = true
        }catch (e: Exception){
            Log.d("Hwang", "deleteMusic error ${e.printStackTrace()}")
        }finally {
            db.close()
        }
        return deleteFlag
    }
}