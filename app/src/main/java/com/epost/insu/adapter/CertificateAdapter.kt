package com.epost.insu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epost.insu.R
import com.epost.insu.data.Data_CertDetail
import com.epost.insu.event.OnListItemClickedEventListener
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.adapter
 * @fileName  : CertificateAdapter.java
 *
 * @Title     : 공동인증서 목록 Adapter for RecyclerView
 * @author    : 이수행
 * @created   : 2017-07-04
 * @version   : 1.0
 *
 * @note      : 공동인증서 목록 RecyclerView에서 사용한다.<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-02    이수행       최초 등록
 * =======================================================================
 */
class CertificateAdapter(private val context: Context) : RecyclerView.Adapter<CertificateAdapter.ViewHolder>() {
    private var listener: OnListItemClickedEventListener? = null
    private val arrData: ArrayList<Data_CertDetail>

    init {
        arrData = ArrayList()
    }

    /**
     * ViewHolder
     */
    inner class ViewHolder(var rootView: View) : RecyclerView.ViewHolder(rootView) {
        //var position = 0
        var imgIcon: ImageView
        var textOID: TextView
        var textDN: TextView
        var textTO: TextView
        var textCA: TextView

        init {
            imgIcon = rootView.findViewById(R.id.list_certificate_imgIcon)
            textOID = rootView.findViewById(R.id.text_oid)
            textDN = rootView.findViewById(R.id.text_dn)
            textCA = rootView.findViewById(R.id.text_ca)
            textTO = rootView.findViewById(R.id.text_to)
            rootView.setOnClickListener {
                if (listener != null) {
                    listener!!.onClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_certificate, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(p_holder: ViewHolder, p_position: Int) {
        p_holder.textDN.text = arrData[p_position].user
        p_holder.textOID.text = "(" + arrData[p_position].oiD_Readable + ")"
        p_holder.textTO.text = convertExpireDate(arrData[p_position].expirationTo)
        p_holder.textCA.text = context.resources.getString(R.string.label_ca) + " " + arrData[p_position].cA_Readable

        if (arrData[p_position].expire) {
            p_holder.imgIcon.setImageResource(R.drawable.ic_certificate_expire)
        } else {
            p_holder.imgIcon.setImageResource(R.drawable.ic_certificate)
        }
    }

    override fun getItemCount(): Int {
        return arrData.size
    }

    /**
     * 만료일 포멧 변경<br></br>
     * @param p_strDate String
     * @return          String
     */
    private fun convertExpireDate(p_strDate: String): String {
        var tmp_convert = p_strDate
        val tmp_arrDate = p_strDate.split(" ").toTypedArray()
        if (tmp_arrDate.size > 0) {
            tmp_convert = tmp_arrDate[0].replace("-", ".")
        }
        return context.resources.getString(R.string.label_expire_to) + " " + tmp_convert
    }

    private fun clear() {
        arrData.clear()
    }

    /**
     * 데이터 세팅 함수<br></br>
     * 데이터 clear -> 데이터 add -> 갱신<br></br>
     * @param p_arrData ArrayList<Data_CertDetail>
    </Data_CertDetail> */
    fun CF_setData(p_arrData: ArrayList<Data_CertDetail>?) {
        clear()
        arrData.addAll(p_arrData!!)
        notifyDataSetChanged()
    }

    /**
     * 인증서의 실제 index값 반환
     * @param p_itemIndex   int
     * @return              int
     */
    fun CF_getCertificateIndex(p_itemIndex: Int): Int {
        return arrData[p_itemIndex].index
    }

    /**
     * 아이템 클릭 이벤트 리스너 세팅 함수<br></br>
     * @param p_listener    OnListItemClickedEventListener
     */
    fun CE_setOnItemClickEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * 인증서 만료 유무 반환 함수
     * @param p_index   int
     * @return          boolean
     */
    fun CF_isExpire(p_index: Int): Boolean {
        return arrData[p_index].expire
    }
}