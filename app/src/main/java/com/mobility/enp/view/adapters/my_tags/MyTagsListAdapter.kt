package com.mobility.enp.view.adapters.my_tags

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.ItemMyTagsBinding
import com.mobility.enp.view.ui_models.my_tags.TagStatusUiModel
import com.mobility.enp.view.ui_models.my_tags.TagUiModel

class MyTagsListAdapter(
    private val onLostClicked: (String) -> Unit,
    private val onFoundClicked: (String) -> Unit
) :
    RecyclerView.Adapter<MyTagsListAdapter.MyTagViewHolder>() {

    private val tags = mutableListOf<TagUiModel>()

    var selectedCountry: String = "SRB"
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class MyTagViewHolder(private val binding: ItemMyTagsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: TagUiModel) = with(binding) {
            txTagSerialNumber.text = tag.serialNumber

            franchiserTag?.text =
                tag.franchiser ?: root.context.getString(R.string.jp_putevi_srbije)

            txtCategoryVal.text = tag.category.toString()

            // Prikaz dugmadi u zavisnosti od stanja
            buttonLostTag.visibility =
                if (selectedCountry == "HRV") View.GONE else if (tag.showButtonLostTag == true) View.VISIBLE else View.GONE
            buttonFoundTag.visibility =
                if (selectedCountry == "HRV") View.GONE else if (tag.showButtonFoundTag == true) View.VISIBLE else View.GONE

            // Ako nema registracije, prikaži "Serijski broj"
            if (tag.registrationPlate.isNullOrEmpty()) {
                txTable.apply {
                    text = root.context.getString(R.string.serial_number)
                    setTextAppearance(R.style.CaptionRegular)
                }
            } else {
                txTable.apply {
                    text = tag.registrationPlate
                    setTextAppearance(R.style.SubtitlesRegular)
                    setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.primary_light_darkest
                        )
                    )
                }
            }

            setStatusAppearance(selectedCountry, tag.statuses)

            binding.buttonLostTag.setOnClickListener {
                onLostClicked(tag.serialNumber)
            }

            binding.buttonFoundTag.setOnClickListener {
                onFoundClicked(tag.serialNumber)
            }

        }

        private fun setStatusAppearance(countryCode: String, statuses: List<TagStatusUiModel>) =
            with(binding) {
                var code = "RS"

                val flagResId = when (countryCode) {
                    "MKD" -> {
                        code = "MK"
                        R.drawable.macedonia_flag
                    }

                    "SRB" -> {
                        code = "RS"
                        R.drawable.serbia_flag
                    }

                    "MNE" -> {
                        code = "ME"
                        R.drawable.montenegro_flag
                    }

                    "HRV" -> {
                        code = "HR"
                        R.drawable.croatia_flag
                    }

                    else -> null
                }
                flagIcon?.setImageResource(flagResId ?: 0)

                // Pronalazak statusa
                val status = statuses.firstOrNull {
                    it.statusesCountry.equals(code, ignoreCase = true)
                }

                // Postavljanje teksta
                countryStatus?.text = status?.statusText ?: ""

                // Stilizacija na osnovu statusValue
                val statusValue = status?.statusValue
                val context = root.context
                when (statusValue) {
                    "5", "6", "8", "10", "12" -> {
                        countryStatus?.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.figmaColorObjection
                            )
                        )
                        tagsStatus?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.figmaToolHistoryUnpaidBackground
                            )
                        )
                    }

                    "4" -> {
                        countryStatus?.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.tag_active
                            )
                        )
                        tagsStatus?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.figmaToolHistoryPaidBackground
                            )
                        )
                    }

                    "9" -> {
                        countryStatus?.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_orange
                            )
                        )
                        tagsStatus?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.soft_peach
                            )
                        )
                    }

                    "1", "11" -> {
                        countryStatus?.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.primary_light_dark
                            )
                        )
                        tagsStatus?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.primary_light_light
                            )
                        )
                    }

                    "13" -> {
                        tagsStatus?.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_deactivated
                        )
                        return
                    }

                    else -> {
                        countryStatus?.setTextColor(
                            ContextCompat.getColor(
                                context,
                                android.R.color.transparent
                            )
                        )
                        tagsStatus?.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                android.R.color.transparent
                            )
                        )
                    }
                }
            }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTagViewHolder {
        return MyTagViewHolder(
            ItemMyTagsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int = tags.size


    override fun onBindViewHolder(holder: MyTagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    fun setItems(newItems: List<TagUiModel>) {
        tags.clear()
        tags.addAll(newItems)
        notifyDataSetChanged()
    }

}