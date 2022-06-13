package com.example.mp3player2

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.DBHelper
import com.example.mp3player.MainActivity
import com.example.mp3player.Music
import com.example.mp3player.databinding.ListViewBinding

class RecyclerViewAdapter2(val context: Context,val option: Int, val searchList: ArrayList<String>?): RecyclerView.Adapter<RecyclerViewAdapter2.CustomViewHolder2>() {
    //데이터베이스 객체화
    val dbHelper = DBHelper(context, "musicDB", 1)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder2 {
        val binding = ListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder2(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder2, position: Int) {
        val binding = holder.binding

        val sortList: MutableList<Music>? = dbHelper.selectSort(option, searchList!![position])
        binding.tvSortName.text = searchList!![position]
        binding.tvCount.text = (sortList?.size ?: 0).toString() + "개"

        binding.ivMove.setOnClickListener {
            (context as MainActivity).changeRecycler(sortList)
        }

        holder.itemView.setOnClickListener {
            (context as MainActivity).changeRecycler(sortList)
        }
    }

    override fun getItemCount(): Int = searchList?.size ?: 0

    class CustomViewHolder2 (val binding: ListViewBinding): RecyclerView.ViewHolder(binding.root)
}