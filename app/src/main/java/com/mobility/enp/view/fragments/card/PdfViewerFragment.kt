package com.mobility.enp.view.fragments.card

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mobility.enp.databinding.FragmentPdfViewerBinding

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding: FragmentPdfViewerBinding get() = _binding!!

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