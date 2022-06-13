package com.example.mp3player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.databinding.ItemViewBinding
import java.text.SimpleDateFormat

class RecyclerViewAdapter (val context: Context, val musicList: MutableList<Music>?): RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {
    var ALBUM_IMAGE_SIZE = 60

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val music: Music? = musicList?.get(position)
        val dbHelper = DBHelper(context, "musicDB", 1)

        binding.tvArtistIV.text = music?.artist
        binding.tvTitleIV.text = music?.title
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(music?.duration).toString()

        //앨범이미지 가져오기
        val bitmap: Bitmap? = music?.getAlbumImage(context, ALBUM_IMAGE_SIZE)
        //앨범이미지 없으면 기본 이미지 설정
        if(bitmap!=null){
            binding.ivMusicIv.setImageBitmap(bitmap)
        }else{
            binding.ivMusicIv.setImageResource(R.drawable.music)
        }

        when(music?.favorite){
            true -> {
                holder.binding.ivInfoIV.setImageResource(R.drawable.favorite)
            }

            false -> {
                holder.binding.ivInfoIV.setImageResource(R.drawable.favorite_border)
            }
        }

        holder.binding.ivInfoIV.setOnClickListener {
            if(music?.favorite == false){
                binding.ivInfoIV.setImageResource(R.drawable.favorite)
                music?.favorite = true
                dbHelper.updateFavorite(music.id, music.favorite)
                notifyDataSetChanged()
            }else{
                binding.ivInfoIV.setImageResource(R.drawable.favorite_border)
                music?.favorite = false
                dbHelper.updateFavorite(music!!.id, music!!.favorite)
                notifyDataSetChanged()
            }
        }

        //itemView 터치 이벤트
        holder.itemView.setOnClickListener {
            val playList: ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>?
            val intent = Intent(binding.root.context, PlayActivity::class.java)
            intent.putExtra("playList", playList)
            //intent.putExtra("music", music)
            intent.putExtra("position", position)
            binding.root.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = musicList?.size ?: 0

    //ViewHolder 클래스
    class CustomViewHolder(val binding: ItemViewBinding): RecyclerView.ViewHolder(binding.root)
}