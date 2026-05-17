package com.deenzstudios.kalori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.app.Dialog
import com.github.chrisbanes.photoview.PhotoView

class SliderAdapter(private val images: List<Int>) :
    RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.imageSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slide_item, parent, false)

        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {

        holder.imageView.setImageResource(images[position])

        holder.imageView.setOnClickListener {

            val dialog = Dialog(holder.itemView.context)
            dialog.setContentView(R.layout.dialog_image)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val fullImage = dialog.findViewById<PhotoView>(R.id.fullImage)

            fullImage.setImageResource(images[position])

            fullImage.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {

        return images.size
    }
}