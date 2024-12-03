package com.mobility.enp.view.fragments.my_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_profile.UpdateUserInfoRequest
import com.mobility.enp.databinding.FragmentBasicInformationBinding
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.viewmodel.BasicInformationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class BasicInformationFragment : Fragment() {

    private var _binding: FragmentBasicInformationBinding? = null
    private val binding: FragmentBasicInformationBinding get() = _binding!!
    private val viewModel: BasicInformationViewModel by viewModels()

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

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setObserver()
        viewModel.fetchPersonalInfo()

        setListener()

        binding.saveChangesButton.setOnClickListener {
            handleSaveChangesButtonClick()
        }

        saveChangesObserve()
    }

    private fun setObserver() {

        // Posmatranje dostupnosti podataka
        collectLatestLifecycleFlow(viewModel.noData) { noData ->
            if (noData) {
                val bind = (activity as MainActivity).binding
                MainActivity.showSnackMessage(
                    getString(R.string.no_internet),
                    bind
                )

                showNoInternetDialog()

                withContext(Dispatchers.IO) {
                    while (true) {
                        if (viewModel.isNetworkAvailable()) {
                            withContext(Dispatchers.Main) {
                                val bindingMain = (activity as MainActivity).binding
                                MainActivity.showSnackMessage(
                                    getString(R.string.connection_restored),
                                    bindingMain
                                )

                                binding.loadingBasicInformation.visibility = View.GONE
                            }
                            viewModel.fetchPersonalInfo()
                            break
                        } else {
                            delay(1000L)
                        }
                    }
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.checkingInternet) { hasInternet ->
            if (!hasInternet) {
                val binding = (activity as MainActivity).binding
                MainActivity.showSnackMessage(
                    getString(R.string.offline_using_stored_data),
                    binding
                )
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

    private fun setListener() {
        collectLatestLifecycleFlow(viewModel.userInfoData) { userInfo ->
            userInfo?.let { // Provjeravamo je li userInfo null
                if (userInfo.companyName == null || userInfo.companyName == "null") {
                    binding.txBasicInfoCompanyName.visibility = View.GONE
                    binding.inputBasicCompanyName.visibility = View.GONE
                    binding.txBasicInfoRegistrationNumber.visibility = View.GONE
                    binding.inputBasicInfoRegistrationNumber.visibility = View.GONE

                    binding.txBasicInfoName.visibility = View.VISIBLE
                    binding.inputBasicInfoName.visibility = View.VISIBLE
                    binding.txBasicInfoSurname.visibility = View.VISIBLE
                    binding.inputBasicInfoSurname.visibility = View.VISIBLE

                } else {
                    binding.txBasicInfoCompanyName.visibility = View.VISIBLE
                    binding.inputBasicCompanyName.visibility = View.VISIBLE
                    binding.txBasicInfoRegistrationNumber.visibility = View.VISIBLE
                    binding.inputBasicInfoRegistrationNumber.visibility = View.VISIBLE

                    binding.txBasicInfoName.visibility = View.GONE
                    binding.inputBasicInfoName.visibility = View.GONE
                    binding.txBasicInfoSurname.visibility = View.GONE
                    binding.inputBasicInfoSurname.visibility = View.GONE

                }
                binding.basicInformationCon.visibility = View.VISIBLE
                binding.bottomContainerMyTags.visibility = View.VISIBLE
                binding.loadingBasicInformation.visibility = View.GONE
            }
        }

    }

    private fun handleSaveChangesButtonClick() {
        val firstName = binding.editName.text.toString()
        val lastName = binding.editSurname.text.toString()
        val phone = binding.editPhone.text.toString()
        val address = binding.editAddress.text.toString()
        val city = binding.editCity.text.toString()
        val postalCode = binding.editZipCode.text.toString()
        val companyName = binding.editCompanyName.text.toString()
        val mb = binding.editRegistrationNumber.text.toString()

        // Provera svakog polja pojedinačno
        when {
            firstName.isEmpty() -> {
                showFieldError(R.string.first_name_mandatory)
                return
            }

            lastName.isEmpty() -> {
                showFieldError(R.string.last_name_mandatory)
                return
            }

            companyName.isEmpty() -> {
                showFieldError(R.string.company_mandatory)
                return
            }

            mb.isEmpty() -> {
                showFieldError(R.string.registration_mandatory)
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
                val userUpdate = if (companyName.equals("null") || companyName.isBlank()) {
                    UpdateUserInfoRequest(
                        firstName,
                        lastName,
                        phone,
                        address,
                        city,
                        postalCode,
                        "null",
                        "null"
                    )
                } else {
                    UpdateUserInfoRequest(
                        "null",
                        "null",
                        phone,
                        address,
                        city,
                        postalCode,
                        companyName,
                        mb
                    )
                }
                // Sačuvaj promene
                viewModel.saveChanges(userUpdate, requireContext())
            }
        }
    }

    // Funkcija za prikazivanje greške
    private fun showFieldError(errorResId: Int) {
        Toast.makeText(requireContext(), getString(errorResId), Toast.LENGTH_LONG).show()
    }


    private fun saveChangesObserve() {
        collectLatestLifecycleFlow(viewModel.saveChangesSuccess) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.change_successfully_saved),
                    Toast.LENGTH_SHORT
                ).show()
                //Ponistavam se fokus sa poslednjeg izmenjenog tekstualnog polja
                view?.clearFocus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

