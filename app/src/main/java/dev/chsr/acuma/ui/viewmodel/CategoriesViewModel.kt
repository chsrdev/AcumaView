package dev.chsr.acuma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    val categories = categoryRepository.getAllCategories()

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun setDeletedCategory(id: Int, value: Int) {
        viewModelScope.launch {
            categoryRepository.setDeleted(id, value)
        }
    }

    fun setCategoryPercent(id: Int, value: Int){
        viewModelScope.launch {
            categoryRepository.setPercent(id, value)
        }
    }

    fun getById(id: Int): Flow<Category> {
        return categoryRepository.getCategoryById(id)
    }

    fun setCategoryBalance(id: Int, value: Int) {
        viewModelScope.launch {
            categoryRepository.setBalance(id, value)
        }
    }
}

class CategoriesViewModelFactory(private val categoryRepository: CategoryRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesViewModel(categoryRepository) as T
    }
}