package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.flowlauncher.databinding.ListItemAppBinding
import com.singularitycoder.flowlauncher.home.model.App
import com.singularitycoder.flowlauncher.toBlueFilter
import com.singularitycoder.flowlauncher.toGrayscaleFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectedAppsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var homeAppList = listOf<App>()
    private var itemClickListener: (app: App, position: Int) -> Unit = { app, position -> }
    private var itemLongClickListener: (view: View, app: App, position: Int) -> Unit = { view, app, position -> }

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

    fun setItemLongClickListener(listener: (view: View, app: App, position: Int) -> Unit) {
        itemLongClickListener = listener
    }

    inner class HomeAppViewHolder(
        private val itemBinding: ListItemAppBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(app: App) {
            itemBinding.apply {
                tvAppName.text = app.title
                ivAppIcon.load(app.icon)
                root.setOnClickListener {
                    itemClickListener.invoke(app, bindingAdapterPosition)
                }
                root.setOnLongClickListener {
                    itemLongClickListener.invoke(it, app, bindingAdapterPosition)
                    false
                }
            }
        }
    }
}
