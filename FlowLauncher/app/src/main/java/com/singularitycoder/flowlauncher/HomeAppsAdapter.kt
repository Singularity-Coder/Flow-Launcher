package com.singularitycoder.flowlauncher

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.flowlauncher.databinding.ListItemAppBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAppsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var homeAppList = mutableListOf<App>()
    private var itemClickListener: (app: App, position: Int) -> Unit = { app, position -> }
    private var itemLongClickListener: (app: App, position: Int) -> Unit = { app, position -> }

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

    fun setItemLongClickListener(listener: (app: App, position: Int) -> Unit) {
        itemLongClickListener = listener
    }

    inner class HomeAppViewHolder(
        private val itemBinding: ListItemAppBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(app: App) {
            itemBinding.apply {
                tvAppName.text = app.title
                CoroutineScope(IO).launch {
                    // Tried adding blue filter. Still needs work
                    val bitmap = app.icon?.toBitmap()?.toGrayscale()?.toBlueScale()
                    withContext(Main) {
                        ivAppIcon.load(bitmap)
                    }
                }
                root.setOnClickListener {
                    itemClickListener.invoke(app, bindingAdapterPosition)
                }
                root.setOnLongClickListener {
                    itemLongClickListener.invoke(app, bindingAdapterPosition)
                    false
                }
            }
        }
    }
}
