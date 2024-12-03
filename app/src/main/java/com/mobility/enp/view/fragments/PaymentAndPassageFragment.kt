package com.mobility.enp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.data.model.api_my_profile.cards.Card
import com.mobility.enp.data.model.api_my_profile.cards.Country
import com.mobility.enp.databinding.FragmentPaymentAndPassageBinding
import com.mobility.enp.interf.PromotionInterface
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.CardsCountryAdapter
import com.mobility.enp.view.adapters.PaymentAndPassageAdapter
import com.mobility.enp.view.dialogs.CardAddDialog
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

    private val viewModel: PaymentAndPassageViewModel by viewModels()
    private lateinit var adapter: PaymentAndPassageAdapter
    private lateinit var cardsCountryAdapter: CardsCountryAdapter
    private var allCards: List<Card> = emptyList()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    private var selectedCountry = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentAndPassageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupAdapters()
        setObserversError()
        setListener()
        setupCountryList()
        fetchCardData()
        handlePrimaryCardChange()
        setupAddCardButton()

    }

    private fun setupAdapters() {
        adapter = PaymentAndPassageAdapter(arrayListOf(), viewModel, this)
        binding.rvCreditCard.adapter = adapter

        cardsCountryAdapter = CardsCountryAdapter(arrayListOf(), this)
        binding.recyclerCardsCountry.adapter = cardsCountryAdapter
    }

    private fun setupCountryList() {

        viewLifecycleOwner.lifecycleScope.launch {

            val list = withContext(Dispatchers.IO) {
                viewModel.cardLimitByUserType()
            }

            Log.d("Test", "setupCountryList: $list")

            val countryMapping = mapOf(
                "RS" to R.string.serbia,
                "MK" to R.string.macedonia,
                "ME" to R.string.montenegro,
                "All" to R.string.all_country
            )

            val countryNameAndAdditionalField = list.mapNotNull { code ->
                countryMapping[code]?.let { resId ->
                    Country(code, getString(resId))
                }
            }.toMutableList()
            
            countryNameAndAdditionalField.add(0, Country("All", getString(R.string.all_country)))

            withContext(Dispatchers.Main) {
                cardsCountryAdapter.updateCountries(countryNameAndAdditionalField)
                cardsCountryAdapter.setSelectedCountry(selectedCountry)
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
                    adapter.updateListCards(allCards)
                } else {
                    filterCardsByCountry(selectedCountry)
                }

                toggleNoCardsMessage(allCards.isEmpty())
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

    private fun setupAddCardButton() {
        binding.bttAddCard.setOnClickListener {
            context?.let { context ->
                val dialogAddCard = CardAddDialog(context, object : PromotionInterface {
                    override fun onCountrySelected(promotion: Promotion) {
                        val action =
                            PaymentAndPassageFragmentDirections.actionPaymentAndPassageFragmentToCardFragment(
                                promotion
                            )
                        findNavController().navigate(action)
                    }
                })

                dialogAddCard.isCancelable = false
                activity?.supportFragmentManager?.let { manager ->
                    dialogAddCard.show(manager, "CardAddDialog")
                }
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
                binding.bttAddCard.visibility = View.VISIBLE
                binding.loadingCards.visibility = View.GONE
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
            }

            "MK" -> {
                selectedCountry = "MK"
                filterCardsByCountry("MK")
            }

            "ME" -> {
                selectedCountry = "ME"
                filterCardsByCountry("ME")
            }

            "HR" -> {
                selectedCountry = "HR"
                filterCardsByCountry("HR")
            }

            else -> {
                selectedCountry = "All"
                adapter.updateListCards(allCards)
                binding.txNoCards.visibility = if (allCards.isEmpty()) View.VISIBLE else View.GONE
                binding.rvCreditCard.visibility =
                    if (allCards.isEmpty()) View.GONE else View.VISIBLE
            }
        }
        cardsCountryAdapter.setSelectedCountry(selectedCountry)  // Dodato za ažuriranje selektovane zemlje u adapteru
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}