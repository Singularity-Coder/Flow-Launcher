package com.singularitycoder.flowlauncher.deviceActivity.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.databinding.ListItemDeviceActivityBinding
import com.singularitycoder.flowlauncher.deviceActivity.model.DeviceActivity
import com.singularitycoder.flowlauncher.helper.clipboard
import com.singularitycoder.flowlauncher.helper.onSafeClick

class DeviceActivityAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var deviceActivityList = listOf<DeviceActivity>()
    private var deleteItemListener: (deviceActivity: DeviceActivity) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemDeviceActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppFlowViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppFlowViewHolder).setData(deviceActivityList[position])
    }

    override fun getItemCount(): Int = deviceActivityList.size

    override fun getItemViewType(position: Int): Int = position

    fun setDeleteListener(deleteListener: (deviceActivity: DeviceActivity) -> Unit) {
        this.deleteItemListener = deleteListener
    }

    inner class AppFlowViewHolder(
        private val itemBinding: ListItemDeviceActivityBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(deviceActivity: DeviceActivity) {
            itemBinding.apply {
                tvDate.isVisible = deviceActivity.isDateShown
                tvDate.text = deviceActivity.date.toString() // format this
                tvDeviceActivityTitle.text = deviceActivity.title
                tvTime.text = deviceActivity.date.toString()
                root.onSafeClick {
                    root.context.clipboard()?.text = deviceActivity.title
                }
                root.setOnLongClickListener {
                    deleteItemListener.invoke(deviceActivity)
                    true
                }
            }
        }
    }
}
