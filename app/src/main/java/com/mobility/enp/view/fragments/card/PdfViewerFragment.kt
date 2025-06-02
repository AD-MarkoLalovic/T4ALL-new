package com.mobility.enp.view.fragments.card

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentPdfViewerBinding
import com.mobility.enp.viewmodel.FranchiseViewModel

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding: FragmentPdfViewerBinding get() = _binding!!

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pdfViewerProgressBar.visibility = View.VISIBLE
        binding.pdfViewerErrorMessage.visibility = View.GONE

        franchiseViewModel.franchiseModel.value?.let { model ->
            binding.pdfViewerBack.setImageResource(model.backButtonResource)
        } ?: binding.pdfViewerBack.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toolbar_shared_back_arrow
            )
        )

        binding.pdfView.fromAsset("Uputstvo-za-registraciju-srpskog-taga-za-Hrvatsku.pdf")
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onLoad { binding.pdfViewerProgressBar.visibility = View.GONE }
            .onError {
                binding.pdfViewerProgressBar.visibility = View.GONE
                binding.pdfViewerErrorMessage.visibility = View.VISIBLE
            }
            .load()

        binding.pdfViewerBack.setOnClickListener {
            val action = PdfViewerFragmentDirections.actionPdfViewerFragmentToPaymentAndPassageFragment("HR")
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}