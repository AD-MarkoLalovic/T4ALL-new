package com.mobility.enp.view.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.CardProgressBarBinding

class HomeProgressAdapter(val total: Int) :
    RecyclerView.Adapter<HomeProgressAdapter.HomeProgressAdapterViewHolder>() {

    var checkedPosition = 0
    var dotFranchiserColor: Int? = null

    fun updateFranchiserDotColor(dot: Int?) {
        this.dotFranchiserColor = dot
        notifyDataSetChanged()
    }

    fun setCurrentDot(position: Int) {
        if (position != checkedPosition && position != -1) {
            val previousPosition = checkedPosition
            checkedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(checkedPosition)
        }
    }


    inner class HomeProgressAdapterViewHolder(val binding: CardProgressBarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentPosition: Int) {

            var drawable: Int = 0

            dotFranchiserColor?.let {
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

    override fun getItemCount(): Int {
        return total
    }

    override fun onBindViewHolder(holder: HomeProgressAdapterViewHolder, position: Int) {
        holder.bind(position)
    }

}