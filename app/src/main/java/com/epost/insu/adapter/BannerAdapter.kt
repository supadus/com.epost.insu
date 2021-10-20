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
 * 배너 이미지 Adapter
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 * 상단 배너이미지 ViewPager에 사용한다.
 * 데이터 세팅 시 Fake Item이 추가된다.
 * ex)
 * 배너 이미지가 5개인 경우 Adapter [.getCount] 는 50개
 * 배너 이미지 수는 [.CF_getRealCount]를 이용한다.
 * ======================================================================
 * 1.0  LSH_20170802    최초 등록
 * 1.1  NJM_20210215    jsonKey_desc key 변경 [하단팝업공지]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class BannerAdapter(private val context: Context) : PagerAdapter() {
    private val arrImgFilePath: ArrayList<String> // 배너 이미지 pth
    private val arrLink: ArrayList<String> // 배너 이미지 link url
    private val arrDesc: ArrayList<String> // 배너 이미지 contentsDescription
    private val arrPagerView: SparseArray<View?> // 배너 이미지 View

    private val maxFakeMultiple = 10
    private var maxFakeItemCount = 0

    init {
        arrImgFilePath = ArrayList()
        arrLink = ArrayList()
        arrDesc = ArrayList()
        arrPagerView = SparseArray()
    }

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
        v = tmp_inflater.inflate(R.layout.layout_banner, null)
        val tmp_imgBanner = v.findViewById<View>(R.id.pager_banner_img) as ImageView
        tmp_imgBanner.setTag(R.string.tag_index, position)
        tmp_imgBanner.contentDescription = arrDesc[position]
        val tmp_file = File(arrImgFilePath[position])
        if (tmp_file.exists()) {
            tmp_imgBanner.setImageBitmap(BitmapFactory.decodeFile(arrImgFilePath[position]))
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
        val tmp_imgBanner = view!!.findViewById<View>(R.id.pager_banner_img) as ImageView
        CommonFunction.CF_recycleBitmap(tmp_imgBanner)
        tmp_imgBanner.setImageBitmap(null)
        view = null
    }

    /**
     * 실제 아이템 수 반환
     * @return  int
     */
    fun CF_getRealCount(): Int {
        return arrImgFilePath.size
    }

    /**
     * 배너 이미지 저장된 파일 경로 반환
     * @param p_index   int
     * @return          String
     */
    fun CF_getFilePath(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrImgFilePath.size) {
            arrImgFilePath[p_index]
        } else ""
    }

    /**
     * 배너 링크 url 반환
     * @param p_index   int
     * @return          String
     */
    fun CF_getLink(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrLink.size) {
            arrLink[p_index]
        } else ""
    }

    /**
     * 배너 이미지 contentsDescription 반환환
     * @param p_index    int
     * @return          String
     */
    fun CF_getDesc(p_index: Int): String {
        return if (p_index >= 0 && p_index < arrDesc.size) arrDesc[p_index] else ""
    }

    /**
     * 배너 정보 갱신
     */
    fun CF_refreshBannerInfo() {
        val jsonKey_path = "savedPath"
        val jsonKey_link = "link"
        val jsonKey_desc = "s_desc"
        arrImgFilePath.clear()
        arrLink.clear()
        arrDesc.clear()
        val tmp_helper = CustomSQLiteHelper(context)
        val tmp_sqlite = tmp_helper.readableDatabase
        try {
            val tmp_jsonArr = tmp_helper.CF_SelectBannerInfo(tmp_sqlite)
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


}