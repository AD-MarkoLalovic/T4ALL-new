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
import com.mobility.enp.databinding.FragmentBasicInformationBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.ui_models.BasicInfoUIModel
import com.mobility.enp.viewmodel.BasicInfoViewModel

class BasicInformationFragment : Fragment() {

    private var _binding: FragmentBasicInformationBinding? = null
    private val binding: FragmentBasicInformationBinding get() = _binding!!
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

        binding.lifecycleOwner = viewLifecycleOwner

        setObserver()

        /*binding.saveChangesButton.setOnClickListener {
            handleSaveChangesButtonClick()
        }*/

        saveChangesObserve()
    }

    private fun setObserver() {
        viewModel.basicInfo.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmitResult.Loading -> binding.loadingBasicInformation.visibility = View.VISIBLE
                is SubmitResult.Success -> {
                    setTextField(result.data)
                    when (result.data.customerType) {
                        1 -> customer()
                        2 -> business()
                        3 -> businessForeign()
                    }
                }

                is SubmitResult.Empty -> {}
                is SubmitResult.FailureNoConnection -> showNoInternetDialog()
                is SubmitResult.FailureServerError -> {}
                is SubmitResult.FailureApiError -> {}
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

    /*private fun setListener() {
        // Provjeravamo je li userInfo null
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
    }*/

    private fun customer() {
        binding.txBasicInfoCompanyName.visibility = View.GONE
        binding.inputBasicCompanyName.visibility = View.GONE
        binding.txBasicInfoRegistrationNumber.visibility = View.GONE
        binding.inputBasicInfoRegistrationNumber.visibility = View.GONE

        binding.txBasicInfoName.visibility = View.VISIBLE
        binding.inputBasicInfoName.visibility = View.VISIBLE
        binding.txBasicInfoSurname.visibility = View.VISIBLE
        binding.inputBasicInfoSurname.visibility = View.VISIBLE

        binding.basicInformationCon.visibility = View.VISIBLE
        binding.bottomContainerBasicInfo.visibility = View.VISIBLE
        binding.loadingBasicInformation.visibility = View.GONE

    }

    private fun business() {
        binding.txBasicInfoCompanyName.visibility = View.VISIBLE
        binding.inputBasicCompanyName.visibility = View.VISIBLE
        binding.txBasicInfoRegistrationNumber.visibility = View.VISIBLE
        binding.inputBasicInfoRegistrationNumber.visibility = View.VISIBLE

        binding.txBasicInfoName.visibility = View.GONE
        binding.inputBasicInfoName.visibility = View.GONE
        binding.txBasicInfoSurname.visibility = View.GONE
        binding.inputBasicInfoSurname.visibility = View.GONE

        binding.basicInformationCon.visibility = View.VISIBLE
        binding.bottomContainerBasicInfo.visibility = View.VISIBLE
        binding.loadingBasicInformation.visibility = View.GONE
    }

    private fun businessForeign() {
        binding.txBasicInfoCompanyName.visibility = View.VISIBLE
        binding.inputBasicCompanyName.visibility = View.VISIBLE
        binding.txBasicInfoRegistrationNumber.visibility = View.GONE
        binding.inputBasicInfoRegistrationNumber.visibility = View.GONE

        binding.txBasicInfoName.visibility = View.GONE
        binding.inputBasicInfoName.visibility = View.GONE
        binding.txBasicInfoSurname.visibility = View.GONE
        binding.inputBasicInfoSurname.visibility = View.GONE

        binding.basicInformationCon.visibility = View.VISIBLE
        binding.bottomContainerBasicInfo.visibility = View.VISIBLE
        binding.loadingBasicInformation.visibility = View.GONE
    }



    /*private fun handleSaveChangesButtonClick() {
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
    }*/

    // Funkcija za prikazivanje greške
    private fun showFieldError(errorResId: Int) {
        Toast.makeText(requireContext(), getString(errorResId), Toast.LENGTH_LONG).show()
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
    }


    private fun saveChangesObserve() {
        viewModel.saveChangesSuccess.observe(viewLifecycleOwner) { success ->
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

