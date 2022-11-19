package com.singularitycoder.flowlauncher.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.flowlauncher.databinding.ListItemAddBinding

class AddItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemsList = emptyList<AddItem>()
    private var itemClickListener: (addItem: AddItem) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).setData(itemsList[position])
    }

    override fun getItemCount(): Int = itemsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setItemClickListener(listener: (addItem: AddItem) -> Unit) {
        itemClickListener = listener
    }

    inner class ItemViewHolder(
        private val itemBinding: ListItemAddBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(item: AddItem) {
            itemBinding.apply {
                tvLink.text = item.link
                tvTitle.text = item.title
                tvStepNumber.text = bindingAdapterPosition.plus(1).toString()

                root.setOnClickListener {
                    clItemContainer.isVisible = false
                    cardUpdateParent.isVisible = true
                    etUpdateLink.setText(item.link)
                }

                ibApproveUpdate.setOnClickListener {
                    clItemContainer.isVisible = true
                    cardUpdateParent.isVisible = false
                    item.link = etUpdateLink.text.toString()
                    notifyItemChanged(bindingAdapterPosition)
                }
                ibCancelUpdate.setOnClickListener {
                    clItemContainer.isVisible = true
                    cardUpdateParent.isVisible = false
                }
            }
        }
    }
}
