package com.example.mp3player

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.mp3player.databinding.ActivityPlayBinding
import com.example.mp3player2.DetailDialog
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlayActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayBinding

    //멤버변수: 뮤직 플레이어
    private var mediaPlayer: MediaPlayer? = null

    //재생목록
    private var playList: ArrayList<Parcelable>? = null

    //전달값 위치
    private var position: Int = 0

    //멤버변수: 음악정보 저장
    private var music: Music? = null


    //앨범이미지 사이즈 정의
    private  val ALBUM_IMAGE_SIZE = 300
    //멤버변수: 코루틴 실행
    private var playerJob: Job? = null
    val dbHelper = DBHelper(this, "musicDB", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //음원 정보 가져오기(recyclerViewAdapter)
        //music = intent.getSerializableExtra("music") as Music
        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position", 0)
        music = playList!![position] as Music

        when(music?.favorite){
            true -> {
                binding.ivLike.setImageResource(R.drawable.favorite)
            }

            false -> {
                binding.ivLike.setImageResource(R.drawable.favorite_border)
            }
        }

        startMusic(music)
    }

    fun startMusic(music: Music?){
        if(music!=null){
            //음악 정보 전달: View 설정
            binding.tvTitle.text = music?.title
            binding.tvArtist.text = music?.artist
            binding.tvStart.text = "00:00"
            binding.tvEnd.text = SimpleDateFormat("mm:ss").format(music?.duration)

            val bitmap: Bitmap? = music?.getAlbumImage(this, ALBUM_IMAGE_SIZE)
            if(bitmap!=null){
                binding.ivMusic.setImageBitmap(bitmap)
            }else{
                binding.ivMusic.setImageResource(R.drawable.music)
            }

            //음악 등록
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.max = music?.duration!!.toInt()

            //seekBar 이벤트 등록: 음악 재생과 동기화
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                //seekBar 터치하여 이동시 발생하는 이벤트
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        mediaPlayer?.seekTo(progress)
                        binding.tvStart.text = SimpleDateFormat("mm:ss").format(progress)
                    }
                }
                //seekBar 터치시 발생하는 이벤트
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }
                //seekBar 터치 해제시 발생하는 이벤트
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }//end of if
    }

    fun onClickView(view: View){
        when(view?.id){
            R.id.ivList -> {
                //음악 정지
                if (mediaPlayer != null) mediaPlayer?.stop()
                //코르틴 해제
                if (playerJob != null) playerJob?.cancel()
                finish()
            }
            R.id.ivPlay -> {
                if(mediaPlayer?.isPlaying == true){
                    mediaPlayer?.pause()
                    binding.ivPlay.setImageResource(R.drawable.play)
                }else{
                    mediaPlayer?.start()
                    binding.ivPlay.setImageResource(R.drawable.pause)

                    //코루틴 처리: 음악실행하는 동안 seekBar, 시작시간 진행
                    val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
                    playerJob = backgroundScope.launch {
                        //음악 재생 현황을 seekBar, 시작시간값에 전달
                        if(mediaPlayer != null){
                            while(mediaPlayer?.isPlaying == true){
                                //사용자가 만든 스레드에서 화면에 뷰값 변경시 실행불가
                                //스레드 안의 뷰값 변경 필요시 runOnUiThread 사용
                                runOnUiThread {
                                    //현위치 전달
                                    var currentPosition = mediaPlayer?.currentPosition!!
                                    binding.seekBar.progress = currentPosition
                                    binding.tvStart.text = SimpleDateFormat("mm:ss").format(currentPosition)
                                }
                                try {
                                    delay(500)
                                }catch (e: Exception){
                                    Log.d("Hwang", "delay error: ${e.printStackTrace()}")
                                }
                            }//end of while
                            runOnUiThread{
                                if(binding.seekBar.progress >= binding.seekBar.max-1000) {
                                    binding.seekBar.progress = 0
                                    binding.tvStart.text = "00:00"
                                }
                                binding.ivPlay.setImageResource(R.drawable.play)
                            }
                        }
                    }
                }
            }
            R.id.ivInfo -> {
                val detailDialog: DetailDialog
                if(music != null) {
                    detailDialog = DetailDialog(this, music!!, position)
                    detailDialog.showDialog()
                }
            }
            R.id.ivPrevious -> {
                //음악 정지
                if (mediaPlayer != null) mediaPlayer?.stop()
                //코르틴 해제
                if (playerJob != null) playerJob?.cancel()

                position = position - 1
                if(position < 0){
                    position =  playList!!.size - 1
                }

                music = playList?.get(position) as Music
                startMusic(music)
            }
            R.id.ivNext -> {
                //음악 정지
                if (mediaPlayer != null) mediaPlayer?.stop()
                //코르틴 해제
                if (playerJob != null) playerJob?.cancel()

                position = position + 1
                if(position > playList!!.size - 1){
                    position = 0
                }
                music = playList?.get(position) as Music
                startMusic(music)
            }
            R.id.ivLike -> {
                if(music?.favorite == false){
                    binding.ivLike.setImageResource(R.drawable.favorite)
                    music?.favorite = true
                    dbHelper.updateFavorite(music!!.id, music!!.favorite)
                }else{
                    binding.ivLike.setImageResource(R.drawable.favorite_border)
                    music?.favorite = false
                    dbHelper.updateFavorite(music!!.id, music!!.favorite)
                }

            }
        }
    }
}