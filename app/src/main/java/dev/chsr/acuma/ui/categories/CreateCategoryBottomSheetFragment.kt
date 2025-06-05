package dev.chsr.acuma.ui.categories

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetCreateCategoryBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import kotlinx.coroutines.launch
import dev.chsr.acuma.ui.history.adapter.formatDate
import dev.chsr.acuma.ui.history.adapter.toLocalDateTime
import java.util.Calendar

class CreateCategoryBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCreateCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoriesViewModel: CategoriesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateCategoryBinding.inflate(inflater, container, false)

        categoriesViewModel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]

        val rootView = binding.root
        val nameTextInput = binding.categoryName
        val descriptionTextInput = binding.categoryDescription
        val goalTextInput = binding.categoryGoal
        val maxBalanceTextInput = binding.categoryMaxBalance
        val goalDateButton = binding.categoryGoalDateButton
        val percentSlider = binding.categoryPercentSlider
        val percentTextView = binding.categoryPercentText
        val createButton = binding.createCategoryBtn

        val goalDateDialog = DatePickerDialog(requireContext())
        var goalDateTimestamp: Long? = null
        goalDateButton.setOnClickListener {
            goalDateDialog.show()
        }
        goalDateDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            goalDateTimestamp = calendar.timeInMillis
            goalDateButton.text =
                calendar.timeInMillis.toLocalDateTime().formatDate(requireContext())
        }

        percentSlider.setValues(0f)
        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewModel.categories.collect {
                val percentSum =
                    it.sumOf { category -> if (category.id != -1) category.percent else 0 }
                if (percentSum >= 100)
                    percentSlider.isEnabled = false
                else
                    percentSlider.valueTo = 100f - percentSum
            }
        }

        percentTextView.text = "${percentSlider.values[0].toInt()}%"
        percentSlider.addOnChangeListener { _, value, _ ->
            percentTextView.text = "${value.toInt()}%"
        }

        createButton.setOnClickListener {
            val percent = percentSlider.values[0].toInt()
            categoriesViewModel.addCategory(
                Category(
                    name = nameTextInput.text.toString(),
                    goal = if (goalTextInput.text.isNullOrBlank())
                        null
                    else (goalTextInput.text.toString().toFloat() * 100).toInt(),
                    percent = percent,
                    goalDate = goalDateTimestamp,
                    description = descriptionTextInput.text.toString(),
                    maxBalance = if (maxBalanceTextInput.text.isNullOrBlank())
                        null
                    else (maxBalanceTextInput.text.toString().toFloat() * 100).toInt()
                )
            )
            viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewModel.getById(-1).collect {
                    categoriesViewModel.setCategoryPercent(-1, it.percent - percent)
                }
            }
            dismiss()
        }

        return rootView
    }
}