package com.epost.insu.common

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.epost.insu.R

object UiUtil {
      fun setTitleColor(view:TextView,str:String,start:Int,end:Int){

        val tmp_spannable: Spannable = SpannableString(str)
        tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
         view.setText(tmp_spannable)
     }

}