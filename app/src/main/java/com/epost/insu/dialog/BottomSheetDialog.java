package com.epost.insu.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.epost.insu.R;
import com.epost.insu.adapter.NotiAdapter;
import com.epost.insu.common.CustomViewPager;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.common.WebBrowserHelper;
import com.epost.insu.control.CustomIndicator;
import com.epost.insu.event.OnTapEventListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * 인트로 & 앱메인 > 하단팝업공지
 * @since     :
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.0  NJM_20210128    최초 등록 [하단팝업공지]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class BottomSheetDialog extends BottomSheetDialogFragment {
    View view;
    Context context;

    private NotiAdapter adapterNoti;                  // 노티 이미지 adapter
    private CustomViewPager viewPagerNoti;            // 이미지 viewPager
    private CustomIndicator indicatorNoti;            // 노티 이미지 indicator

    public BottomSheetDialog(Context pContext) {
        this.context = pContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- BottomSheetDialog.onCreateView()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        view = inflater.inflate(R.layout.dialog_bottom_sheet_noti,container,false);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(view.findViewById(R.id.linBottomContent));
        behavior.setHideable(false);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //behavior.setPeekHeight(1800);
        setInit();
        return view;
    }

    public void setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- BottomSheetDialog.setInit()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        // 배너 이미지 Adapter 생성 및 ViewPager 설정
        adapterNoti = new NotiAdapter(context);
        adapterNoti.CF_refreshPopupInfo();
        //indicatorNoti.CF_drawDots(adapterNoti.CF_getRealCount());
        viewPagerNoti = view.findViewById(R.id.viewPagerNoti);
        viewPagerNoti.setAdapter(adapterNoti);
        viewPagerNoti.setAccessibilityDelegate(null);
        viewPagerNoti.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // viewpager 페이지 변경 이벤트 설정
        viewPagerNoti.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // LogPrinter.CF_debug("!-- viewPagerBanner.addOnPageChangeListener.onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                // LogPrinter.CF_debug("!-- viewPagerBanner.addOnPageChangeListener.onPageSelected");
                // ---------------------------------------------------------------------------------
                //  접근성 대체 텍스트 강제 출력
                // ---------------------------------------------------------------------------------
                    int tmp_realCount = adapterNoti.CF_getRealCount();
                    int tmp_curIndex = position % tmp_realCount;
                    String tmp_message = adapterNoti.CF_getDesc(tmp_curIndex);
                    if (position == 0) {
                        tmp_message += ", 첫번째 페이지입니다.";
                    } else if (position == (adapterNoti.getCount() - 1)) {
                        tmp_message += ", 마지막 페이지입니다.";
                    }
                    viewPagerNoti.announceForAccessibility(tmp_message);

                // 배너이미지 indicator 포지션 변경
                indicatorNoti.CF_setCurrentIndex(position%adapterNoti.CF_getRealCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // LogPrinter.CF_debug("!-- viewPagerBanner.addOnPageChangeListener.onPageScrollStateChanged");
            }
        });

        // -- viewpager Tab 이벤트 설정
        viewPagerNoti.CE_setOnTapEventListener(new OnTapEventListener() {
            @Override
            public void onTap(final int p_index) {
                final String tmp_url = adapterNoti.CF_getLink(p_index % adapterNoti.CF_getRealCount());

                // ---------------------------------------------------------------------------------
                //  url 정보가 empty 또는 http 양식이 아닌경우 ActivityNotFoundException 발생
                //  link 정보가 empty인 배너도 있음 => 탭 이벤트 무시
                // ---------------------------------------------------------------------------------
                if(tmp_url.startsWith("http://") || tmp_url.startsWith("https://")) {
                        WebBrowserHelper.callWebBrowser(context, tmp_url);
                }
            }
        });

        // noti indicator Draw
        indicatorNoti = view.findViewById(R.id.indicatorNoti);
        indicatorNoti.CF_drawDots(adapterNoti.CF_getRealCount());

        viewPagerNoti.setCurrentItem(adapterNoti.CF_GetInitPosition());
    }
}
