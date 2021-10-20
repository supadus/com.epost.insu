package com.epost.insu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.event.OnListItemClickedEventListener
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.adapter
 * @fileName  : BankListAdapter.java
 *
 * @Title     : 금융기관 목록 Adapter
 * @author    : 이수행
 * @created   : 2017-08-31
 * @version   : 1.0
 *
 * @note      : <u>금융기관 목록 Adapter      (화면 ID : IUII31M00)</u><br></br>
 * GridView의 column Row를 균등하게 맞추기 위해 INVISIBLE 상태의 TextView를 사용한다.<br></br>
 * INVISIBLE 상태의 TextView에는 전체 데이터 중 length가 가장 큰 TEXT가 세팅된다.<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-31    이수행       최초 등록
 * =======================================================================
 */
class BankListAdapter(private val context: Context, private val layoutResourceId: Int) : BaseAdapter() {
    private var listener: OnListItemClickedEventListener? = null // 아이템 클릭 이벤트 리스너
    private var strMaxLength= "" // 가장 긴 금융기관 이름 length

    private val arrCode: ArrayList<String> // 금융기관 코드 리스트
    private val arrName: ArrayList<String> // 금융기관 이름 리스트

    /**
     * 생성자 type_1
     * @param p_context
     * @param p_layoutResourceId
     */
    init {
        arrCode = ArrayList()
        arrName = ArrayList()
    }

    /**
     * ViewHolder<br></br>
     * View를 담아 놓고 꺼내 쓰기 편하게 하기 위해 사용
     * @author MyHome
     */
    internal inner class ViewHolder {
        var textShadowName: TextView? = null // 아이템 height를 일치 시키기 위해 뒤에 배치되는 TextView(- 가장 긴 이름 세팅)
        var btn: Button? = null // 아이템 선택 버튼
    }

    override fun getCount(): Int {
        return arrName.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false)

            // ----------------------------------
            //	ViewHolder 생성 및 세팅
            // ----------------------------------
            holder = ViewHolder()
            holder.textShadowName = view.findViewById(R.id.textView)

            holder.btn = view.findViewById(R.id.btn)
            holder.btn?.setOnClickListener { v ->
                val tmp_index = v.getTag(R.string.tag_index) as Int
                if (listener != null) {
                    listener!!.onClick(tmp_index)
                }
            }
            view.tag = holder
        }
        else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        // TEXT 세팅
        holder.btn?.setTag(R.string.tag_index, position) // 포지션 태그 세팅
        holder.textShadowName?.text = strMaxLength
        holder.btn?.text = arrName[position]

        return view
    }

    /**
     * 금융기관 이름 변환
     * @param p_bankName
     * @return
     */
    private fun convertBankName(p_bankName: String): String {
        var tmp_convertBankName = p_bankName
        if (p_bankName == "상호저축은행중앙회") {
            tmp_convertBankName = "상호저축\r\n은행중앙회"
        } else if (p_bankName == "신용협동조합중앙회") {
            tmp_convertBankName = "신용협동\r\n조합중앙회"
        } else if (p_bankName == "한화투자증권주식회사") {
            tmp_convertBankName = "한화투자증권\r\n주식회사"
        } else if (p_bankName == "K.E.B(한국자금융)") {
            tmp_convertBankName = "K.E.B\r\n(한국자금융)"
        } else if (p_bankName == "은행연합회(온라인)") {
            tmp_convertBankName = "은행연합회\r\n(온라인)"
        } else if (p_bankName == "미래에셋대우증권") {
            tmp_convertBankName = "미래에섯\r\n대우증권"
        } else if (p_bankName == "한화투자증권주식회사") {
            tmp_convertBankName = "한화투자증권\r\n주식회사"
        } else if (p_bankName == "하나금융투자증권") {
            tmp_convertBankName = "하나금융\r\n투자증권"
        } else if (p_bankName == "펀드온라인코리아") {
            tmp_convertBankName = "펀드온라인\r\n코리아"
        }
        return tmp_convertBankName
    }

    /**
     * clear 함수
     */
    private fun clear() {
        strMaxLength = ""
        arrCode.clear()
        arrName.clear()
    }

    /**
     * 데이터 세팅 함수
     * @param p_arrName
     */
    fun CF_setData(p_arrCode: ArrayList<String>?, p_arrName: ArrayList<String>?) {
        clear()
        arrCode.addAll(p_arrCode!!)
        arrName.addAll(p_arrName!!)
        for (i in arrName.indices) {
            if (arrName[i].length > strMaxLength.length) {
                strMaxLength = arrName[i]
            }
        }
        notifyDataSetChanged()
    }

    /**
     * 아이템 클릭 이벤트 리스터 함수<br></br>
     * [android.widget.AdapterView.OnItemClickListener] 대용으로 사용한다.
     * @param p_listener
     */
    fun CE_setOnListItemClickedEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * 해당 index의 코드값 반환 함수
     * @param p_index
     * @return
     */
    fun CF_getCode(p_index: Int): String {
        return arrCode[p_index]
    }

    /**
     * 해당 index의 이름 반환 함수
     * @param p_index
     * @return
     */
    fun CF_getName(p_index: Int): String {
        return arrName[p_index]
    }
}