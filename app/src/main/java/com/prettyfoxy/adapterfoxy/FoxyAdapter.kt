package com.prettyfoxy.adapterfoxy

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.prettyfoxy.MainActivity
import com.prettyfoxy.R
import com.prettyfoxy.databinding.FoxyItemBinding
import com.prettyfoxy.datafoxy.FoxyData
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class FoxyAdapter(val listFox : List<FoxyData>, val activity : AppCompatActivity) : RecyclerView.Adapter<FoxyAdapter.FoxyHolder>() {
    class FoxyHolder(val binding: FoxyItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setFoxyItem(foxItem : FoxyData, activity: AppCompatActivity) = with(binding){
            Picasso.get().load(foxItem.imageFoxy).centerCrop().resize(720,1280)
                .into(foxyMainImg, object : Callback {
                    override fun onSuccess() {
                        pFoxBar.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {

                    }

                })

            fbFoxSave.setOnClickListener {
                if(ContextCompat.checkSelfPermission(activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),100)
                } else {
                    val externalStoreState = Environment.getExternalStorageState()
                    if(externalStoreState.equals(Environment.MEDIA_MOUNTED)){
                        try{
                            var storeDirectory = Environment.getExternalStorageDirectory().absolutePath
                            val file = File(storeDirectory,"${UUID.randomUUID()}.jpg")
                            val stream : OutputStream = FileOutputStream(file)
                            val drawable  = ContextCompat.getDrawable(activity.applicationContext,foxItem.imageFoxy)
                            val bitmap = (drawable as BitmapDrawable).bitmap
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
                            stream.flush()
                            stream.close()
                            val snackBar = Snackbar.make((activity as MainActivity).findViewById(android.R.id.content),"Image is saving...",Snackbar.LENGTH_LONG)
                            snackBar.show()
                        }catch(e : Exception){
                            Toast.makeText(activity.applicationContext, "Error occured", Toast.LENGTH_LONG).show()
                        }


                    }
                }
            }
            fbFaveFox.setOnClickListener {
                fbFaveFox.setImageResource(R.drawable.ic_fav_fill)

                val snackBar = Snackbar.make((activity as MainActivity).findViewById(android.R.id.content),"Just download and enjoy!",Snackbar.LENGTH_SHORT)
                snackBar.show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoxyHolder {
       val bindFox = FoxyItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FoxyHolder(bindFox)
    }

    override fun onBindViewHolder(holder: FoxyHolder, position: Int) {
        holder.setFoxyItem(listFox[position],activity)

        if(holder.adapterPosition == 2) {
            holder.binding.fbFoxyArrowAhead.visibility = View.GONE
            holder.binding.fbArrowFoxBack.visibility = View.VISIBLE

        } else if(holder.adapterPosition == 0){
            holder.binding.fbFoxyArrowAhead.visibility = View.VISIBLE
            holder.binding.fbArrowFoxBack.visibility = View.GONE
        }
    }

    override fun getItemCount() = listFox.size


}