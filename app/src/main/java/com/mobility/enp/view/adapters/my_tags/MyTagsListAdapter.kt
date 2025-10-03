package com.mobility.enp.view.adapters.my_tags

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tags.ActivateDeactivateTagModel
import com.mobility.enp.databinding.ItemMyTagsBinding
import com.mobility.enp.view.ui_models.my_tags.TagStatusUiModel
import com.mobility.enp.view.ui_models.my_tags.TagUiModel

class MyTagsListAdapter(
    private val onLostClicked: (String) -> Unit,
    private val onFoundClicked: (String) -> Unit,
    private val onDeactivateTagClicked: (body: ActivateDeactivateTagModel) -> Unit,
    private val onActivateTagClicked: (body: ActivateDeactivateTagModel) -> Unit
) :
    RecyclerView.Adapter<MyTagsListAdapter.MyTagViewHolder>() {

    private val tags = mutableListOf<TagUiModel>()

    fun setItems(list: List<TagUiModel>) {  // this list should be used
        tags.clear()
        tags.addAll(list)
        notifyDataSetChanged()
    }

    fun clearData() {
        tags.clear()
    }

    var selectedCountry: String = "SRB"
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class MyTagViewHolder(private val binding: ItemMyTagsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: TagUiModel) = with(binding) {

            Log.d("UIError", "$tag")

            txTagSerialNumber.text = tag.serialNumber

            buttonActivateTag.visibility = View.GONE
            buttonDeactivateTag.visibility = View.GONE

            val countryCode = when (selectedCountry) {
                "MKD" -> "MK"
                "MNE" -> "ME"
                else -> ""
            }

            binding.buttonActivateTag.setOnClickListener {
                onActivateTagClicked(
                    ActivateDeactivateTagModel(
                        tag.serialNumber,
                        countryCode
                    )
                )
            }

            binding.buttonDeactivateTag.setOnClickListener {
                onDeactivateTagClicked(
                    ActivateDeactivateTagModel(
                        tag.serialNumber,
                        countryCode
                    )
                )
            }

            franchiserTag.text =
                tag.franchiser ?: root.context.getString(R.string.jp_putevi_srbije)

            txtCategoryVal.text = tag.category.toString()

            // Prikaz dugmadi u zavisnosti od stanja
            buttonLostTag.visibility =
                if (selectedCountry == "HRV") View.GONE else if (tag.showButtonLostTag == true) View.VISIBLE else View.GONE
            buttonFoundTag.visibility =
                if (selectedCountry == "HRV") View.GONE else if (tag.showButtonFoundTag == true) View.VISIBLE else View.GONE


            tags.let { list ->
                val foundTag =
                    list.filter { it.serialNumber == tag.serialNumber }

                val showActivateButton = foundTag[0].showButtonActivateTag
                val showDeactivateButton = foundTag[0].showButtonDeactivateTag

                buttonActivateTag.visibility =
                    if (showActivateButton == true && selectedCountry != "SRB") View.VISIBLE else View.GONE
                buttonDeactivateTag.visibility =
                    if (showDeactivateButton == true && selectedCountry != "SRB") View.VISIBLE else View.GONE
            }

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
                flagIcon.setImageResource(flagResId ?: 0)

                // Pronalazak statusa
                val status = statuses.firstOrNull {
                    it.statusesCountry.equals(code, ignoreCase = true)
                }

                // Postavljanje teksta
                countryStatus.text = status?.statusText ?: ""

                // Stilizacija na osnovu statusValue
                val statusValue = status?.statusValue
                val context = root.context

                //dont mix background tint list and drawables it causes an issue when deactivated tag is set and then switches to activated one / drawable takes priority and the color with tint list is now shown
                // when switching tabs because recycler reuses ui elements
                when (statusValue) {
                    "5", "6", "8", "10", "12" -> {
                        countryStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.figmaColorObjection
                            )
                        )
                        tagsStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_5_6_8_10_12
                        )
                    }

                    "4" -> {
                        countryStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.tag_active
                            )
                        )
                        tagsStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_activated
                        )
                    }

                    "9" -> {
                        countryStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_orange
                            )
                        )
                        tagsStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_9
                        )
                    }

                    "1", "11" -> {
                        countryStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.primary_light_dark
                            )
                        )
                        tagsStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_1_11
                        )
                    }

                    "13" -> {
                        tagsStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_deactivated
                        )
                        return
                    }

                    else -> {
                        countryStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                android.R.color.transparent
                            )
                        )
                        tagsStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.border_tag_state_traansparent
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

}