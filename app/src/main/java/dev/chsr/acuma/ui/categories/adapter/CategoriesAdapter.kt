package dev.chsr.acuma.ui.categories.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import dev.chsr.acuma.R
import dev.chsr.acuma.databinding.CategoryItemBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.ui.categories.EditCategoryBottomSheetFragment
import dev.chsr.acuma.ui.history.adapter.formatDate
import dev.chsr.acuma.ui.history.adapter.formatTime
import dev.chsr.acuma.ui.history.adapter.toLocalDateTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext

class CategoriesAdapter(val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var categories: List<Category> = emptyList()
    fun submitList(newList: List<Category>) {
        categories = newList.filter { category -> category.deleted == 0 }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryName.text = category.name
        if (category.goal != null) {
            val balanceText = (category.balance / 100f).toString() + "/" + category.goal / 100f
            holder.binding.categoryBalance.text =
                getSpannableBalanceWithGoal(holder.binding.root.context, balanceText)
            holder.binding.categoryGoalProcess.visibility = View.VISIBLE
            holder.binding.categoryGoalProcess.progress = 100 * category.balance / category.goal
        } else {
            holder.binding.categoryBalance.text = (category.balance / 100f).toString()
            holder.binding.categoryGoalProcess.visibility = View.GONE
        }
        if (category.goalDate != null) {
            val leftDays = ChronoUnit.DAYS.between(LocalDateTime.now(), category.goalDate.toLocalDateTime())
            val leftDaysString = if (leftDays > 0) "($leftDays ${holder.itemView.context.getString(R.string.days)})" else ""
            val resultString = "${category.goalDate.toLocalDateTime().formatDate(holder.itemView.context)} $leftDaysString"

            holder.binding.categoryEarnPerDay.visibility = View.VISIBLE
            holder.binding.categoryGoalDate.visibility = View.VISIBLE
            holder.binding.categoryGoalDate.text = resultString

            if (category.goal != null && leftDays > 0) {
                val earnPerDay = (category.goal - category.balance) / leftDays/100
                holder.binding.categoryEarnPerDay.text = "$earnPerDay/${holder.itemView.context.getString(R.string.day)}"
            }
        } else {
            holder.binding.categoryEarnPerDay.visibility = View.GONE
            holder.binding.categoryGoalDate.visibility = View.VISIBLE
        }

        if (category.percent != 0) {
            holder.binding.categoryName.text =
                getSpannableNameWithPercent(holder.binding.root.context, category)
        }
        holder.binding.root.setOnClickListener {
            val editCategoryBottomSheet = EditCategoryBottomSheetFragment(categories[position])
            editCategoryBottomSheet.show(fragmentManager, "editCategoryBottomSheet")
        }
    }

    private fun getSpannableNameWithPercent(context: Context, category: Category): Spannable {
        val nameText = category.name
        val percentText = " ${category.percent}%"
        val fullText = nameText + percentText

        val spannable = SpannableString(fullText)

        val start = nameText.length
        val end = fullText.length

        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.primaryVariant)
            ),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(18, true),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }


    private fun getSpannableBalanceWithGoal(context: Context, balanceText: String): Spannable {
        val spanBalanceText = SpannableString(balanceText)
        spanBalanceText.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            balanceText.indexOf("/"),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBalanceText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.primary)),
            0,
            balanceText.indexOf("/"),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBalanceText.setSpan(
            StyleSpan(Typeface.NORMAL),
            balanceText.indexOf("/"),
            balanceText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBalanceText.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    context,
                    R.color.category_item_goal_text_color
                )
            ),
            balanceText.indexOf("/"),
            balanceText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spanBalanceText
    }

    override fun getItemCount() = categories.size
}