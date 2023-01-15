package com.singularitycoder.flowlauncher.home.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.flowlauncher.databinding.ListItemAppBinding
import com.singularitycoder.flowlauncher.helper.onSafeClick
import com.singularitycoder.flowlauncher.home.model.App

class HomeAppsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    // https://stackoverflow.com/questions/12075645/detect-when-baseadapter-notifydatasetchanged-finished
    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataSetChangedCustom(
        completion: () -> Unit
    ) {
        val adapter = this
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                adapter.unregisterAdapterDataObserver(this)
                completion.invoke()
            }
        }
        registerAdapterDataObserver(observer)
        notifyDataSetChanged()
    }

    inner class HomeAppViewHolder(
        private val itemBinding: ListItemAppBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(app: App) {
            itemBinding.apply {
                tvAppName.text = app.title
                ivAppIcon.load(app.iconPath)
                root.onSafeClick {
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
