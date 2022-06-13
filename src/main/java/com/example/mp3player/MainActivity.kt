package com.example.mp3player

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3player.databinding.ActivityMainBinding
import com.example.mp3player2.RecyclerViewAdapter2
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    var musicList: MutableList<Music>? = mutableListOf()
    var searchMusicList: MutableList<Music>? = mutableListOf()

    //승인받을 항목 퍼미션: Manifest에 설정한 퍼미션, 확장을 위해 Array
    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val REQUEST_READ = 100

    //데이터베이스 객체화
    val dbHelper: DBHelper by lazy { DBHelper(this, "musicDB", 1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        //navigationView 이벤트 등록
        binding.navigationView.setNavigationItemSelectedListener(this)

        //승인될 때까지 요청
        if(isPermitted()){
            //실행: 외부파일 가져와 컬랙션프레임워크 저장, 어뎁터 호출
            startProcess()
        }else{
            //승인 재요청: 승인시 콜백함수(onRequestPermissionsResult)로 결과값 전달
            ActivityCompat.requestPermissions(this, permission, REQUEST_READ)
        }

        //검색 설정
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            //택스트 입력시
            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrBlank()){
                    musicList?.clear()
                    searchMusicList = dbHelper.selectSearch(newText)
                    binding.recyclerView.adapter = RecyclerViewAdapter(this@MainActivity, searchMusicList)
                }else{
                    binding.recyclerView.adapter = RecyclerViewAdapter(this@MainActivity, musicList)
                    searchMusicList?.clear()
                }

                return true
            }
        })
    }

    //퍼미션 승인 결과값을 받는 콜백함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //요청코드 확인
        if(requestCode==REQUEST_READ){
            //permission: 승인한 권한의 이름
            //grantResults : 승인한 권한의 정수값
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) startProcess()
            else{
                Toast.makeText(this, "승인 거절", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //옵션메뉴 이벤트 설정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //업버튼 클릭시 메뉴 오픈
            android.R.id.home -> binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    //navigation menu 이벤트 설정
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var option = Int.MAX_VALUE
        val FAVORITE = 0
        val ARTIST = 1
        val ALBUM = 2
        when(item.itemId){
            //재생목록 돌아가기
            R.id.listAll -> {
                binding.searchView.visibility = View.VISIBLE
                musicList = dbHelper.sellectAll()
                binding.recyclerView.adapter = RecyclerViewAdapter(this, musicList)
                binding.toolbar.title = "재생목록"
            }
            //선호항목 목록
            R.id.preference -> {
                option = FAVORITE
                binding.searchView.visibility = View.GONE
                val musicList: MutableList<Music>? = dbHelper.selectSort(option, "true")
                binding.recyclerView.adapter = RecyclerViewAdapter(this, musicList)
                binding.toolbar.title = "선호 목록"
            }
            //가수명 분류 목록
            R.id.artistList -> {
                option = ARTIST
                binding.searchView.visibility = View.GONE
                val searchList = groupSort(option, musicList)
                binding.recyclerView.adapter = RecyclerViewAdapter2(this@MainActivity, option, searchList)
                binding.toolbar.title = "아티스트별 분류 목록"
            }
            //앨범명 분류 목록
            R.id.albumList -> {
                option = ALBUM
                binding.searchView.visibility = View.GONE
                val searchList = groupSort(option, musicList)
                binding.recyclerView.adapter = RecyclerViewAdapter2(this@MainActivity, option, searchList)
                binding.toolbar.title = "앨범별 분류 목록"
            }
            //메뉴 닫기
            R.id.menuExit -> binding.drawerLayout.closeDrawers()
        }

        binding.drawerLayout.closeDrawers()
        return true
    }

    //중복값 제거한 가수명, 앨범명 전달
    fun groupSort(option: Int, musicList: MutableList<Music>?): ArrayList<String>? {
        var filteredList: List<Music>? = null
        var searchList: ArrayList<String> = arrayListOf()
        val ARTIST = 1
        val ALBUM = 2

        when(option){
            //중복 제거한 가수명 전달
            ARTIST->{
                filteredList = musicList.run {
                    this?.distinctBy { it.artist }
                }

                for(i in 0 until filteredList?.size!!){
                    filteredList[i].artist?.let { searchList.add(it) }
                }
            }
            //중복 제거한 앨범명 전달
            ALBUM->{
                filteredList = musicList.run {
                    this?.distinctBy { it.album }
                }

                for(i in 0 until filteredList?.size!!){
                    filteredList[i].album?.let { searchList.add(it) }
                }
            }
        }

        //Log.d("Hwang", "groupSort: ${searchList!![0]}, ${searchList?.size}")
        return searchList
    }

    //recyclerViewAdapter 변경
    fun changeRecycler(musicList: MutableList<Music>?){
        binding.recyclerView.adapter = RecyclerViewAdapter(this, musicList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    //외부파일 접근 승인 요청
    fun isPermitted(): Boolean {
        return ContextCompat.checkSelfPermission(this, permission[0]) == PackageManager.PERMISSION_GRANTED
    }

    //외부파일의 음악정보 가져와 recyclerView 전달
    private fun startProcess() {
        //음악 정보 추출
        //DB 데이터 유무 확인: 있으면 가져오고 없으면 DB에 추가
        musicList= dbHelper.sellectAll()

        //DB 저장, 중복확인: id(기본키)
        //음악이 없는 경우 음악정보 가져와 DB에 추가
        if(musicList == null || musicList!!.size<=0){
            musicList = getMusic()
            for(i in 0 until musicList!!.size){
                val music = musicList!![i]
                if(!dbHelper.insertMusic(music)){
                    Log.d("Hwang", "insert error ${music.toString()}")
                }
            }
        }

        //어뎁터 객체화
        recyclerViewAdapter = RecyclerViewAdapter(this, musicList)
        binding.recyclerView.adapter = recyclerViewAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    //음악 정보 추출
    private fun getMusic(): MutableList<Music>? {
        //음악 저장 주소
        val uriList = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        //음악 정보 저장
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,     //음악 id
            MediaStore.Audio.Media.TITLE,   //음악 title
            MediaStore.Audio.Media.ARTIST,  //가수
            MediaStore.Audio.Media.ALBUM_ID,//음악이미지
            MediaStore.Audio.Media.ALBUM,   //앨범
            MediaStore.Audio.Media.DURATION, //재생시간
        )

        //contentResolver에 Uri, 음원정보컬럼 쿼리 요청, 결과값 반환(cursor)
        val cursor = contentResolver.query(uriList, projection, null, null, null)
        val musicList: MutableList<Music>? = mutableListOf()

        while(cursor?.moveToNext()==true){
            val id = cursor.getString(0)
            val title = cursor.getString(1).replace("'", "")
            val artist = cursor.getString(2).replace("'", "")
            val albumId = cursor.getString(3)
            val album = cursor.getString(4).replace("'", "")
            val duration = cursor.getLong(5)

            musicList?.add(Music(id, title, artist, albumId, album, duration, false))
        }
        cursor?.close()

        return musicList
    }
}