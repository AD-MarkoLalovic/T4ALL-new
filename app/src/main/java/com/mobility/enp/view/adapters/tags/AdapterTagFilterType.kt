package com.mobility.enp.view.adapters.tags

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tags.TagFilterData
import com.mobility.enp.data.model.api_tags.TagStatus
import com.mobility.enp.databinding.CardTagsTypeBinding

class AdapterTagFilterType(list: ArrayList<TagStatus>, private val context: Context) :
    RecyclerView.Adapter<AdapterTagFilterType.AdapterTagFilterViewHolder>() {

    private val listOfFilterTypes: ArrayList<TagFilterData> = arrayListOf() // initial list
    private val adapterFilteredList: ArrayList<TagFilterData> =
        arrayListOf() // filtered out duplicates

    private lateinit var tagInterface: OnClick
    private var clickedButtonByPosition: Int = 0

    fun setInterface(tagInterface: OnClick) {
        this.tagInterface = tagInterface
    }

    fun triggerClearByPosition(int: Int) {
        this.clickedButtonByPosition = int
        for (i in 0..adapterFilteredList.size) {
            notifyItemChanged(i)
        }
    }

    init {
        val initAllFilter = TagFilterData(-1, context.getString(R.string.all))
        listOfFilterTypes.add(initAllFilter)
        for (tag: TagStatus in list) {
            tag.status?.status?.let {
                val tagType = TagFilterData(it.value, it.text)
                listOfFilterTypes.add(tagType)
            }
        }

        val seenTagValues =
            HashSet<Int>()  // filters all data to only contains one instance of each

        for (tagFilterData in listOfFilterTypes) {
            val tagValue = tagFilterData.tagValue

            if (seenTagValues.add(tagValue)) {  // returns true only if the value has not been added before it filters here
                adapterFilteredList.add(tagFilterData)
            }
        }
    }

    class AdapterTagFilterViewHolder(val binding: CardTagsTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tagData: TagFilterData, position: Int, tagInterface: OnClick) {

            binding.tagStatus.setOnClickListener {
                tagInterface.send(tagData)
                tagInterface.sendClickedPosition(position)
            }

            binding.data = tagData
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterTagFilterViewHolder {
        return AdapterTagFilterViewHolder(
            CardTagsTypeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return adapterFilteredList.size
    }

    override fun onBindViewHolder(holder: AdapterTagFilterViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition

        if (pos == clickedButtonByPosition) {
            holder.binding.tagStatus.background =
                AppCompatResources.getDrawable(context, R.drawable.rounded_status_marked_border)
            holder.binding.tagStatus.setTextColor(context.getColor(R.color.white_smoke))
        } else {
            holder.binding.tagStatus.background = AppCompatResources.getDrawable(
                context,
                R.drawable.rounded_status_unmarked_border_
            )
            holder.binding.tagStatus.setTextColor(context.getColor(R.color.primary_light_dark))
        }

        holder.bind(adapterFilteredList[pos], pos, tagInterface)
    }

    interface OnClick {
        fun send(filterType: TagFilterData)
        fun sendClickedPosition(position: Int)
    }

}