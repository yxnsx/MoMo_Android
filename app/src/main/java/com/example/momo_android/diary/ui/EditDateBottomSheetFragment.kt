package com.example.momo_android.diary.ui

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.example.momo_android.R
import com.example.momo_android.databinding.BottomsheetDiaryEditDateBinding
import com.example.momo_android.diary.ui.DiaryActivity.Companion.diary_date
import com.example.momo_android.diary.ui.DiaryActivity.Companion.diary_month
import com.example.momo_android.diary.ui.DiaryActivity.Companion.diary_year
import com.example.momo_android.util.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class EditDateBottomSheetFragment(val itemClick: (IntArray) -> Unit) : BottomSheetDialogFragment() {

    private var _Binding: BottomsheetDiaryEditDateBinding? = null
    private val Binding get() = _Binding!!

    override fun getTheme(): Int = R.style.RoundBottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(activity!!, theme)

        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
            BottomSheetBehavior.from(bottomSheet).isHideable = true
        }

        // 모달 밖 영역 탭하면 모달 닫힘
        bottomSheetDialog.apply {
            setCanceledOnTouchOutside(true)
        }

        return bottomSheetDialog
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _Binding = BottomsheetDiaryEditDateBinding.inflate(layoutInflater)
        return Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val year = Binding.includeYmdPicker.year
        val month = Binding.includeYmdPicker.month
        val date = Binding.includeYmdPicker.date

        // divider 색깔 투명으로 변경하는 함수
        fun NumberPicker.removeDivider() {
            val pickerFields = NumberPicker::class.java.declaredFields
            for (pf in pickerFields) {
                if (pf.name == "mSelectionDivider") {
                    pf.isAccessible = true
                    try {
                        val colorDrawable = ColorDrawable(Color.TRANSPARENT)
                        pf[this] = colorDrawable
                    } catch (e: java.lang.IllegalArgumentException) {
                        // log exception here
                    } catch (e: Resources.NotFoundException) {
                        // log exception here
                    } catch (e: IllegalAccessException) {
                        // log exception here
                    }
                    break
                }
            }
        }

        year.removeDivider()
        month.removeDivider()
        date.removeDivider()

        // 현재 날짜 가져오기
        val currentDate = Calendar.getInstance()

        // minValue = 최소 날짜 표시
        year.minValue = 2020
        month.minValue = 1
        date.minValue = 1

        // maxValue = 최대 날짜 표시
        year.maxValue = 2021

        // year에 따라 month maxValue 변경
        if(diary_year == currentDate.get(Calendar.YEAR)) {
            month.maxValue = currentDate.get(Calendar.MONTH) + 1
        } else {
            month.maxValue = 12
        }

        // month에 따라 month, date maxValue 변경
        if(diary_month == currentDate.get(Calendar.MONTH) + 1) {
            month.maxValue = currentDate.get(Calendar.MONTH) + 1
            date.maxValue = currentDate.get(Calendar.DAY_OF_MONTH)
        } else {
            setMonthMax()
        }

        // 일단은
        Log.d("companion", diary_year.toString())
        Log.d("companion", diary_month.toString())
        Log.d("companion", diary_date.toString())
        year.value = diary_year
        month.value = diary_month
        date.value = diary_date

        // 순환 안되게 막기

       year.wrapSelectorWheel = false
        month.wrapSelectorWheel = false
        date.wrapSelectorWheel = false

        // edittext 입력 방지
        year.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        month.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        date.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        // year picker change listener
        year.setOnValueChangedListener { _, _, _ ->

            if(year.value == currentDate.get(Calendar.YEAR)) {
                month.maxValue = currentDate.get(Calendar.MONTH) + 1
                date.maxValue = currentDate.get(Calendar.DAY_OF_MONTH)
            } else {
                month.value = currentDate.get(Calendar.MONTH) + 1
                date.value = currentDate.get(Calendar.DAY_OF_MONTH)
                month.maxValue = 12
                setMonthMax()
            }

        }

        // month picker change listener
        month.setOnValueChangedListener { _, _, _ ->

            if(year.value == currentDate.get(Calendar.YEAR) && month.value == currentDate.get(
                            Calendar.MONTH) + 1) {
                // 현재 년도에 현재 날짜일 때
                month.maxValue = currentDate.get(Calendar.MONTH) + 1
                date.maxValue = currentDate.get(Calendar.DAY_OF_MONTH)
            } else {
                month.maxValue = 12
                setMonthMax()
            }

        }

        Binding.btnDiaryDateEdit.isEnabled = true

        Binding.btnDiaryDateEdit.setOnClickListener {
            val pick = intArrayOf(year.value, month.value, date.value)
            itemClick(pick)

            // 날짜수정 통신

            context!!.showToast("날짜가 수정되었습니다.")
            dialog?.dismiss()
        }

        Binding.btnCloseDiaryEditDate.setOnClickListener {
            dialog?.dismiss()
        }



    }

    // 달 별로 일수 다른거 미리 세팅해둔 함수
    private fun setMonthMax() {
        val month = Binding.includeYmdPicker.month
        val date = Binding.includeYmdPicker.date

        when (month.value) {
            2 -> {
                date.maxValue = 29
            }
            4, 6, 9, 11 -> {
                date.maxValue = 30
            }
            1, 3, 5, 7, 8, 10, 12 -> {
                date.maxValue = 31
            }
        }
    }




}