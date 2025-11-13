package com.mobility.enp.view.fragments

import android.content.res.ColorStateList
import android.graphics.Paint
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.cards.response.Card
import com.mobility.enp.data.model.cards.response.CardsResponse
import com.mobility.enp.data.model.cards.response.Country
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.databinding.FragmentPaymentAndPassageBinding
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.cards.CardsCountryAdapter
import com.mobility.enp.view.adapters.cards.PaymentAndPassageAdapter
import com.mobility.enp.view.adapters.cards.TagsForCroatiaAdapter
import com.mobility.enp.view.dialogs.ConfirmRemovalCardDialog
import com.mobility.enp.view.dialogs.LostTagDialog
import com.mobility.enp.view.dialogs.SerbianTagInCroatiaDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.PaymentAndPassageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentAndPassageFragment : Fragment(), PaymentAndPassageAdapter.PrimaryCardListener,
    CardsCountryAdapter.CountryListener {

    private var _binding: FragmentPaymentAndPassageBinding? = null
    private val binding: FragmentPaymentAndPassageBinding get() = _binding!!

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: PaymentAndPassageViewModel by activityViewModels { PaymentAndPassageViewModel.Factory }
    private lateinit var adapter: PaymentAndPassageAdapter
    private lateinit var cardsCountryAdapter: CardsCountryAdapter
    private lateinit var tagsForCroatiaAdapter: TagsForCroatiaAdapter

    private var allCards: List<Card> = emptyList()
    private var showLoginToHac = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentAndPassageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argCountry = arguments?.getString("countryCode")

        if (!argCountry.isNullOrEmpty()) {
            viewModel.saveSelectCountry = argCountry
            arguments?.remove("countryCode")
        } else if (viewModel.saveSelectCountry.isNullOrEmpty()) {
            viewModel.saveSelectCountry = ""
        }

        setListener()
        setupAdapters()
        setObservers()

        viewModel.fetchCardFlow()

    }

    private fun setObservers() {
        collectLatestLifecycleFlow(viewModel.getCardDataFlow) { cardWeb ->
            when (cardWeb) {
                is SubmitResult.Loading -> {
                    if (viewModel.saveSelectCountry != "HR") {
                        binding.loadingCards.visibility = View.VISIBLE
                        binding.rvCreditCard.visibility = View.GONE
                        binding.recyclerCardsCountry.visibility = View.GONE
                    }
                }

                is SubmitResult.Success -> {
                    binding.loadingCards.visibility = View.GONE
                    binding.recyclerCardsCountry.visibility = View.VISIBLE

                    if (viewModel.saveSelectCountry != "HR") {
                        binding.rvCreditCard.visibility = View.VISIBLE
                        binding.bttAddCard.visibility = View.VISIBLE
                    } else {
                        binding.rvCreditCard.visibility = View.GONE
                        binding.bttAddCard.visibility = View.GONE
                    }

                    showLoginToHac = cardWeb.data.data?.hacPortalUrl != null
                    updateAdapter(cardWeb.data)

                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                    internetReconnectMethod()
                }

                is SubmitResult.FailureServerError -> {
                    binding.loadingCards.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.loadingCards.visibility = View.GONE
                    showError(cardWeb.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(cardWeb.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.Empty -> {}
            }
        }

        collectLatestLifecycleFlow(viewModel.successfullyChangedPrimaryCard) { cardChanged ->
            when (cardChanged) {
                is SubmitResult.Loading -> {
                    binding.loadingCards.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.loadingCards.visibility = View.GONE

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.primary_card_changed),
                        Toast.LENGTH_LONG
                    ).show()

                    viewModel.fetchCardFlow()
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.loadingCards.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.loadingCards.visibility = View.GONE
                    showError(cardChanged.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(cardChanged.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.Empty -> {}
            }
        }

        collectLatestLifecycleFlow(viewModel.successfullyDeletedCard) { cardDeleted ->

            when (cardDeleted) {
                is SubmitResult.Loading -> {
                    binding.loadingCards.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.loadingCards.visibility = View.GONE

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.card_successfully_deleted),
                        Toast.LENGTH_LONG
                    ).show()

                    viewModel.fetchCardFlow()
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.loadingCards.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.loadingCards.visibility = View.GONE
                    showError(cardDeleted.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(cardDeleted.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.Empty -> {}
            }

        }

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->

            franchiseModel?.franchisePrimaryColor?.let { color ->
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    color,  // ON color
                    ContextCompat.getColor(
                        requireContext(), R.color.primary_light_dark
                    ) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.termsConditionsCheckmark.buttonTintList = colorStateList
                binding.bttRegTagForCroatia.backgroundTintList =
                    ColorStateList.valueOf(color)
            } ?: run {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.figmaSplashScreenColor
                    ),  // ON color
                    ContextCompat.getColor(
                        requireContext(), R.color.primary_light_dark
                    ) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.termsConditionsCheckmark.buttonTintList = colorStateList
                binding.bttRegTagForCroatia.backgroundTintList =
                    ColorStateList.valueOf(requireContext().getColor(R.color.figmaSplashScreenColor))
            }
        }

        collectLatestLifecycleFlow(viewModel.tagsList) { result ->
            when (result) {
                is SubmitResultFold.Loading -> {
                    binding.loadingCards.visibility = View.VISIBLE
                }

                is SubmitResultFold.Success -> {
                    binding.loadingCards.visibility = View.GONE

                    val tagsList = result.data.filter { it.status == 11 }
                    if (tagsList.isNotEmpty()) {
                        binding.txCroatiaText.text =
                            getString(R.string.tag_registration_instruction_hr)
                        visibleCroatianComponents(true)
                        binding.bttRegTagForCroatia.visibility = View.VISIBLE

                        tagsForCroatiaAdapter.submitList(tagsList)
                    } else {
                        visibleCroatianComponents(true)
                        binding.bttRegTagForCroatia.visibility = View.GONE
                        binding.txCroatiaText.text =
                            getString(R.string.activation_successful_enp_tag_device)
                    }

                    if (showLoginToHac) {
                        binding.loginToHac.paintFlags =
                            binding.loginToHac.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        binding.loginToHac.visibility = View.VISIBLE
                    } else {
                        binding.loginToHac.visibility = View.GONE
                    }

                }

                is SubmitResultFold.Failure -> {
                    binding.loadingCards.visibility = View.GONE

                    when (result.error) {
                        is NetworkError.NoConnection -> {
                            binding.loadingCards.visibility = View.GONE
                            showNoConnectionState()
                        }

                        is NetworkError.ServerError -> {
                            binding.loadingCards.visibility = View.GONE
                            showError(getString(R.string.server_error_msg))
                        }

                        is NetworkError.ApiError -> {
                            binding.loadingCards.visibility = View.GONE
                            showError(result.error.errorResponse.message ?: "")
                        }
                    }
                }

                is SubmitResultFold.Idle -> {}
            }
        }

        collectLatestLifecycleFlow(viewModel.registrationHr) { result ->
            when (result) {
                is SubmitResultFold.Loading -> {
                    binding.loadingCards.visibility = View.VISIBLE
                }

                is SubmitResultFold.Success -> {
                    binding.loadingCards.visibility = View.GONE
                    val url = result.data

                    val action =
                        PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToHacPortalWebFragment(
                            url
                        )
                    findNavController().navigate(action)
                }

                is SubmitResultFold.Failure -> {
                    when (result.error) {
                        is NetworkError.NoConnection -> {
                            binding.loadingCards.visibility = View.GONE
                            showNoConnectionState()
                        }

                        is NetworkError.ServerError -> {
                            binding.loadingCards.visibility = View.GONE
                            showError(getString(R.string.server_error_msg))
                        }

                        is NetworkError.ApiError -> {
                            binding.loadingCards.visibility = View.GONE
                            showError(result.error.errorResponse.message ?: "")
                        }
                    }
                }

                is SubmitResultFold.Idle -> {}
            }
        }
    }

    private fun setupAdapters() {
        adapter = PaymentAndPassageAdapter(arrayListOf(), this, franchiseViewModel)
        binding.rvCreditCard.adapter = adapter

        cardsCountryAdapter = CardsCountryAdapter(arrayListOf(), this)
        binding.recyclerCardsCountry.adapter = cardsCountryAdapter


        val franchiseColor = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor
        tagsForCroatiaAdapter = TagsForCroatiaAdapter(
            { serialNumbers -> viewModel.onCheckChanged(serialNumbers) },
            franchiseColor
        )

        binding.rvTagsForCroatia.adapter = tagsForCroatiaAdapter
    }

    private fun processCardResponse(cardWebResponse: CardWebModel) {
        val paymentAndPassage: CardsResponse = viewModel.objectTransformer(cardWebResponse)

        paymentAndPassage.let {
            val sortedCards =
                it.data?.sortedWith(compareByDescending { card -> card.defaultCard })

            allCards = sortedCards ?: emptyList()

            filterCardsByCountry(viewModel.saveSelectCountry!!)
        }
    }

    private fun updateAdapter(cardWebResponse: CardWebModel) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val paymentAndPassage: CardsResponse = viewModel.objectTransformer(cardWebResponse)

            // Preuzimanje dodatih kartica
            val addedCards = paymentAndPassage.data?.map { it.country?.code } ?: emptyList()
            Log.d("PaymentAndPassageFragment", "Added cards: $addedCards")

            val availableCountries: ArrayList<String> = arrayListOf()

            if (cardWebResponse.data?.showTabMK == true) {  // api is sending us when tabs are visible now
                availableCountries.add("MK")
            }

            if (cardWebResponse.data?.showTabME == true) {
                availableCountries.add("ME")
            }

            if (cardWebResponse.data?.showTabHR == true) {
                availableCountries.add("HR")
            }

            if (cardWebResponse.data?.isFranchiser == false) {  // if serbia is not a franshizer then it gets shown
                availableCountries.add("RS")
            }

            // Mapiranje kodova zemalja u string resurse
            val countryMapping = mapOf(
                "RS" to R.string.serbia,
                "MK" to R.string.macedonia,
                "ME" to R.string.montenegro,
                "HR" to R.string.croatia
            )

            // Filtrirajte zemlje koje postoje u availableCountries
            val filteredCountryMapping = countryMapping.filter { (code, _) ->
                availableCountries.contains(code)
            }


            // Priprema liste zemalja sa statusom klikabilnosti
            val countryNameAndAdditionalField = mutableListOf<Country>()

            filteredCountryMapping.forEach { (code, resId) ->
                val isClickable = when (code) {
                    "RS" -> true // Srbija je uvek klikabilna
                    //"HR" -> true
                    else -> cardWebResponse.data?.hasSerbianCard
                }

                countryNameAndAdditionalField.add(
                    Country(
                        code,
                        getString(resId),
                        isClickable = isClickable!!
                    )
                )
            }

            // Ažuriranje adaptera sa novom listom zemalja
            withContext(Dispatchers.Main) {
                cardsCountryAdapter.updateCountries(countryNameAndAdditionalField)

                // Ako ništa nije selektovano iz argumenata, uzimamo prvu dostupnu zemlju
                if (viewModel.saveSelectCountry.isNullOrEmpty() && countryNameAndAdditionalField.isNotEmpty()) {
                    val firstClickable =
                        countryNameAndAdditionalField.firstOrNull { it.isClickable }
                    viewModel.saveSelectCountry =
                        firstClickable?.code ?: "" // fallback ako nema klikabilne
                }

                cardsCountryAdapter.setSelectedCountry(viewModel.saveSelectCountry!!)

                val position =
                    cardsCountryAdapter.getPositionByCountryCode(viewModel.saveSelectCountry!!)
                if (position != -1) {
                    binding.recyclerCardsCountry.scrollToPosition(position)
                }

                setClickableText()  // terms and conditions

                processCardResponse(cardWebResponse)
            }
        }
    }

    private fun filterCardsByCountry(country: String) {
        val filteredCards = allCards.filter { it.country?.code == country }
        if (filteredCards.isEmpty() && (country != "HR")) {
            binding.txNoCards.visibility = View.VISIBLE
            binding.rvCreditCard.visibility = View.GONE
        } else {
            adapter.updateListCards(filteredCards)
            binding.txNoCards.visibility = View.GONE
            binding.rvCreditCard.visibility = View.VISIBLE
        }
    }

    private fun setListener() {
        binding.bttRegTagForCroatia.setOnClickListener {
            viewModel.registrationTagsForHr()
        }

        binding.txCroatiaCardsNote.setOnClickListener {
            SerbianTagInCroatiaDialog().show(parentFragmentManager, "SerbianTagInCroatia")
        }
        binding.termsConditionsCheckmark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                makeCardClickable(true)
            } else {
                makeCardClickable(false)
            }
        }

        binding.loginToHac.setOnClickListener {
            val action =
                PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToHacPortalWebFragment(
                    " https://prodaja.hac.hr/Account/Login?selectedTab=v-pills-buyEnc"
                )
            findNavController().navigate(action)
        }

        binding.bttAddCard.setOnClickListener {
            if (!viewModel.saveSelectCountry.isNullOrEmpty()) {
                val action =
                    PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToCardFragment(
                        viewModel.saveSelectCountry
                    )
                findNavController().navigate(action)
            }
        }

        setFragmentResultListener("htmlDialogDismissed") { _, _ ->
            binding.loadingCards.visibility = View.GONE
        }

        binding.txCroatiaCardsPdf.setOnClickListener {
            val action =
                PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToPdfViewerFragment()
            findNavController().navigate(action)
        }
    }

    private fun internetReconnectMethod() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
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
            }

            while (isActive && isAdded) {
                if (viewModel.isInternetAvailable()) {
                    triggerUpdate()
                    break
                } else {
                    delay(3000L)
                }
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

                viewModel.fetchCardFlow()
            }
        }
    }

    override fun setPrimaryCard(cardId: Int) {
        LostTagDialog.newInstance(
            title = getString(R.string.choose_primary_card),
            subtitle = getString(R.string.confirm_change_primary_card),
            onButtonClick = {
                viewModel.setNewPrimaryCard(cardId)
            }
        ).show(parentFragmentManager, "PrimaryCardDialog")
    }

    override fun clickRemoveCard(cardId: String) {
        val confirmRemovalCardDialog =
            ConfirmRemovalCardDialog(object : ConfirmRemovalCardDialog.ClickedDeleteCardInterface {
                override fun onPositiveButtonClicked() {
                    viewModel.deleteCard(cardId)
                }
            })
        confirmRemovalCardDialog.isCancelable = false
        confirmRemovalCardDialog.show(childFragmentManager, "ConfirmRemovalCardDialog")
    }

    override fun setCountryListener(country: String) {
        viewModel.saveSelectCountry = country

        when (country) {
            "RS" -> {
                visibleCroatianComponents(false)
                binding.termsConditionsCheckmark.isChecked = false
                filterCardsByCountry("RS")
                setBlockVisibility(false)
                setCardVisibility(true)
                makeCardClickable(true)
                binding.bttRegTagForCroatia.visibility = View.GONE
            }

            "MK" -> {
                visibleCroatianComponents(false)
                filterCardsByCountry("MK")
                setBlockVisibility(true)
                setCardVisibility(true)
                makeCardClickable(false)
                binding.termsConditionsCheckmark.isChecked = false
                binding.bttRegTagForCroatia.visibility = View.GONE
            }

            "ME" -> {
                visibleCroatianComponents(false)
                filterCardsByCountry("ME")
                setBlockVisibility(true)
                setCardVisibility(true)
                makeCardClickable(false)
                binding.termsConditionsCheckmark.isChecked = false
                binding.bttRegTagForCroatia.visibility = View.GONE
            }

            "HR" -> {
                setBlockVisibility(false)
                setCardVisibility(false)
                makeCardClickable(false)
                binding.txNoCards.visibility = View.GONE
                binding.termsConditionsCheckmark.isChecked = false
                binding.rvCreditCard.visibility = View.GONE
                viewModel.fetchTagsForCroatia()
            }
        }
        cardsCountryAdapter.setSelectedCountry(country)  // Dodato za ažuriranje selektovane zemlje u adapteru
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

        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor


        val clickableSpanTerms = object : ClickableSpan() { // terms and conditions
            override fun onClick(widget: View) {
                val action =
                    PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToPdfViewDialog(
                        viewModel.saveSelectCountry,
                        "termsAndConditions"  // dont change this string
                    )
                findNavController().navigate(action)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true

                color?.let {
                    ds.color = it
                } ?: run {
                    ds.color = requireContext().getColor(R.color.figmaSplashScreenColor)
                }
            }
        }

        val clickablePrivacyTerms = object : ClickableSpan() {  // privacy policy
            override fun onClick(widget: View) {
                val action =
                    PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToPdfViewDialog(
                        viewModel.saveSelectCountry, "privacyPolicy"  // dont change this string
                    )
                findNavController().navigate(action)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true

                color?.let {
                    ds.color = it
                } ?: run {
                    ds.color = requireContext().getColor(R.color.figmaSplashScreenColor)
                }
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
        if (enable) {
            binding.termsBlock.visibility = View.VISIBLE
        } else {
            binding.termsBlock.visibility = View.GONE
        }

    }

    private fun setCardVisibility(enable: Boolean) {
        if (enable) {
            binding.bttAddCard.visibility = View.VISIBLE
        } else {
            binding.bttAddCard.visibility = View.GONE
        }

    }

    private fun makeCardClickable(enable: Boolean) {
        val franchiseModel = franchiseViewModel.franchiseModel.value

        binding.bttAddCard.apply {
            isClickable = enable
            isEnabled = enable

            backgroundTintList = ColorStateList.valueOf(
                if (enable) {
                    franchiseModel?.franchisePrimaryColor
                        ?: requireContext().getColor(R.color.figmaSplashScreenColor)
                } else {
                    franchiseModel?.halfColor
                        ?: requireContext().getColor(R.color.button_not_enabled_web)
                }
            )
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showNoConnectionState() {
        binding.loadingCards.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun visibleCroatianComponents(visible: Boolean) {
        if (visible) {
            binding.txCroatiaText.visibility = View.VISIBLE
            binding.txCroatiaCardsNote.visibility = View.VISIBLE
            binding.rvTagsForCroatia.visibility = View.VISIBLE
            binding.txCroatiaCardsPdf.visibility = View.VISIBLE
            binding.loginToHac.visibility = View.VISIBLE
        } else {
            binding.txCroatiaText.visibility = View.GONE
            binding.txCroatiaCardsNote.visibility = View.GONE
            binding.rvTagsForCroatia.visibility = View.GONE
            binding.txCroatiaCardsPdf.visibility = View.GONE
            binding.loginToHac.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearTagsList()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.termsConditionsCheckmark.isChecked = false
    }
}