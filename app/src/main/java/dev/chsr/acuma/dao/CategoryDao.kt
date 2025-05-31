package dev.chsr.acuma.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.chsr.acuma.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): Flow<Category>

    @Query("UPDATE categories SET deleted=:value WHERE id=:id")
    suspend fun setDeleted(id: Int, value: Int)

    @Query("UPDATE categories SET percent=:value WHERE id=:id")
    suspend fun setPercent(id: Int, value: Int)

    @Insert
    suspend fun insertAll(vararg categories: Category)

    @Update
    suspend fun update(category: Category)

    @Query("UPDATE categories SET balance=:value WHERE id=:id")
    suspend fun setBalance(id: Int, value: Int)
}