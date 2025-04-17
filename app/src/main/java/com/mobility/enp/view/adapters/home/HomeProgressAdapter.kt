package com.mobility.enp.view.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.databinding.CardProgressBarBinding

class HomeProgressAdapter(val franchiseModel: FranchiseModel?) :
    ListAdapter<Int, HomeProgressAdapter.HomeProgressAdapterViewHolder>(HomeProgressDiffCallback()) {

    var checkedPosition = 0

    inner class HomeProgressAdapterViewHolder(val binding: CardProgressBarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentPosition: Int) {

            var drawable = 0

            franchiseModel?.promotionsDot?.let {
                drawable = if (checkedPosition == currentPosition) it else R.drawable.dot_unchecked
            } ?: run {
                drawable =
                    if (checkedPosition == currentPosition) R.drawable.dot_checked else R.drawable.dot_unchecked
            }

            binding.dot.setImageResource(drawable)
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HomeProgressAdapterViewHolder {
        return HomeProgressAdapterViewHolder(
            CardProgressBarBinding.inflate(
                LayoutInflater.from(p0.context),
                p0,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeProgressAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HomeProgressDiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

    }

    fun setCurrentDot(position: Int) {
        if (position == -1 || position >= itemCount || position == checkedPosition) return

        val previousPosition = checkedPosition
        checkedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(checkedPosition)
    }

}