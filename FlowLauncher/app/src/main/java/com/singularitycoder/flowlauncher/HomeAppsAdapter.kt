package com.singularitycoder.flowlauncher

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.flowlauncher.databinding.ListItemAppBinding

class HomeAppsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var homeAppList = mutableListOf<App>()
    private var itemClickListener: (app: App, position: Int) -> Unit = { app, position -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeAppViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HomeAppViewHolder).setData(homeAppList[position])
    }

    override fun getItemCount(): Int = homeAppList.size

    override fun getItemViewType(position: Int): Int = position

    fun setItemClickListener(listener: (app: App, position: Int) -> Unit) {
        itemClickListener = listener
    }

    inner class HomeAppViewHolder(
        private val itemBinding: ListItemAppBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(app: App) {
            itemBinding.apply {
                tvAppName.text = app.title
                val bitmap = app.icon?.toBitmap()?.toGrayscale()
                ivAppIcon.load(bitmap)
                root.setOnClickListener {
                    itemClickListener.invoke(app, bindingAdapterPosition)
                }
            }
        }
    }
}
