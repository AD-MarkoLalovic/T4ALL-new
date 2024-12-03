package com.mobility.enp.view.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.databinding.CardAddDialogBinding
import com.mobility.enp.interf.PromotionInterface
import com.mobility.enp.viewmodel.PaymentAndPassageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardAddDialog(context: Context, private val promotionInterface: PromotionInterface) :
    DialogFragment() {

    private lateinit var binding: CardAddDialogBinding
    private val context: Context
    private val timeDelay = 500L
    private val promotion = Promotion("", "", 0, "", "", false)
    private val viewModel: PaymentAndPassageViewModel by viewModels()

    init {
        isCancelable = false
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = CardAddDialogBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                viewModel.cardLimitByUserType()
            }

            list.forEach { countryCode ->
                when (countryCode) {
                    "RS" -> {
                        binding.serbianPassage.visibility = View.VISIBLE
                    }

                    "MK" -> {
                        binding.macedonianPassage.visibility = View.VISIBLE
                    }

                    "ME" -> {
                        binding.montenegroPassage.visibility = View.VISIBLE
                    }
                }
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.buttonCloseDialog.setOnClickListener {
            dialog!!.dismiss()
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {


                R.id.serbianPassage -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.serbianPassage.isChecked = true
                        promotion.countryCode = "RS"

                        delay(timeDelay)
                        dialog!!.dismiss()
                        promotionInterface.onCountrySelected(promotion)
                    }
                }

                R.id.macedonianPassage -> {
                    binding.macedonianPassage.isChecked = true
                    promotion.countryCode = "MK"

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(timeDelay)
                        dialog!!.dismiss()
                        promotionInterface.onCountrySelected(promotion)
                    }
                }

                R.id.montenegroPassage -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.montenegroPassage.isChecked = true
                        promotion.countryCode = "ME"

                        delay(timeDelay)
                        dialog!!.dismiss()
                        promotionInterface.onCountrySelected(promotion)
                    }
                }

            }
        }
    }

}