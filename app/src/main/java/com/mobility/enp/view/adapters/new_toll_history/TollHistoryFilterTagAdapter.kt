package com.mobility.enp.view.adapters.new_toll_history

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemTollHistoryFilterTagBinding
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterTagUi

class TollHistoryFilterTagAdapter(
    private val onTagToggled: (serialNumber: String, isChecked: Boolean) -> Unit,
    private var primaryColor: Int? = null
) : ListAdapter<TollHistoryFilterTagUi, TollHistoryFilterTagAdapter.TagViewHolder>(DIFF_CALLBACK) {

    fun updatePrimaryColor(color: Int?) {
        primaryColor = color
        notifyDataSetChanged()
    }

    inner class TagViewHolder(
        private val binding: ItemTollHistoryFilterTagBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TollHistoryFilterTagUi) {
            binding.regPlate.text = item.registrationPlate
            binding.serialNumber.text = item.serialNumber
            binding.serialNumber.visibility =
                if (item.showSerialNumber) View.VISIBLE else View.INVISIBLE
            binding.line.visibility =
                if (item.isLastItem) View.INVISIBLE else View.VISIBLE

            binding.checkbox.setOnCheckedChangeListener(null)
            binding.checkbox.isChecked = item.isSelected
            applyCheckboxColors(item.isSelected)

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener
                onTagToggled(item.serialNumber, isChecked)
            }
        }

        private fun applyCheckboxColors(isChecked: Boolean) {
            val context = binding.root.context
            val selectedColor = primaryColor
                ?: ContextCompat.getColor(context, R.color.figmaSplashScreenColor)
            val unselectedColor = ContextCompat.getColor(context, R.color.primary_light_dark)

            val textColor = if (isChecked) selectedColor else unselectedColor
            binding.checkbox.buttonTintList = ColorStateList.valueOf(textColor)
            binding.regPlate.setTextColor(textColor)
            binding.serialNumber.setTextColor(textColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTollHistoryFilterTagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TollHistoryFilterTagUi>() {
            override fun areItemsTheSame(
                oldItem: TollHistoryFilterTagUi,
                newItem: TollHistoryFilterTagUi
            ): Boolean = oldItem.serialNumber == newItem.serialNumber

            override fun areContentsTheSame(
                oldItem: TollHistoryFilterTagUi,
                newItem: TollHistoryFilterTagUi
            ): Boolean = oldItem == newItem
        }
    }
}
