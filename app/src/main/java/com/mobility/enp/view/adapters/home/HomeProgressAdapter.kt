package com.mobility.enp.view.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobility.enp.R
import com.mobility.enp.databinding.CardProgressBarBinding

class HomeProgressAdapter(val total: Int) :
    RecyclerView.Adapter<HomeProgressAdapter.HomeProgressAdapterViewHolder>() {

    var checkedPosition = 0

    fun setCurrentDot(position: Int) {
        if (position != checkedPosition && position != -1) {
            checkedPosition = position
            notifyDataSetChanged()
        }
    }

    inner class HomeProgressAdapterViewHolder(val binding: CardProgressBarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentPosition: Int) {
            if (checkedPosition == currentPosition) {
                Glide.with(binding.root.context).load(R.drawable.dot_checked).into(binding.dot)
            } else {
                Glide.with(binding.root.context).load(R.drawable.dot_unchecked).into(binding.dot)
            }
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