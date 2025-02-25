package com.mobility.enp.view.adapters.home

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemRelationHomeBinding
import com.mobility.enp.view.ui_models.home.HomeTollHistoryUI

class HomePassageAdapter : ListAdapter<HomeTollHistoryUI, HomePassageAdapter.RelationViewHolder>(
    HomeTollHistoryDiffCallback()
) {

    inner class RelationViewHolder(private val binding: ItemRelationHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: HomeTollHistoryUI) {
            binding.relation = relation

            val context = binding.root.context
            when (relation.status) {
                1 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_green)
                    binding.relationItemHome.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.figmaToolHistoryPaidBackground
                        )
                    )
                    binding.relationItemHome.strokeColor =
                        ContextCompat.getColor(context, R.color.figmaToolHistoryPaidBackground)
                }

                7 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_orange)
                    binding.relationItemHome.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.primary_light_orange
                        )
                    )
                    binding.relationItemHome.strokeColor =
                        ContextCompat.getColor(context, R.color.primary_light_orange)
                }

                3 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_red)
                    binding.relationItemHome.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.figmaToolHistoryUnpaidBackground
                        )
                    )
                    binding.relationItemHome.strokeColor =
                        ContextCompat.getColor(context, R.color.figmaToolHistoryUnpaidBackground)

                }
            }


            /*val titleEntry = binding.passageRelationEntry.text.isEmpty()
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
            }*/

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRelationHomeBinding.inflate(layoutInflater, parent, false)

        return RelationViewHolder(binding)
    }

    override fun getItemCount() = currentList.size

    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentRelation = getItem(position)
        holder.bind(currentRelation)
    }

    class HomeTollHistoryDiffCallback : DiffUtil.ItemCallback<HomeTollHistoryUI>() {
        override fun areItemsTheSame(
            oldItem: HomeTollHistoryUI,
            newItem: HomeTollHistoryUI
        ): Boolean {
            return oldItem.invoiceNumber == newItem.invoiceNumber
        }

        override fun areContentsTheSame(
            oldItem: HomeTollHistoryUI,
            newItem: HomeTollHistoryUI
        ): Boolean {
            return oldItem == newItem
        }

    }

}

