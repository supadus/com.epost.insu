package com.epost.insu.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.data.Data_IUII50M00_F
import com.epost.insu.event.OnListItemClickedEventListener
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.adapter
 * @fileName  : ReqBenefitListAdapter.java
 *
 * @Title     : 보험금지급청구 목록 Adapter
 * @author    : 이수행
 * @created   : 2017-08-02
 * @version   : 1.0
 *
 * @note      : <u>IUII50M00(보험금청구조회)</u><br></br>
 * 보험금청구조회 화면에서 사용하는 Adapter<br></br>
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2017-08-02    이수행     : 최초 등록
 * 02       2020-06-16    노지민     : 접수상태값 추가 (11,12)
 * =======================================================================
 */
class ReqBenefitListAdapter(private val context: Context, private val layoutResourceId: Int) : BaseAdapter() {
    private var listener: OnListItemClickedEventListener? = null
    private val arrData: ArrayList<Data_IUII50M00_F>
    private var flagStartAnim = false
    private var mLastPosition = 0

    internal inner class ViewHolder {
        var imgStatus: ImageView? = null
        var imgAddDoc: ImageView? = null
        var textStatus_1: TextView? = null
        var textStatus_2: TextView? = null
        var textReqDate: TextView? = null
        var textReqType: TextView? = null
        var textCheckMan: TextView? = null
        var textPayDate: TextView? = null
    }

    override fun getCount(): Int {
        return arrData.size
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

            holder = ViewHolder()
            holder.imgStatus = view.findViewById(R.id.imgStatus)
            holder.imgAddDoc = view.findViewById(R.id.imgAddDoc)
            holder.textStatus_1 = view.findViewById(R.id.textStatus_1)
            holder.textStatus_2 = view.findViewById(R.id.textStatus_2)
            holder.textReqDate = view.findViewById(R.id.textReqDate)
            holder.textReqType = view.findViewById(R.id.textReqType)
            holder.textCheckMan = view.findViewById(R.id.textCheckMan)
            holder.textPayDate = view.findViewById(R.id.textPayDate)

            view.setOnClickListener { v ->
                val tmp_index = v.getTag(R.string.tag_index) as Int
                if (listener != null) {
                    listener!!.onClick(tmp_index)
                }
            }
            view.tag = holder
        }
        else {
            flagStartAnim = true
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        view.setTag(R.string.tag_index, position)

        // 서류보완 이미지 표기 체크
        if (arrData[position].CF_isFlagNeedMod()) {
            holder.imgAddDoc?.visibility = View.VISIBLE
        } else {
            holder.imgAddDoc?.visibility = View.INVISIBLE
        }

        // -----------------------------------------------
        //	view에 data 셋팅
        // -----------------------------------------------
        holder.imgStatus?.setImageResource(getStatusImgId(arrData[position].CF_getStatusCode()))
        holder.textStatus_1?.text = arrData[position].CF_getStatusName_1()
        val tmp_status_2 = arrData[position].CF_getStatusName_2()
        if (TextUtils.isEmpty(tmp_status_2)) {
            holder.textStatus_2?.visibility = View.GONE
        } else {
            holder.textStatus_2?.visibility = View.VISIBLE
            holder.textStatus_2?.text = arrData[position].CF_getStatusName_2()
        }
        holder.textReqDate?.text = arrData[position].CF_getReqDate()
        holder.textReqType?.text = arrData[position].CF_getTypeName()
        holder.textCheckMan?.text = arrData[position].CF_getCheckPerson()
        holder.textPayDate?.text = arrData[position].CF_getPayDate()

        // -----------------------------------------------------------------------------------------
        // Animaition : View 재사용 이후 && 다운 스크롤 시에만 애니메이션 실행
        // -----------------------------------------------------------------------------------------
        if (mLastPosition < position && flagStartAnim) {
            view.translationY = CommonFunction.CF_convertDipToPixel(context, 160f).toFloat()
            view.scaleX = 0.8f
            view.scaleY = 0.8f
            view.animate()
                    .setInterpolator(DecelerateInterpolator(1.0f))
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300L)
                    .setListener(null)
        }
        mLastPosition = position
        return view
    }

    /**
     * 상태이미지 리소스 아이디 반환 함수
     * @param p_status 접수상태코드
     * @return 이미지
     */
    private fun getStatusImgId(p_status: String): Int {
        if (p_status == "00" || p_status == "01") {
            return R.drawable.ic_status_0
        } else if (p_status == "02") {
            return R.drawable.ic_status_2
        } else if (p_status == "03") {
            return R.drawable.ic_status_3
        } else if (p_status == "04" || p_status == "05") {
            return R.drawable.ic_status_4
        } else if (p_status == "06") {
            return R.drawable.ic_status_6
        } else if (p_status == "07" || p_status == "08" || p_status == "09" || p_status == "10" || p_status == "11" || p_status == "12") {
            return R.drawable.ic_status_7
        }
        return R.drawable.ic_status_0
    }

    /**
     * clear 함수
     */
    private fun clear() {
        arrData.clear()
    }

    /**
     * 데이터 세팅 함
     * @param p_arrData ArrayList<Data_IUII50M00_F>
    </Data_IUII50M00_F> */
    fun CF_setData(p_arrData: ArrayList<Data_IUII50M00_F>?) {
        clear()
        arrData.addAll(p_arrData!!)
        notifyDataSetChanged()
    }



    /**
     * 아이템 클릭 이벤트 리스너 세팅 함수
     * @param p_listener    OnListItemClickedEventListener
     */
    fun CE_setOnListItemClickedEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * 청구접수 아이디 반환
     * @param p_index   int
     * @return          String
     */
    fun CF_getReqId(p_index: Int): String {
        return arrData[p_index].CF_getReqId()
    }

    fun CF_updateFlagAddDoc(p_reqId: String) {
        for (tmp_data in arrData) {
            if (tmp_data.CF_getReqId() == p_reqId) {
                tmp_data.CF_setFlagNeedMod(false)
                break
            }
        }
        notifyDataSetChanged()
    }

    init {
        arrData = ArrayList()
    }
}