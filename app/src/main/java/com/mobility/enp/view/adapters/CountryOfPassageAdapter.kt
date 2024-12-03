package com.mobility.enp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.registration.CountryModel
import com.mobility.enp.databinding.CountryOfPassageCardBinding

class CountryOfPassageAdapter(
    private val list: ArrayList<CountryModel>,
    private val context: Context
) : RecyclerView.Adapter<CountryOfPassageAdapter.CountryOfPassageViewHolder>() {
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private lateinit var selectedCountry: SelectedCountry
    private var darkMode: Boolean = false

    fun setInter(selectedCountry: SelectedCountry) {
        this.selectedCountry = selectedCountry
    }

    fun setDarkMode(darkMode: Boolean) {
        this.darkMode = darkMode
    }

    class CountryOfPassageViewHolder(val binding: CountryOfPassageCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(county: CountryModel) {
            binding.countryData = county
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryOfPassageViewHolder {
        val binding = CountryOfPassageCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CountryOfPassageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CountryOfPassageViewHolder, position: Int) {
        val current = list[holder.bindingAdapterPosition]
        holder.bind(current)

        if (darkMode) {
            holder.binding.CountryTextField.setTextColor(context.getColor(R.color.black))
        }
        holder.binding.checkbox.setOnClickListener {
            if (selectedPosition != holder.bindingAdapterPosition) {
                // Unselect the previously selected item
                list.getOrNull(selectedPosition)?.isChecked = false
                holder.binding.tosContainer.visibility = View.GONE
                // Select the current item
                current.isChecked = true
                selectedPosition = holder.bindingAdapterPosition
                selectedCountry.pickedCountry(list[selectedPosition])
                notifyItemChanged(holder.bindingAdapterPosition)
            }

            if (holder.binding.tosContainer.visibility == View.VISIBLE) {
                holder.binding.tosContainer.visibility = View.GONE
            } else {
                current.isChecked = true
                holder.binding.tosContainer.visibility = View.VISIBLE
            }

            notifyDataSetChanged()
        }

        holder.binding.checkBoxTos.setOnClickListener {
            val bool = holder.binding.checkBoxTos.isChecked
            list[holder.bindingAdapterPosition].isTosChecked = bool
            selectedCountry.pickedCountry(list[holder.bindingAdapterPosition])   // once adapter return this checkbox as true the user has confirmed data processing and next window can start
        }

        if (!current.isIconVisible) {
            holder.binding.icon.visibility = View.GONE
        }
    }

    interface SelectedCountry {
        fun pickedCountry(county: CountryModel)
    }
}