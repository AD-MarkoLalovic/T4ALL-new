package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_profile.basic_information.request.UpdateUserDataRequest
import com.mobility.enp.databinding.FragmentBasicInformationBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.ui_models.BasicInfoUIModel
import com.mobility.enp.viewmodel.BasicInfoViewModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BasicInformationFragment : Fragment() {

    private var _binding: FragmentBasicInformationBinding? = null
    private val binding: FragmentBasicInformationBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: BasicInfoViewModel by viewModels { BasicInfoViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_basic_information, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserverGetBasicInfo()
        setObserverUpdateBasicInfo()
        setFranchiser()

        binding.saveChangesButton.setOnClickListener {
            handleSaveChangesButtonClick()
        }

    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner){franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let {
                binding.saveChangesButton.backgroundTintList = ColorStateList.valueOf(it)
            }
        }
    }

    private fun setObserverGetBasicInfo() {
        viewModel.basicInfo.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmitResult.Loading -> binding.loadingBasicInformation.visibility = View.VISIBLE
                is SubmitResult.Success -> {
                    binding.loadingBasicInformation.visibility = View.GONE
                    setTextField(result.data)
                    updateBasicInfoUI(result.data.customerType)
                }

                is SubmitResult.Empty -> {
                    // Trenutno ne preduzimamo nikakve akcije za praznu vrednost
                }

                is SubmitResult.FailureNoConnection -> showNoConnectionState()
                is SubmitResult.FailureServerError -> {
                    binding.loadingBasicInformation.visibility = View.GONE
                    showMessage(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.loadingBasicInformation.visibility = View.GONE
                    showMessage(result.errorMessage)
                }
                is SubmitResult.InvalidApiToken -> {
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                    showMessage(result.errorMessage)
                }
            }
        }
    }

    private fun setObserverUpdateBasicInfo() {
        viewModel.updateBasicInfoUI.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmitResult.Loading -> binding.loadingBasicInformation.visibility = View.VISIBLE
                is SubmitResult.Success -> {
                    binding.loadingBasicInformation.visibility = View.GONE
                    setTextField(result.data)
                    updateBasicInfoUI(result.data.customerType)
                    showMessage(getString(R.string.change_successfully_saved))

                    //Ponistavam se fokus sa poslednjeg izmenjenog tekstualnog polja
                    view?.clearFocus()
                }

                is SubmitResult.Empty -> {
                    // Trenutno ne preduzimamo nikakve akcije za praznu vrednost
                }

                is SubmitResult.FailureNoConnection -> showNoConnectionState()
                is SubmitResult.FailureServerError -> {
                    binding.loadingBasicInformation.visibility = View.GONE
                    showMessage(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.loadingBasicInformation.visibility = View.GONE
                    showMessage(result.errorMessage)
                }
                is SubmitResult.InvalidApiToken -> {
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                    showMessage(result.errorMessage)
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle),
                getString(R.string.please_connect_to_the_internet)
            )
        }
        findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)
    }

    private fun showNoConnectionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            val hasData = viewModel.existLocalData()
            if (!hasData) {
                showNoInternetDialog()
                waitForInternetAndRetry()
            } else noInternetMessage()
        }
    }

    private suspend fun waitForInternetAndRetry() {
        while (!isAdded || viewLifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
            delay(3000)
            if (viewModel.isInternetAvailable()) {
                triggerUpdate()
                return
            }
        }
    }

    private fun triggerUpdate() {
        lifecycleScope.launch(Dispatchers.Main) {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.loadingBasicInformation.visibility = View.GONE

            viewModel.fetchBasicInfo()
        }
    }

    private fun updateBasicInfoUI(customerType: Int) {
        with(binding) {
            val isCustomer = customerType == 1
            val isBusiness = customerType == 2
            val isBusinessForeign = customerType == 3

            txBasicInfoCompanyName.isVisible = isBusiness || isBusinessForeign
            inputBasicCompanyName.isVisible = isBusiness || isBusinessForeign
            txBasicInfoPib.isVisible = isBusiness || isBusinessForeign
            inputBasicInfoPib.isVisible = isBusiness || isBusinessForeign

            txBasicInfoRegistrationNumber.isVisible = isBusiness
            inputBasicInfoRegistrationNumber.isVisible = isBusiness

            txBasicInfoName.isVisible = isCustomer
            inputBasicInfoName.isVisible = isCustomer
            txBasicInfoSurname.isVisible = isCustomer
            inputBasicInfoSurname.isVisible = isCustomer

            basicInformationCon.isVisible = true
            bottomContainerBasicInfo.isVisible = true
            loadingBasicInformation.visibility = View.GONE
        }
    }


    private fun handleSaveChangesButtonClick() {
        val basicInfo = (viewModel.basicInfo.value as? SubmitResult.Success)?.data
        val customerType = basicInfo?.customerType ?: return

        val firstName = binding.editName.text.toString().trim()
        val lastName = binding.editSurname.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()
        val city = binding.editCity.text.toString().trim()
        val postalCode = binding.editZipCode.text.toString().trim()
        val companyName = binding.editCompanyName.text.toString().trim()
        val mb = binding.editRegistrationNumber.text.toString().trim()
        val pib = binding.editPib.text.toString().trim()

        // Provera svakog polja pojedinačno
        when {
            customerType == 1 && firstName.isEmpty() -> {
                showFieldError(R.string.first_name_mandatory)
                return
            }

            customerType == 1 && lastName.isEmpty() -> {
                showFieldError(R.string.last_name_mandatory)
                return
            }

            (customerType == 2 || customerType == 3) && companyName.isEmpty() -> {
                showFieldError(R.string.company_mandatory)
                return
            }

            (customerType == 2) && mb.isEmpty() -> {
                showFieldError(R.string.registration_mandatory)
                return
            }

            (customerType == 2) && pib.isEmpty() -> {
                showFieldError(R.string.pib_mandatory)
                return
            }


            (customerType == 2) && (pib.length != 9 || !pib.all { it.isDigit() }) -> {
                showFieldError(R.string.pib_not_cantains_nine_digits)
                return
            }

            (customerType == 3) && ((pib.isNotEmpty()) && (pib.length !in 9..13)) -> {
               showFieldError(R.string.pib_invalid_length)
                return
            }

            phone.isEmpty() -> {
                showFieldError(R.string.phone_mandatory)
                return
            }

            address.isEmpty() -> {
                showFieldError(R.string.address_mandatory)
                return
            }

            city.isEmpty() -> {
                showFieldError(R.string.city_mandatory)
                return
            }

            else -> {
                // Ako su sva polja popunjena, kreiramo zahtev za ažuriranje
                try {
                    val userUpdate = when (customerType) {
                        1 -> UpdateUserDataRequest(
                            address = address,
                            city = city,
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            postalCode = postalCode
                        )
                        2 -> UpdateUserDataRequest(
                            address = address,
                            city = city,
                            companyName = companyName,
                            mb = mb,
                            phone = phone,
                            postalCode = postalCode,
                            pib = pib
                        )
                        3 -> UpdateUserDataRequest(
                            address = address,
                            city = city,
                            companyName = companyName,
                            phone = phone,
                            postalCode = postalCode,
                            pib = pib
                        )
                        else -> throw IllegalArgumentException("Invalid customer type in BasicInformationFragment: $customerType")

                    }
                    // Sačuvaj promene
                    viewModel.updateUserData(userUpdate)
                } catch (e: IllegalArgumentException) {
                    Log.e("Error", "Caught exception: ${e.message}")
                }
            }
        }
    }

    // Funkcija za prikazivanje greške
    private fun showFieldError(errorResId: Int) {
        Toast.makeText(requireContext(), getString(errorResId), Toast.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setTextField(data: BasicInfoUIModel) {
        binding.editEmail.setText(data.email)
        binding.editName.setText(data.firstName)
        binding.editSurname.setText(data.lastName)
        binding.editCompanyName.setText(data.companyName)
        binding.editRegistrationNumber.setText(data.mb)
        binding.editPhone.setText(data.phone)
        binding.editAddress.setText(data.address)
        binding.editCity.setText(data.city)
        binding.editZipCode.setText(data.postalCode)
        binding.editCountry.setText(data.countryName)
        binding.editPib.setText(data.pib)
    }

    /**
     * Displays a no-internet message using a SnackBar.
     */
    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

