package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.databinding.FragmentAddTagBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageAddTag
import com.mobility.enp.viewmodel.AddTagViewModel
import com.mobility.enp.viewmodel.FranchiseViewModel

class AddTagFragment : Fragment() {

    private lateinit var binding: FragmentAddTagBinding
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: AddTagViewModel by viewModels()

    private var data: MutableLiveData<LostTagResponse> = MutableLiveData()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    val TAG = "AddTagFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setFranchiser()

        binding.bttConfirmAddTag.setOnClickListener {
            if (Repository.isNetworkAvailable(requireContext())) {
                if (binding.serialNumber.text.toString().trim()
                        .isNotEmpty() || binding.verificationCode.text.toString().trim()
                        .isNotEmpty()
                ) {
                    val serial = binding.serialNumber.text.toString().trim()
                    val verification = binding.verificationCode.text.toString().trim()


                    binding.progBar.visibility = View.VISIBLE
                    viewModel.addTagForUser(serial, verification, data, errorBody)
                    binding.bttConfirmAddTag.isEnabled = false
                } else {
                    Toast.makeText(
                        context,
                        R.string.please_enter_all_required_data,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                val bundle = Bundle().apply {
                    putString(getString(R.string.title), getString(R.string.no_connection_title))
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)
            }

        }

    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.bttConfirmAddTag.backgroundTintList = ColorStateList.valueOf(color)

                val parent = binding.constraintLayout

                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is TextInputLayout) {
                        view.boxStrokeColor = color
                        val editText = view.editText
                        editText?.textSelectHandle?.setTint(color)
                        editText?.setTextColor(color)

                        val states = arrayOf(
                            intArrayOf(android.R.attr.state_pressed),  // pressed
                            intArrayOf(android.R.attr.state_focused),  // focused
                            intArrayOf()                               // default
                        )

                        val colors = intArrayOf(
                            color,        // pressed
                            color,        // focused
                            color         // default
                        )

                        view.cursorColor = ColorStateList(states, colors)
                    }
                }
            }
        }
    }

    private fun setObservers() {
        data = MutableLiveData()
        errorBody = MutableLiveData()


        data.observe(viewLifecycleOwner) {
            binding.progBar.visibility = View.GONE
            binding.bttConfirmAddTag.isEnabled = true
            if (it != null) {
                val fragManager = (context as AppCompatActivity).supportFragmentManager

                val diag = GeneralMessageAddTag(object : GeneralMessageAddTag.OnButtonClick {
                    override fun onClickConfirmed() {
                        findNavController().popBackStack()
                    }
                })

                diag.isCancelable = false
                diag.show(fragManager, "DialogAddTag")
            }
        }
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            binding.progBar.visibility = View.GONE
            binding.bttConfirmAddTag.isEnabled = true

            context?.let { context ->
                Toast.makeText(
                    context,
                    errorBody.errorBody,
                    Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }
    }

}