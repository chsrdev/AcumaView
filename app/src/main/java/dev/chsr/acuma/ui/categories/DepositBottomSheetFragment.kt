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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDepositBinding.inflate(inflater, container, false)
        val root = binding.root
        val categoriesViewmodel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]
        val transactionsViewmodel = ViewModelProvider(
            this,
            TransactionsViewModelFactory(
                TransactionRepository(
                    AppDatabase.getInstance(requireContext()).transactionDao()
                )
            )
        )[TransactionsViewModel::class.java]

        val categoriesSpinner = binding.categoriesSpinner
        var categories: List<Category> = listOf()

        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewmodel.categories.collect { list ->
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
            val amount = (amountText.text.toString().toFloat() * 100).toInt()
            if (categoriesSpinner.selectedItemPosition == 0) {
                categories.forEach { category ->
                    if (amount * category.percent / 100 != 0) {
                        categoriesViewmodel.setCategoryBalance(category.id, category.balance + amount * category.percent / 100)
                        transactionsViewmodel.addTransaction(
                            Transaction(
                                fromId = null,
                                toId = category.id,
                                amount = amount * category.percent / 100,
                                comment = binding.comment.text.toString() + " (${getString(R.string.distribute)} ${amount/100f})",
                                date = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } else {
                val selected = categories[categoriesSpinner.selectedItemPosition - 1]
                categoriesViewmodel.setCategoryBalance(selected.id, selected.balance + amount)
                transactionsViewmodel.addTransaction(
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

        return root
    }
}