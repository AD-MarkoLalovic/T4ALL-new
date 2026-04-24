package com.mobility.enp.view.adapters.my_invoices_adapters

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_invoices.Bill
import com.mobility.enp.data.model.api_my_invoices.BillData
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.databinding.ItemInvoicesBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.adapters.tool_history.first_screen.HistorySerialAdapter
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class BillsDetailsAdapter(
    private var billData: BillData,
    private val viewModel: MyInvoicesViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val spinnerInterface: MonthlyBillsAdapter.TriggerSpinner,
    private val availableCurrencies: String
) : ListAdapter<Bill, BillsDetailsAdapter.BillsViewHolder>(DiffCallback()) {

    private var billsArray: ArrayList<Bill> = ArrayList(billData.bills)
    private var currentPage = billData.currentPage
    private var lastPage = billData.lastPage
    private var canDownload: Boolean = true

    companion object {
        const val TAG = "BillsAdapter"
    }

    private var countDownTimer: CountDownTimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            canDownload = false
        }

        override fun onFinish() {
            canDownload = true
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Bill>() {

        override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem == newItem
        }
    }

    fun submitInitialData(data: BillData) {
        currentPage = data.currentPage
        lastPage = data.lastPage
        submitList(data.bills)
    }

    inner class BillsViewHolder(val binding: ItemInvoicesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            bill: Bill,
            viewModel: MyInvoicesViewModel
        ) {
            binding.bill = bill

            val billId = bill.id.toString()

            when (bill.status?.value) {
                1 -> {

                    binding.bttPayNow.apply {
                        visibility = View.INVISIBLE
                        isClickable = false
                    }

                    binding.invoicesCardView.strokeColor = ContextCompat.getColor(
                        binding.invoicesCardView.context,
                        R.color.figmaToolHistoryPaidBackground
                    )

                    binding.invoicesStatus.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.invoicesStatus.context,
                            R.drawable.status_icon_green
                        )
                    )

                    binding.firstContainerInvoice.setBackgroundResource(R.color.figmaToolHistoryPaidBackground)
                    binding.secondContainerInvoices.setBackgroundResource(R.color.white)

                }

                7 -> {
                    binding.bttPayNow.apply {
                        visibility = View.INVISIBLE
                        isClickable = false
                    }

                    binding.invoicesCardView.strokeColor = ContextCompat.getColor(
                        binding.invoicesCardView.context,
                        R.color.light_orange
                    )

                    binding.invoicesStatus.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.invoicesStatus.context,
                            R.drawable.status_icon_orange
                        )
                    )

                    binding.firstContainerInvoice.setBackgroundResource(R.color.light_orange)
                    binding.secondContainerInvoices.setBackgroundResource(R.color.white)
                }

                else -> {
                    binding.invoicesCardView.strokeColor = ContextCompat.getColor(
                        binding.invoicesCardView.context,
                        R.color.figmaToolHistoryUnpaidBackground
                    )

                    binding.invoicesStatus.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.invoicesStatus.context,
                            R.drawable.status_icon_red
                        )
                    )
                    binding.firstContainerInvoice.setBackgroundResource(R.color.figmaToolHistoryUnpaidBackground)
                    binding.secondContainerInvoices.setBackgroundResource(R.color.white)
                }
            }

            binding.downloadBillDetails.setOnClickListener {
                showPopupMenu(it, billId)
            }

            binding.bttPayNow.setOnClickListener {

                binding.bttPayNow.backgroundTintList = AppCompatResources.getColorStateList(
                    binding.root.context,
                    R.color.very_light_gray
                )

                binding.bttPayNow.isEnabled = false
                binding.bttPayNow.isClickable = false

                val buttonCompletionListener = object : onApiCallCompletion {
                    override fun apiCompletedSuccess() {

                        binding.bttPayNow.backgroundTintList = AppCompatResources.getColorStateList(
                            binding.root.context,
                            R.color.figmaIntroEclipseColorInactive
                        )

                        binding.bttPayNow.isEnabled = true
                        binding.bttPayNow.isClickable = true
                    }

                }

                spinnerInterface.onStartSpinner()
                viewModel.payBill(billId, buttonCompletionListener)
            }

            binding.executePendingBindings()
        }

        private fun showPopupMenu(view: View, id: String) {

            binding.downloadBillDetails.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.popup_menu
                )
            )

            val context = ContextThemeWrapper(view.context, R.style.CustomPopupMenuStyle)
            val popupMenu = PopupMenu(context, view, Gravity.END, 0, 0)
            popupMenu.menuInflater.inflate(R.menu.downloads_menu, popupMenu.menu)
            popupMenu.setForceShowIcon(true)

            popupMenu.setOnDismissListener {
                binding.downloadBillDetails.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.transparent
                    )
                )
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val isListing = menuItem.itemId == R.id.listingOfPassagesDownload
                val isPdf = menuItem.itemId == R.id.pdfDownload

                if (isPdf || isListing) {

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        showNotification(binding, id, isListing)
                    } else {
                        if (ContextCompat.checkSelfPermission(
                                binding.root.context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Ako je permisija već odobrena  odmah šaljemo notifikaciju
                            showNotification(binding, id, isListing)
                        } else {
                            // Ako nije tražimo od korisnika
                            spinnerInterface.requestNotificationFromUser(object : UserPermission {
                                override fun onPermissionGranted() {
                                    showNotification(binding, id, isListing)
                                }

                                override fun onPermissionDenied() {
                                }
                            })
                        }
                    }
                    true
                } else {
                    false
                }
            }

            popupMenu.show()
        }

    }

    private fun showNotification(
        binding: ItemInvoicesBinding,
        billId: String,
        isListingOfPassages: Boolean
    ) {
        if (canDownload) {
            spinnerInterface.onStartSpinner()
            countDownTimer.start()

            val pdf = ".pdf"
            val downloadName = "$billId$pdf"

            if (isListingOfPassages) {
                // API poziv za listing prolazaka
                viewModel.downloadPassageData(object : DownloadBillsDetails {
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    override fun onOK(pdf: BillDownload?) {
                        pdf?.data?.pdfContent?.let {
                            spinnerInterface.onStopSpinner()
                            viewModel.savePdfToDrive(
                                it,
                                downloadName,
                                binding.root.context,
                                true
                            )
                        }
                    }

                    override fun onFailed() {
                        spinnerInterface.onStopSpinner()
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.download_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, billId)
            } else {
                // API poziv za PDF račun
                viewModel.downloadPdfBill(object : DownloadBillsDetails {
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    override fun onOK(pdf: BillDownload?) {
                        pdf?.data?.pdfContent?.let {
                            spinnerInterface.onStopSpinner()
                            viewModel.savePdfToDrive(
                                it,
                                downloadName,
                                binding.root.context,
                                false
                            )
                        }
                    }

                    override fun onFailed() {
                        spinnerInterface.onStopSpinner()
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.download_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, billId)
            }
        } else {
            Toast.makeText(
                binding.root.context,
                binding.root.context.getString(R.string.download_timeout),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemInvoicesBinding.inflate(layoutInflater, parent, false)
        return BillsViewHolder(binding)
    }

    override fun getItemCount() = billsArray.size

    override fun onBindViewHolder(holder: BillsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, viewModel)

        checkDataFill(position, current, holder.binding.root.context)
    }

    private fun checkDataFill(position: Int, currentBill: Bill, context: Context) {
        if (position == currentList.lastIndex && lastPage > currentPage) {

            val billDetailsFlow =
                MutableStateFlow<SubmitResult<BillsDetailsResponse>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, billDetailsFlow) { serverResponse ->
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        spinnerInterface.onStopSpinner()

                        val data = serverResponse.data.data
                        currentPage = data.currentPage
                        lastPage = data.lastPage

                        val updatedList = currentList.toMutableList()
                        updatedList.addAll(data.bills)

                        submitList(updatedList)
                    }

                    is SubmitResult.FailureServerError -> {
                        spinnerInterface.onStopSpinner()
                        logError(context.getString(R.string.server_error_msg))
                    }

                    is SubmitResult.FailureApiError -> {
                        spinnerInterface.onStopSpinner()
                        logError(context.getString(R.string.api_call_error))
                    }

                    else -> spinnerInterface.onStopSpinner()
                }
            }

            spinnerInterface.onStartSpinner()
            spinnerInterface.pagingUpdateBill(currentPage + 1, billDetailsFlow, availableCurrencies)
        }
    }

    private fun logError(string: String) {
        Log.d(HistorySerialAdapter.TAG, "showError: $string")
    }

    interface DownloadBillsDetails {
        fun onOK(pdf: BillDownload?)
        fun onFailed()
    }

    interface UserPermission {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

    interface onApiCallCompletion {
        fun apiCompletedSuccess()
    }

}