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
import androidx.core.view.isGone
import dev.chsr.acuma.ui.history.adapter.formatDate
import dev.chsr.acuma.ui.history.adapter.toLocalDateTime
import java.util.Calendar

class CreateCategoryBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCreateCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateCategoryBinding.inflate(inflater, container, false)
        val root = binding.root
        val categoriesViewmodel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]

        val categoryNameText = binding.categoryName
        val categoryGoalText = binding.categoryGoal
        val goalDateDialog = DatePickerDialog(requireContext())
        var goalDateTimestamp: Long? = null
        binding.categoryGoalDateButton.setOnClickListener {
            goalDateDialog.show()
        }
        goalDateDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            goalDateTimestamp = calendar.timeInMillis
            binding.categoryGoalDateButton.text = calendar.timeInMillis.toLocalDateTime().formatDate(requireContext())
        }

        val categoryPercentSlider = binding.categoryPercentSlider
        categoryPercentSlider.setValues(0f)
        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewmodel.categories.collect {
                val percentSum = it.sumOf { category -> if (category.id != -1) category.percent else 0}
                if (percentSum >= 100)
                    categoryPercentSlider.isEnabled = false
                else
                    categoryPercentSlider.valueTo = 100f - percentSum
            }
        }
        val categoryPercentText = binding.categoryPercentText

        categoryPercentText.text = "${categoryPercentSlider.values[0].toInt()}%"
        categoryPercentSlider.addOnChangeListener { _, value, _ ->
            categoryPercentText.text = "${value.toInt()}%"
        }

        val createCategoryButton = binding.createCategoryBtn
        createCategoryButton.setOnClickListener {
            val percent = categoryPercentSlider.values[0].toInt()
            categoriesViewmodel.addCategory(
                Category(
                    name = categoryNameText.text.toString(),
                    goal = if (categoryGoalText.text.toString()
                            .isEmpty()
                    ) null else (categoryGoalText.text.toString().toFloat() * 100).toInt(),
                    percent = percent,
                    goalDate = goalDateTimestamp,
                    description = binding.categoryDescription.text.toString()
                )
            )
            viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewmodel.getById(-1).collect {
                    categoriesViewmodel.setCategoryPercent(-1, it.percent - percent)
                }
            }
            dismiss()
        }

        return root
    }
}