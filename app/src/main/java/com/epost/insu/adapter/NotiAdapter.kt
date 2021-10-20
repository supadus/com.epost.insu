package com.epost.insu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteHelper
import com.epost.insu.common.LogPrinter
import org.json.JSONException
import java.io.File
import java.util.*

/**
 * 하단팝업공지 Adapter
 * @since     :
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * 메인화면 하단 공지이미지 ViewPager에 사용한다.
 * ex) 공지 이미지 수는 [.CF_getRealCount]를 이용한다.
 * ======================================================================
 * 1.0  NJM_20210209    최초 등록 [하단팝업공지]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class NotiAdapter(private val context: Context) : PagerAdapter() {

    private val arrImgFilePath: ArrayList<String> // 공지 이미지 path
    private val arrLink: ArrayList<String> // 공지 이미지 link url
    private val arrDesc: ArrayList<String> // 공지 이미지 contentsDescription
    private val arrPagerView: SparseArray<View?> // 공지 이미지 View

    private val maxFakeMultiple = 1
    private var maxFakeItemCount = 0

    override fun getCount(): Int {
        return if (arrImgFilePath.size > 0) {
            maxFakeItemCount
        } else 0
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(pager: View, obj: Any): Boolean {
        return pager === obj
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var position = position
        var v: View? = null
        if (position >= arrImgFilePath.size) {
            position = position % arrImgFilePath.size
        }
        val tmp_inflater = LayoutInflater.from(context)
        v = tmp_inflater.inflate(R.layout.layout_noti, null)
        val tmp_imgNoti = v.findViewById<ImageView>(R.id.pager_noti_img)
        tmp_imgNoti.setTag(R.string.tag_index, position)
        tmp_imgNoti.contentDescription = arrDesc[position]
        val tmp_file = File(arrImgFilePath[position])
        if (tmp_file.exists()) {
            tmp_imgNoti.setImageBitmap(BitmapFactory.decodeFile(arrImgFilePath[position]))
        }
        container.addView(v, 0)
        arrPagerView.put(position, v)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        var view: View? = `object` as View
        container.removeView(view)
        arrPagerView.remove(position)

        // 이미지 recycle
        val tmp_imgNoti = view!!.findViewById<ImageView>(R.id.pager_noti_img)
        CommonFunction.CF_recycleBitmap(tmp_imgNoti)
        tmp_imgNoti.setImageBitmap(null)
        view = null
    }

    /**
     * 실제 아이템 수 반환
     * @return  int
     */
    fun CF_getRealCount(): Int {
        return arrImgFilePath.size
    }
    //    /**
    //     * 공지 이미지 저장된 파일 경로 반환
    //     * @param p_index   int
    //     * @return          String
    //     */
    //    public String CF_getFilePath(int p_index){
    //        if(p_index >= 0 && p_index < arrImgFilePath.size()) {
    //            return arrImgFilePath.get(p_index);
    //        }
    //        return  "";
    //    }

    /**
     * 공지 링크 url 반환
     * @param p_index   int
     * @return          String
     */
    fun CF_getLink(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrLink.size) {
            arrLink[p_index]
        } else ""
    }

    /**
     * 공지 이미지 contentsDescription 반환환
     * @param p_index   int
     * @return          String
     */
    fun CF_getDesc(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrDesc.size) arrDesc[p_index] else ""
    }

    /**
     * 공지 정보 갱신
     */
    fun CF_refreshPopupInfo() {
        val jsonKey_path = "savedPath"
        val jsonKey_link = "link"
        val jsonKey_desc = "s_desc"

        arrImgFilePath.clear()
        arrLink.clear()
        arrDesc.clear()
        val tmp_helper = CustomSQLiteHelper(context)
        val tmp_sqlite = tmp_helper.readableDatabase
        try {
            val tmp_jsonArr = tmp_helper.CF_SelectPopupInfo(tmp_sqlite)
            for (i in 0 until tmp_jsonArr.length()) {
                val tmp_jsonData = tmp_jsonArr.getJSONObject(i)
                arrImgFilePath.add(tmp_jsonData.getString(jsonKey_path))
                arrLink.add(tmp_jsonData.getString(jsonKey_link))
                arrDesc.add("배너 멀티페이지, " + tmp_jsonData.getString(jsonKey_desc) + ", " + tmp_jsonArr.length() + "항목 중 " + (i + 1) + "항목")
            }
        } catch (e: JSONException) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(context.resources.getString(R.string.log_json_exception))
        }
        tmp_sqlite.close()
        tmp_helper.close()

        // 페이크 아이템 카운트 세팅 : 실제 배너 수의 maxFakeMultiple배
        maxFakeItemCount = arrImgFilePath.size * maxFakeMultiple
        notifyDataSetChanged()
    }

    /**
     * 초기 포지션 반환
     * @return  int
     */
    fun CF_GetInitPosition(): Int {
        if (CF_getRealCount() > 0) {
            val tmp_centerPosition = maxFakeItemCount / 2
            return tmp_centerPosition - tmp_centerPosition % CF_getRealCount()
        }
        return -1
    }

    init {
        arrImgFilePath = ArrayList()
        arrLink = ArrayList()
        arrDesc = ArrayList()
        arrPagerView = SparseArray()
    }
}