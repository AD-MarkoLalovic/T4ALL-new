package com.mobility.enp.view.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.data.model.cards.response.Card
import com.mobility.enp.data.model.cards.response.Country
import com.mobility.enp.databinding.FragmentPaymentAndPassageBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.CardsCountryAdapter
import com.mobility.enp.view.adapters.PaymentAndPassageAdapter
import com.mobility.enp.view.dialogs.ConfirmRemovalCardDialog
import com.mobility.enp.view.dialogs.LostTagDialog
import com.mobility.enp.viewmodel.PaymentAndPassageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentAndPassageFragment : Fragment(), PaymentAndPassageAdapter.PrimaryCardListener,
    CardsCountryAdapter.CountryListener {

    private var _binding: FragmentPaymentAndPassageBinding? = null
    private val binding: FragmentPaymentAndPassageBinding get() = _binding!!

    private val viewModel: PaymentAndPassageViewModel by activityViewModels()
    private lateinit var adapter: PaymentAndPassageAdapter
    private lateinit var cardsCountryAdapter: CardsCountryAdapter
    private var allCards: List<Card> = emptyList()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    private var selectedCountry = "All"
    private var isButtonEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentAndPassageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        fetchCardData()
        setupAdapters()
        setObserversError()
        setListener()
        setupCountryList()
        handlePrimaryCardChange()
        setAddCardButton()
    }

    private fun setupAdapters() {
        adapter = PaymentAndPassageAdapter(arrayListOf(), this)
        binding.rvCreditCard.adapter = adapter

        cardsCountryAdapter = CardsCountryAdapter(arrayListOf(), this)
        binding.recyclerCardsCountry.adapter = cardsCountryAdapter
    }

    private fun setupCountryList() {
        viewModel.paymentAndPassageList.observe(viewLifecycleOwner) { paymentAndPassage ->
            viewLifecycleOwner.lifecycleScope.launch {
                val availableCountries = withContext(Dispatchers.IO) {
                    viewModel.cardLimitByUserType()
                }

                // Preuzimanje dodatih kartica
                val addedCards = paymentAndPassage.data?.map { it.country?.code } ?: emptyList()
                Log.d("PaymentAndPassageFragment", "Available countries: $availableCountries")
                Log.d("PaymentAndPassageFragment", "Added cards: $addedCards")

                val isSerbiaAdded = addedCards.contains("RS")

                // Mapiranje kodova zemalja u string resurse
                val countryMapping = mapOf(
                    "All" to R.string.all_country,
                    "RS" to R.string.serbia,
                    "MK" to R.string.macedonia,
                    "ME" to R.string.montenegro
                )

                // Filtrirajte zemlje koje postoje u availableCountries ili "All"
                val filteredCountryMapping = countryMapping.filter { (code, _) ->
                    code == "All" || availableCountries.contains(code)
                }

                // Priprema liste zemalja sa statusom klikabilnosti
                val countryNameAndAdditionalField = mutableListOf<Country>()

                filteredCountryMapping.forEach { (code, resId) ->
                    val isClickable = when (code) {
                        "All" -> true
                        "RS" -> true // Srbija je uvek klikabilna
                        else -> isSerbiaAdded // Ostale zemlje su klikabilne samo ako je Srbija dodata
                    }

                    countryNameAndAdditionalField.add(
                        Country(
                            code,
                            getString(resId),
                            isClickable = isClickable
                        )
                    )
                }

                // Ažurirajte adapter sa novom listom zemalja
                withContext(Dispatchers.Main) {
                    cardsCountryAdapter.updateCountries(countryNameAndAdditionalField)
                    cardsCountryAdapter.setSelectedCountry(selectedCountry)
                }

                setClickableText()  // terms and conditions
            }
        }
    }


    private fun fetchCardData() {
        viewModel.paymentAndPassageList.observe(viewLifecycleOwner) { paymentAndPassage ->
            paymentAndPassage?.let {
                val sortedCards =
                    it.data?.sortedWith(compareByDescending<Card> { card -> card.defaultCard })

                allCards = sortedCards ?: emptyList()

                if (selectedCountry == "All") {
                    toggleNoCardsMessage(allCards.isEmpty())
                    adapter.updateListCards(allCards)
                } else {
                    filterCardsByCountry(selectedCountry)
                }
            }
        }

        viewModel.fetchCard(errorBody)
    }

    private fun handlePrimaryCardChange() {
        viewModel.successfullyChangedPrimaryCard.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(
                    requireContext(), getString(R.string.primary_card_changed), Toast.LENGTH_LONG
                ).show()
                viewModel.fetchCard(errorBody)
            }
        }
    }

    private fun setAddCardButton() {
        binding.bttAddCard.setOnClickListener {
            val promotion = Promotion("", "", 0, "", selectedCountry, false)
            if (selectedCountry != "All" && selectedCountry.isNotEmpty()) {
                val action =
                    PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToCardFragment(
                        promotion
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun toggleNoCardsMessage(isEmpty: Boolean) {
        if (isEmpty) {
            binding.txNoCards.visibility = View.VISIBLE
            binding.rvCreditCard.visibility = View.GONE
        } else {
            binding.txNoCards.visibility = View.GONE
            binding.rvCreditCard.visibility = View.VISIBLE
        }
    }

    private fun filterCardsByCountry(country: String) {
        val filteredCards = allCards.filter { it.country?.code == country }
        if (filteredCards.isEmpty()) {
            binding.txNoCards.visibility = View.VISIBLE
            binding.rvCreditCard.visibility = View.GONE
        } else {
            adapter.updateListCards(filteredCards)
            binding.txNoCards.visibility = View.GONE
            binding.rvCreditCard.visibility = View.VISIBLE
        }

    }

    private fun setListener() {
        viewModel.paymentAndPassageList.observe(viewLifecycleOwner) { paymentAndPassage ->
            paymentAndPassage?.let {
                binding.rvCreditCard.visibility = View.VISIBLE
                binding.recyclerCardsCountry.visibility = View.VISIBLE
                binding.loadingCards.visibility = View.GONE
            }
        }

        binding.termsConditionsCheckmark.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    isButtonEnabled = true
                    makeCardClickable(true)
                }

                false -> {
                    isButtonEnabled = false
                    makeCardClickable(false)
                }
            }
        }
    }

    private fun setObserversError() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            context?.let { context ->
                Toast.makeText(
                    context, errorBody.errorBody, Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

        viewModel.checkNetCards.observe(viewLifecycleOwner) { hasInternet ->
            if (hasInternet != null && !hasInternet) {

                val bundle = Bundle().apply {
                    putString(getString(R.string.title), getString(R.string.no_connection_title))
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

                val binding = (activity as MainActivity).binding
                MainActivity.showSnackMessage(getString(R.string.checking_for_connection), binding)

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    context?.let {
                        while (true) {
                            if (Repository.isNetworkAvailable(it)) {
                                triggerUpdate()
                                break
                            } else {
                                delay(1000L)
                            }
                        }
                    }
                }
            }
        }

        viewModel.dataLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingCards.visibility = View.VISIBLE
                binding.rvCreditCard.visibility = View.GONE
                binding.recyclerCardsCountry.visibility = View.GONE
            } else {
                binding.loadingCards.visibility = View.GONE
                binding.rvCreditCard.visibility = View.VISIBLE
                binding.recyclerCardsCountry.visibility = View.VISIBLE
            }
        }
    }

    private fun triggerUpdate() {
        val mainActivity = activity as? MainActivity
        val bindingMain = mainActivity?.binding

        bindingMain?.let {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                MainActivity.showSnackMessage(getString(R.string.connection_restored), it)
                binding.loadingCards.visibility = View.GONE

                viewModel.fetchCard(errorBody)
            }
        }
    }

    override fun setPrimaryCard(cardId: Int) {

        val primaryCardDialog = LostTagDialog(getString(R.string.choose_primary_card),
            getString(R.string.confirm_change_primary_card),
            object : LostTagDialog.OnButtonClickInLostTag {
                override fun onClickConfirmed() {
                    viewModel.setNewPrimaryCard(cardId, errorBody)
                }
            })
        primaryCardDialog.isCancelable = false
        primaryCardDialog.show(childFragmentManager, "PrimaryCardDialog")

    }

    override fun clickRemoveCard(cardId: String) {
        val confirmRemovalCardDialog =
            ConfirmRemovalCardDialog(object : ConfirmRemovalCardDialog.ClickedDeleteCardInterface {
                override fun onPositiveButtonClicked() {
                    viewModel.deleteCard(cardId, errorBody)
                }

            })
        confirmRemovalCardDialog.isCancelable = false
        confirmRemovalCardDialog.show(childFragmentManager, "ConfirmRemovalCardDialog")
    }

    override fun setCountryListener(country: String) {
        when (country) {
            "RS" -> {
                selectedCountry = "RS"
                filterCardsByCountry("RS")
                setBlockVisibility(false)
                setCardVisibility(true)
                makeCardClickable(true)
                isButtonEnabled = true
                binding.termsConditionsCheckmark.isChecked = false
            }

            "MK" -> {
                selectedCountry = "MK"
                filterCardsByCountry("MK")
                setBlockVisibility(true)
                setCardVisibility(true)
                makeCardClickable(false)
                isButtonEnabled = false
                binding.termsConditionsCheckmark.isChecked = false
            }

            "ME" -> {
                selectedCountry = "ME"
                filterCardsByCountry("ME")
                setBlockVisibility(true)
                setCardVisibility(true)
                makeCardClickable(false)
                isButtonEnabled = false
                binding.termsConditionsCheckmark.isChecked = false
            }

            "HR" -> {
                selectedCountry = "HR"
                filterCardsByCountry("HR")
                setBlockVisibility(true)
                setCardVisibility(true)
                makeCardClickable(false)
                isButtonEnabled = false
                binding.termsConditionsCheckmark.isChecked = false
            }

            else -> {
                setBlockVisibility(false)
                setCardVisibility(false)
                makeCardClickable(false)
                isButtonEnabled = false
                selectedCountry = "All"
                adapter.updateListCards(allCards)
                binding.txNoCards.visibility = if (allCards.isEmpty()) View.VISIBLE else View.GONE
                binding.rvCreditCard.visibility =
                    if (allCards.isEmpty()) View.GONE else View.VISIBLE
                binding.termsConditionsCheckmark.isChecked = false

            }
        }
        cardsCountryAdapter.setSelectedCountry(selectedCountry)  // Dodato za ažuriranje selektovane zemlje u adapteru
    }

    private fun setClickableText() {
        val fullText = resources.getString(R.string.cards_terms_text_full)
        val spannableString = SpannableString(fullText)

        val termsStart =
            fullText.indexOf(resources.getString(R.string.card_terms_left_clickable))  // sets part that is clickable
        val termsEnd = termsStart + resources.getString(R.string.card_terms_left_clickable).length

        val privacyStart =
            fullText.indexOf(resources.getString(R.string.card_term_right_clickable)) // same here
        val privacyEnd =
            privacyStart + resources.getString(R.string.card_term_right_clickable).length


        val clickableSpanTerms = object : ClickableSpan() { // terms and conditions
            override fun onClick(widget: View) {
                val action =
                    PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToPdfViewDialog(
                        selectedCountry, "termsAndConditions"  // dont change this string
                    )
                findNavController().navigate(action)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = requireContext().getColor(R.color.figmaSplashScreenColor)
            }
        }

        val clickablePrivacyTerms = object : ClickableSpan() {  // privacy policy
            override fun onClick(widget: View) {
                val action =
                    PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToPdfViewDialog(
                        selectedCountry, "privacyPolicy"  // dont change this string
                    )
                findNavController().navigate(action)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = requireContext().getColor(R.color.figmaSplashScreenColor)
            }
        }

        spannableString.setSpan(
            clickableSpanTerms,
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            clickablePrivacyTerms,
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvTerms.text = spannableString
        binding.tvTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setBlockVisibility(enable: Boolean) {
        when (enable) {
            true -> {
                binding.termsBlock.visibility = View.VISIBLE
            }

            false -> {
                binding.termsBlock.visibility = View.GONE
            }
        }
    }

    private fun setCardVisibility(enable: Boolean) {
        when (enable) {
            true -> {
                binding.bttAddCard.visibility = View.VISIBLE
            }

            false -> {
                binding.bttAddCard.visibility = View.GONE
            }
        }
    }

    private fun makeCardClickable(enable: Boolean) {
        when (enable) {
            true -> {
                binding.bttAddCard.isClickable = true
                binding.bttAddCard.isEnabled = true
                binding.bttAddCard.backgroundTintList =
                    ColorStateList.valueOf(requireContext().getColor(R.color.figmaSplashScreenColor))
            }

            false -> {
                binding.bttAddCard.isClickable = false
                binding.bttAddCard.isEnabled = false
                binding.bttAddCard.backgroundTintList =
                    ColorStateList.valueOf(requireContext().getColor(R.color.button_not_enabled_web))
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        isButtonEnabled = false
        binding.termsConditionsCheckmark.isChecked = false
        setCountryListener(selectedCountry)
    }
}