package com.example.mp3player2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.Toast
import com.example.mp3player.DBHelper
import com.example.mp3player.MainActivity
import com.example.mp3player.Music
import com.example.mp3player.R
import com.example.mp3player.databinding.DetailDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class DetailDialog(val context: Context, val music: Music, val position: Int) {
    private val dialog = BottomSheetDialog(context)

    //사용자 다이얼로그창 띄우기
    @SuppressLint("ResourceType")
    fun showDialog(){
        //detail_dialog의 화면 가져오기
        val binding = DetailDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.show()

        //다이얼로그 항목과 음악 정보 매핑
        //앨범이미지 가져오기
        val bitmap: Bitmap? = music?.getAlbumImage(context, 60)
        //앨범이미지가 없으면 기본 이미지 설정
        if(bitmap != null) binding.ivMusicDetail.setImageBitmap(bitmap)
        else binding.ivMusicDetail.setImageResource(R.drawable.music)

        binding.tvTitleDetail.text = music.title
        binding.tvArtistDetail.text = music.artist
        binding.tvAlbumName.text = music.album

        //곡삭제 이벤트 등록
        binding.tvDelete.setOnClickListener {
            //db에서 삭제
            val dbHelper = DBHelper(context, "musicDB", 1)
            if(dbHelper.deleteMusic(music.id)){
                Toast.makeText(context, "곡삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else Toast.makeText(context, "곡삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()

            context.startActivity(Intent(context, MainActivity::class.java))
        }

        //취소 이벤트 등록
        binding.tvReturn.setOnClickListener {
            dialog.dismiss()
        }
    }
}