package com.mobility.enp.view.adapters.refund_request_adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemTagPickerRequestBinding
import com.mobility.enp.view.ui_models.refund_request.TagsRefundRequestUIModel
import com.mobility.enp.viewmodel.FranchiseViewModel

class RefundRequestTagPickerAdapter(
    private val itemClickListener: (serialNumber: String) -> Unit,
    private val franchiseViewModel: FranchiseViewModel
) :
    RecyclerView.Adapter<RefundRequestTagPickerAdapter.TagPickerViewHolder>() {

    private val differ =
        AsyncListDiffer(this, object : DiffUtil.ItemCallback<TagsRefundRequestUIModel>() {
            override fun areItemsTheSame(
                oldItem: TagsRefundRequestUIModel,
                newItem: TagsRefundRequestUIModel
            ): Boolean {
                return oldItem.serialNumber == newItem.serialNumber // Ili druga jedinstvena identifikacija
            }

            override fun areContentsTheSame(
                oldItem: TagsRefundRequestUIModel,
                newItem: TagsRefundRequestUIModel
            ): Boolean {
                return oldItem == newItem
            }
        })

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class TagPickerViewHolder(val binding: ItemTagPickerRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(myTags: TagsRefundRequestUIModel) {
            binding.tag = myTags
            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagPickerViewHolder {
        return TagPickerViewHolder(
            ItemTagPickerRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = differ.currentList.size


    override fun onBindViewHolder(holder: TagPickerViewHolder, position: Int) {
        val currentTag = differ.currentList[position]
        holder.bind(currentTag)

        holder.binding.refundRequestRadioButton.isChecked = (position == selectedPosition)

        // Ažuriranje boje na osnovu selektovane pozicije
        updateColors(holder, position == selectedPosition)

        holder.binding.refundRequestRadioButton.setOnClickListener {
            // Ako trenutni položaj nije već selektovan, ažuriraj `selectedPosition`
            if (selectedPosition != holder.bindingAdapterPosition) {
                val previousPosition = selectedPosition
                selectedPosition = holder.bindingAdapterPosition

                // Očisti prethodno selektovano radio dugme
                notifyItemChanged(previousPosition)
                // Obeleži novo selektovano radio dugme
                notifyItemChanged(selectedPosition)

                // Poziva se `itemClickListener` kako bi obavestio fragment o promeni selektovanog taga
                itemClickListener(currentTag.serialNumber)
            }
        }
    }

    private fun updateColors(holder: TagPickerViewHolder, isSelected: Boolean) {
        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor
        var colorStateList: ColorStateList? = null

        color?.let {
            colorStateList = if (isSelected) {
                ColorStateList.valueOf(it)
            } else {
                ContextCompat.getColorStateList(holder.itemView.context, R.color.primary_light_dark)
            }
        } ?: run {
            colorStateList = if (isSelected) {
                ContextCompat.getColorStateList(
                    holder.itemView.context,
                    R.color.figmaSplashScreenColor
                )
            } else {
                ContextCompat.getColorStateList(holder.itemView.context, R.color.primary_light_dark)
            }
        }

        holder.binding.refundRequestTable.setTextColor(colorStateList)
        holder.binding.refundRequestSerialNumber.setTextColor(colorStateList)
        holder.binding.refundRequestRadioButton.buttonTintList = colorStateList
    }

    fun submitList(list: List<TagsRefundRequestUIModel>) {
        differ.submitList(list)
    }
}