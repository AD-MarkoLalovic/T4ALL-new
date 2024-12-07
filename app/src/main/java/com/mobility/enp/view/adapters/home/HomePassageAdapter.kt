package com.mobility.enp.view.adapters.home

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.homedata.TollHistory
import com.mobility.enp.databinding.ItemRelationHomeBinding

class HomePassageAdapter(
    context: Context, private val relation: ArrayList<TollHistory> = arrayListOf()
) : RecyclerView.Adapter<HomePassageAdapter.RelationViewHolder>() {

    private var serial: String = ""
    private var context: Context

    init {
        this.context = context
    }

    inner class RelationViewHolder(private val binding: ItemRelationHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: TollHistory, serial: String) {
            binding.relation = relation
            binding.tagSerial = serial
            binding.executePendingBindings()

            when (relation.status.value) {
                1 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_green)

                    if (isTabletXml(binding.root.context)) {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_green_tablet)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_green_tablet)
                    } else {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_green)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_green)
                    }
                }

                7 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_orange)
                    if (isTabletXml(binding.root.context)) {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_orange_tablet)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_orange_tablet)
                    } else {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_orange)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_orange)
                    }
                }

                3 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_red)

                    if (isTabletXml(binding.root.context)) {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_red_tablet)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_red_tablet)
                    } else {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_red)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_red)
                    }
                }
            }


            val titleEntry = binding.passageRelationEntry.text.isEmpty()
            val titleEntryDate = binding.passageEntryDate.text.isEmpty()
            val titleEntryTime = binding.passageEntryTime.text.isEmpty()

            if (titleEntry || titleEntryDate || titleEntryTime) {

                binding.passageRelationEntry.text = binding.passageRelationExit.text
                binding.passageEntryTime.text = binding.passageExitTime.text
                binding.passageEntryDate.text = binding.passageExitDate.text

                binding.passageRelationExit.visibility = View.GONE
                binding.passageExitDate.visibility = View.GONE
                binding.passageExitTime.visibility = View.GONE

            } else {

                binding.passageRelationExit.visibility = View.VISIBLE
                binding.passageExitDate.visibility = View.VISIBLE
                binding.passageExitTime.visibility = View.VISIBLE
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRelationHomeBinding.inflate(layoutInflater, parent, false)

        return RelationViewHolder(binding)
    }

    override fun getItemCount() = relation.size

    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentRelation = relation[position]
        Log.d("test", "onBindViewHolder: $currentRelation")
        holder.bind(currentRelation, serial)
    }


    private fun isTabletXml(context: Context): Boolean {
        val config: Configuration = context.resources.configuration
        val smallestScreenWidthDp: Int = config.smallestScreenWidthDp
        return smallestScreenWidthDp >= 600 // min layout with for tablet
    }

}