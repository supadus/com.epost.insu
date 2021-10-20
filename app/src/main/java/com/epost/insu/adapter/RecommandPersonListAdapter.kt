package com.epost.insu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
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
 * @created   : 2019-04-18
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
class RecommandPersonListAdapter(private val context: Context, private val layoutResourceId: Int) : BaseAdapter() {
    private var listener: OnListItemClickedEventListener? = null // 아이템 클릭 이벤트 리스너

    private var strMaxLength = "" // 가장 긴 금융기관 이름 length
    private val arrRemnNo: ArrayList<String> // 추천인 번호 리스트
    private val arrName: ArrayList<String> // 추천인 이름 리스트

    /**
     * 생성자
     * @param p_context             Context
     * @param p_layoutResourceId    int
     */
    init {
        //초기화
        arrRemnNo = ArrayList()
        arrName = ArrayList()
    }

    /**
     * ViewHolder<br></br>
     */
    internal inner class ViewHolder {
        var tv_name: TextView? = null // 아이템 height를 일치 시키기 위해 뒤에 배치되는 TextView(- 가장 긴 이름 세팅)
        var tv_remnNo: TextView? = null // 추천인 번호
        lateinit var viewHolderCon: LinearLayout
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

    //@SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false)

            // ----------------------------------
            //	ViewHolder 생성 및 세팅
            // ----------------------------------
            holder = ViewHolder()
            holder.tv_name = view.findViewById(R.id.tv_name)
            holder.tv_remnNo = view.findViewById(R.id.tv_remnNo)

            holder.viewHolderCon = view.findViewById(R.id.viewHolderCon)
            holder.viewHolderCon.setOnClickListener(View.OnClickListener { v ->
                val tmp_index = v.getTag(R.string.tag_index) as Int
                if (listener != null) {
                    listener!!.onClick(tmp_index)
                }
            })
            view.tag = holder
        }
        else {
            // -----------------------------------------------
            //	기존에 그려진 Item을 다시 그릴때에는 getTag()를 이용한다.
            // -----------------------------------------------
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        // -- 뷰홀더 세팅
        holder.tv_name?.text = arrName[position] // 추천인 이름
        holder.tv_remnNo?.text = "(" + arrRemnNo[position] + ")" // 추천인 번호
        holder.viewHolderCon?.setTag(R.string.tag_index, position) // 포지션 태그 세팅 (선택버튼)

        return view
    }

    /**
     * clear 함수
     */
    private fun clear() {
        strMaxLength = ""
        arrRemnNo.clear()
        arrName.clear()
    }

    /**
     * 데이터 세팅 함수
     * @param p_arrRemnNO   ArrayList<String>
     * @param p_arrName     ArrayList<String>
    </String></String> */
    fun CF_setData(p_arrRemnNO: ArrayList<String>?, p_arrName: ArrayList<String>?) {
        clear()
        arrRemnNo.addAll(p_arrRemnNO!!)
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
     * @param p_listener    OnListItemClickedEventListener
     */
    fun CE_setOnListItemClickedEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * 해당 index의 코드값 반환 함수
     * @param p_index   int
     * @return  String
     */
    fun CF_getRemnNo(p_index: Int): String {
        return arrRemnNo[p_index]
    }

    /**
     * 해당 index의 이름 반환 함수
     * @param p_index   int
     * @return          String
     */
    fun CF_getName(p_index: Int): String {
        return arrName[p_index]
    }

}