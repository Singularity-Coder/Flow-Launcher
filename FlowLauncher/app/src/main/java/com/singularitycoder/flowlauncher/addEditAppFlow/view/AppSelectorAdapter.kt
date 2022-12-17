package com.singularitycoder.flowlauncher.addEditAppFlow.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.flowlauncher.databinding.ListItemAppBinding
import com.singularitycoder.flowlauncher.databinding.ListItemAppSelectorBinding
import com.singularitycoder.flowlauncher.home.model.App

class AppSelectorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedAppList = listOf<App>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemAppSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppFlowViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppFlowViewHolder).setData(selectedAppList[position])
    }

    override fun getItemCount(): Int = selectedAppList.size

    override fun getItemViewType(position: Int): Int = position

    inner class AppFlowViewHolder(
        private val itemBinding: ListItemAppSelectorBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(app: App) {
            itemBinding.apply {
                tvAppName.text = app.title
                tvPackageName.text = app.packageName
                ivAppIcon.load(app.iconPath)
            }
        }
    }
}
