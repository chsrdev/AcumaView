package dev.chsr.acuma.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val percent: Int = 0,
    val balance: Int = 0,
    val goal: Int?,
    val deleted: Int = 0,
    @ColumnInfo("is_hidden")
    val isHidden: Int = 0,
    @ColumnInfo("goal_date")
    val goalDate: Long?,
    val description: String?,
    @ColumnInfo("max_balance") val maxBalance: Int?
)
