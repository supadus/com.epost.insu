package com.epost.insu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.epost.insu.R
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.adapter
 * @fileName  : AddressAdapter.java
 *
 * @Title     : 우편번호/주소목록 Adapter
 * @author    : 이수행
 * @created   : 2017-07-25
 * @version   : 1.0
 *
 * @note      :  <u>IUCOF0M01(주소검색)</u><br></br>
 * 주소검색 결과 목록 ListView에 사용한다.<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-07-25    이수행       최초 등록
 * =======================================================================
 */
class AddressAdapter(private val context: Context, private val layoutResourceId: Int) : BaseAdapter() {
    private val arrZipCode: ArrayList<String> // 우편번호 리스트
    private val arrTownName: ArrayList<String> // 동이름 리스트
    private val arrAddrRoad: ArrayList<String> // 도로명 주소 리스트
    private val arrAddrBunji: ArrayList<String> // 지번 주소 리스트

    /**
     * 생성자 type_1
     * @param p_context
     * @param p_layoutResourceId
     */
    init {
        arrZipCode = ArrayList()
        arrTownName = ArrayList()
        arrAddrRoad = ArrayList()
        arrAddrBunji = ArrayList()
    }

    /**
     * ViewHolder<br></br>
     * View를 담아 놓고 꺼내 쓰기 편하게 하기 위해 사용
     * @author MyHome
     */
    internal inner class ViewHolder {
        var textZipCode // 우편번호 TextView
                : TextView? = null
        var textAddrRoad // 도로명 주소 TextView
                : TextView? = null
        var textAddrBunji // 번지 주소 TextView
                : TextView? = null
    }

    override fun getCount(): Int {
        return arrZipCode.size
    }

    /** not use  */
    override fun getItem(arg0: Int): Any? {
        return null
    }

    /** not use  */
    override fun getItemId(arg0: Int): Long {
        return 0
    }

    override fun getView(p_position: Int, p_convertView: View?, p_viewGroup: ViewGroup?): View {
        var p_convertView = p_convertView
        val tmp_viewHolder: ViewHolder
        if (p_convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            p_convertView = layoutInflater.inflate(layoutResourceId, p_viewGroup, false)

            // ----------------------------------
            //	ViewHolder 생성 및 세팅
            // ----------------------------------
            tmp_viewHolder = ViewHolder()
            tmp_viewHolder.textZipCode = p_convertView.findViewById<View>(R.id.textZipCode) as TextView
            tmp_viewHolder.textAddrRoad = p_convertView.findViewById<View>(R.id.textAddrRoad) as TextView
            tmp_viewHolder.textAddrBunji = p_convertView.findViewById<View>(R.id.textAddrBunji) as TextView
            p_convertView.tag = tmp_viewHolder
        } else {
            // -----------------------------------------------
            //	기존에 그려진 Item을 다시 그릴때에는 getTag()를 이용한다.
            // -----------------------------------------------
            tmp_viewHolder = p_convertView.tag as ViewHolder
        }

        //
        tmp_viewHolder.textZipCode!!.text = arrZipCode[p_position]
        tmp_viewHolder.textAddrRoad!!.text = arrAddrRoad[p_position] + " (" + arrTownName[p_position] + ")"
        tmp_viewHolder.textAddrBunji!!.text = arrAddrBunji[p_position]
        return p_convertView!!
    }

    /**
     * clear 함수
     */
    fun CF_clear() {
        arrZipCode.clear()
        arrTownName.clear()
        arrAddrRoad.clear()
        arrAddrBunji.clear()
    }

    /**
     * 데이터 세팅 함수
     * @param p_arrZipcode      zipCode
     * @param p_arrTown         타운명(ex 신림동,풍향동)
     * @param p_arrAddrRoad    도로명 주소
     * @param p_arrAddrBunji   번지 주소
     */
    fun CF_setData(p_arrZipcode: ArrayList<String>?, p_arrTown: ArrayList<String>?, p_arrAddrRoad: ArrayList<String>?, p_arrAddrBunji: ArrayList<String>?) {
        CF_clear()
        arrZipCode.addAll(p_arrZipcode!!)
        arrTownName.addAll(p_arrTown!!)
        arrAddrRoad.addAll(p_arrAddrRoad!!)
        arrAddrBunji.addAll(p_arrAddrBunji!!)
        notifyDataSetChanged()
    }

    /**
     * 해당 index 위치의 zipcode 반환
     * @param p_index
     * @return
     */
    fun CF_getZipCode(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrZipCode.size) {
            arrZipCode[p_index]
        } else ""
    }

    /**
     * 해당 index 위치의 address 반환
     * @param p_index
     * @return
     */
    fun CF_getAddrRoad(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrAddrRoad.size) {
            arrAddrRoad[p_index]
        } else ""
    }

    /**
     * 해당 index 위치의 townName 반환
     * @param p_index
     * @return
     */
    fun CF_getTownName(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrTownName.size) {
            arrTownName[p_index]
        } else ""
    }

}