package com.example.mp3player

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
class Music(var id: String, var title: String?, var artist: String?, var albumId: String?, var album: String?, var duration: Long, var favorite: Boolean): Serializable, Parcelable {

    companion object : Parceler<Music> {

        override fun Music.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumId)
            parcel.writeString(album)
            parcel.writeLong(duration)
            parcel.writeByte(if (favorite) 1 else 0)
        }

        override fun create(parcel: Parcel): Music {
            return Music(parcel)
        }
    }

    //생성자
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()) {
    }

    //멤버함수: 컨텐트리졸버를 이용하여 앨범 정보 가져오는 Uri
    fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/"+albumId)
    }

    //멤버함수: 음악정보 가져오는 Uri
    fun getMusicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    //음악 앨범 이미지 비트맵 생성
    fun getAlbumImage(context: Context, imageSize: Int): Bitmap?{
        val contentResolver: ContentResolver = context.getContentResolver()
        val uri = getAlbumUri()
        val option = BitmapFactory.Options()

        if(uri != null){
            var parcelFileDescriptor: ParcelFileDescriptor? = null

            try {
                //외부 파일의 이미지 정보 가져오는 Stream
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                var bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null, option)

                //비트맵 사이즈 결정: 원본이미지 사이즈를 원하는 사이즈로 설정
                if(bitmap != null) {
                    if(option.outHeight !== imageSize || option.outWidth !== imageSize){
                        val tempBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
                        bitmap.recycle()
                        bitmap = tempBitmap
                    }
                }
                return bitmap
            }catch (e: Exception){
                Log.d("Hwang", "getAlbumImage error : ${e.printStackTrace()}")
            }finally {
                try {
                    parcelFileDescriptor?.close()
                }catch (e: Exception){
                    Log.d("Hwang", "parcelFileDescriptor?.close() error : ${e.printStackTrace()}")
                }
            }
        }

        return null
    }


}