package com.mobility.enp.view.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.databinding.CardAddDialogBinding
import com.mobility.enp.interf.PromotionInterface
import com.mobility.enp.viewmodel.PaymentAndPassageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardAddDialog(private val promotionInterface: PromotionInterface) : DialogFragment() {

    private lateinit var binding: CardAddDialogBinding
    private val timeDelay = 500L
    private val promotion = Promotion("", "", 0, "", "", false)
    private val viewModel: PaymentAndPassageViewModel by activityViewModels() // ili ako želite isti ViewModel iz Fragmenta

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = CardAddDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCountryVisibility()
    }

    private fun setupCountryVisibility() {
        viewModel.paymentAndPassageList.observe(viewLifecycleOwner) { paymentAndPassage ->
            viewLifecycleOwner.lifecycleScope.launch {
                val availableCountries = withContext(Dispatchers.IO) {
                    viewModel.cardLimitByUserType()
                }
                val addedCards = paymentAndPassage.data?.map { it.country?.code } ?: emptyList()
                val isSerbiaAdded = addedCards.contains("RS")

                // Vidljivost zemalja bazirana na dodatoj Srbiji
                binding.serbianPassage.visibility = View.VISIBLE // Srbija je uvek vidljiva

                binding.macedonianPassage.visibility =
                    if (isSerbiaAdded && availableCountries.contains("MK")) View.VISIBLE else View.GONE
                binding.montenegroPassage.visibility =
                    if (isSerbiaAdded && availableCountries.contains("ME")) View.VISIBLE else View.GONE

            }
        }
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false

        binding.buttonCloseDialog.setOnClickListener {
            dialog?.dismiss()
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.serbianPassage -> handleCountrySelection("RS", binding.serbianPassage)
                R.id.macedonianPassage -> handleCountrySelection("MK", binding.macedonianPassage)
                R.id.montenegroPassage -> handleCountrySelection("ME", binding.montenegroPassage)
            }
        }
    }

    private fun handleCountrySelection(countryCode: String, radioButton: RadioButton) {
        viewLifecycleOwner.lifecycleScope.launch {
            radioButton.isChecked = true
            promotion.countryCode = countryCode

            delay(timeDelay)
            dialog?.dismiss()
            promotionInterface.onCountrySelected(promotion)
        }
    }
}
