package dev.chsr.acuma.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.R
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetDepositBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.repository.TransactionRepository
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModel
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModelFactory
import kotlinx.coroutines.launch


class DepositBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetDepositBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var transactionsViewModel: TransactionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDepositBinding.inflate(inflater, container, false)

        categoriesViewModel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]
        transactionsViewModel = ViewModelProvider(
            this,
            TransactionsViewModelFactory(
                TransactionRepository(
                    AppDatabase.getInstance(requireContext()).transactionDao()
                )
            )
        )[TransactionsViewModel::class.java]

        val rootView = binding.root
        val categoriesSpinner = binding.categoriesSpinner
        var categories: List<Category> = listOf()

        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewModel.categories.collect { list ->
                categories = list.filter { category -> category.deleted == 0 }

                val names = mutableListOf(getString(R.string.distribute))
                names.addAll(categories.map { it.name })

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoriesSpinner.adapter = spinnerAdapter
            }
        }

        val amountText = binding.amount
        val depositButton = binding.depositBtn

        depositButton.setOnClickListener {
            if (amountText.text.isNullOrBlank()) return@setOnClickListener

            val amount = (amountText.text.toString().toFloat() * 100).toInt()
            if (categoriesSpinner.selectedItemPosition == 0) {
                distribute(categories, amount)
            } else {
                val selected = categories[categoriesSpinner.selectedItemPosition - 1]
                categoriesViewModel.setCategoryBalance(selected.id, selected.balance + amount)
                transactionsViewModel.addTransaction(
                    Transaction(
                        fromId = null,
                        toId = selected.id,
                        amount = amount,
                        comment = binding.comment.text.toString(),
                        date = System.currentTimeMillis()
                    )
                )
            }

            dismiss()
        }

        return rootView
    }

    private fun distribute(
        categories: List<Category>,
        amountToDistribute: Int
    ) {
        var left = amountToDistribute
        categories.forEach { category ->
            if (category.id == -1) return@forEach

            var categoryShare = amountToDistribute * category.percent / 100
            if (category.maxBalance != null) {
                val balanceGap = category.maxBalance - category.balance
                if (categoryShare > balanceGap)
                    categoryShare = balanceGap

            }

            if (categoryShare <= 0) return@forEach

            categoriesViewModel.setCategoryBalance(
                category.id,
                category.balance + categoryShare
            )
            transactionsViewModel.addTransaction(
                Transaction(
                    fromId = null,
                    toId = category.id,
                    amount = categoryShare,
                    comment = binding.comment.text.toString() + " (${getString(R.string.distribute)} ${amountToDistribute / 100f})",
                    date = System.currentTimeMillis()
                )
            )

            left -= categoryShare
        }

        Log.d("depositDistribute", left.toString())
        if (left != 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewModel.getById(-1).collect { reserve ->
                    categoriesViewModel.setCategoryBalance(
                        -1, // reserve category
                        reserve.balance + left
                    )
                    transactionsViewModel.addTransaction(
                        Transaction(
                            fromId = null,
                            toId = -1,
                            amount = left,
                            comment = binding.comment.text.toString() + " (${getString(R.string.distribute)} ${amountToDistribute / 100f})",
                            date = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}