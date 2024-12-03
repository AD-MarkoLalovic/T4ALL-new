package com.mobility.enp.view.fragments.my_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tags.LostTagResponse
import com.mobility.enp.databinding.FragmentAddTagBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageAddTag
import com.mobility.enp.viewmodel.AddTagViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTagFragment : Fragment() {

    private lateinit var binding: FragmentAddTagBinding
    private val viewModel: AddTagViewModel by viewModels()

    private var data: MutableLiveData<LostTagResponse> = MutableLiveData()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    val TAG = "AddTagFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTagBinding.inflate(inflater, container, false)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.initDatabase()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()

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

    private fun setObservers() {
        data = MutableLiveData()
        errorBody = MutableLiveData()


        data.observe(viewLifecycleOwner, Observer {
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
        })
        errorBody.observe(viewLifecycleOwner, Observer { errorBody ->
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
        })
    }

}