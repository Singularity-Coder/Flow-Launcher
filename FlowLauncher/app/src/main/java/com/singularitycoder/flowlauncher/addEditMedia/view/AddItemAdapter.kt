package com.singularitycoder.flowlauncher.addEditMedia.view

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.load
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.addEditMedia.model.AddItem
import com.singularitycoder.flowlauncher.databinding.ListItemAddBinding
import com.singularitycoder.flowlauncher.helper.constants.AddItemType
import com.singularitycoder.flowlauncher.helper.constants.VideoFormat
import com.singularitycoder.flowlauncher.helper.onCustomLongClick
import com.singularitycoder.flowlauncher.helper.onSafeClick
import com.singularitycoder.flowlauncher.helper.toLowCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemsList = emptyList<AddItem>()
    private var itemClickListener: (addItem: AddItem) -> Unit = {}
    private var itemLongClickListener: (addItem: AddItem) -> Unit = {}
    private var listType: String? = null

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

    fun setItemLongClickListener(listener: (addItem: AddItem) -> Unit) {
        itemLongClickListener = listener
    }

    fun setListType(listType: String?) {
        this.listType = listType
    }

    inner class ItemViewHolder(
        private val itemBinding: ListItemAddBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(item: AddItem) {
            itemBinding.apply {
                tvLink.text = if (listType == AddItemType.YOUTUBE_VIDEO) {
                    "youtube.com/watch?v=${item.link}"
                } else {
                    item.link
                }
                tvTitle.text = item.title
                tvStepNumber.text = bindingAdapterPosition.plus(1).toString()

                root.onSafeClick {
//                    clItemContainer.isVisible = false
//                    cardUpdateParent.isVisible = true
//                    etUpdateLink.setText(item.link)
                    itemClickListener.invoke(item)
                }

                if (listType == AddItemType.GLANCE_IMAGE) {
                    ivGlanceImage.isVisible = true
                    if (VideoFormat.values().map { it.extension.toLowCase() }.contains(item.link.substringAfterLast(".").toLowCase())) {
                        CoroutineScope(IO).launch {
                            val imageLoader = ImageLoader.Builder(root.context)
                                .components {
                                    add(VideoFrameDecoder.Factory())
                                }
                                .build()
                            val imageRequest = ImageRequest.Builder(root.context).data(item.link).build()
                            val drawable = imageLoader.execute(imageRequest).drawable

                            withContext(Dispatchers.Main) {
                                ivGlanceImage.load(drawable, imageLoader) {
                                    videoFrameMillis(1000)
                                    placeholder(R.color.black)
                                    error(R.color.md_red_dark)
                                }
                            }
                        }
                    } else {
                        ivGlanceImage.load(item.link)
                    }
                }

                root.onCustomLongClick {
                    itemLongClickListener.invoke(item)
                }

                ibApproveUpdate.onSafeClick {
                    clItemContainer.isVisible = true
                    cardUpdateParent.isVisible = false
                    item.link = etUpdateLink.text.toString()
                    notifyItemChanged(bindingAdapterPosition)
                }
                ibCancelUpdate.onSafeClick {
                    clItemContainer.isVisible = true
                    cardUpdateParent.isVisible = false
                }
            }
        }
    }
}
