package com.epost.insu.control;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.DeprecatedFunc;
import com.epost.insu.event.OnSelectedChangeEventListener;

import java.util.ArrayList;


/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : ButtonGroupView.java
 *
 * @Title     : 버튼 그룹 View
 * @author    : 이수행
 * @created   : 2017-10-26
 * @version   : 1.0
 *
 * @note      : <u>버튼 그룹 View</u><br/>
 *              LinearLayout에 버튼을 담는다.<br/>
 *              모드 : 1. 단일선택, 2. 멀티선택<br/>
 *              단일선택 모드는 선택상태 해제가 불가능하다. 멀티선택 모드는 선택상태의 View 클릭시 선택 해제된다.<br/>
 *
 *              {@link #CF_setMode(SelectMode)} 모드 설정 OneSelect : 단일 선택, MultipleSelect 멀티 선택
 *              {@link #CF_isChecked()} 체크 상태 반환<br/>
 *              {@link #CF_setCheck(int)} {@link #CF_setCheck(ArrayList)}체크 상태 설정<br/>
 *              {@link #CF_getCheckdArray()} 체크 상태 반환<br/>
 *              {@link #CF_getMode()} 모드 반환<br/>
 *              {@link #CF_setButtons(String[], boolean[])}  한줄 버튼 세팅
 *              {@link #CF_setButtons(String[][], boolean[])} 여러줄 버튼 세팅
 *
 *              접근성을 위해 선택 상태에 따라 description 설정<br/>
 *              선택 or 해제 시 announce Ment 출력<br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-26    이수행       최초 등록
 * =======================================================================
 */
public class ButtonGroupView extends TableLayout implements View.OnClickListener {

    private final String strSelectedState = " 선택됨";
    private final String strUnSelectedState = " 선택해제됨";
    private final String strDisabledState = "비활성화됨";

    private OnSelectedChangeEventListener listener;             // 선택상태 변경 이벤트 리스너

    @Override
    public void onClick(View v) {
        int tmp_index = arrTextView.indexOf(v);

        if(tmp_index >= 0 && tmp_index < arrChecked.size()){

            if(mode == SelectMode.OneSelect && arrChecked.get(tmp_index) == false){
                // ---------------------------------------------------------------------------------
                //  단일 선택 모드
                // ---------------------------------------------------------------------------------
                for(int i = 0; i < arrTextView.size(); i++){
                    if(i == tmp_index){
                        setBtnSelectedAndDesc(arrTextView.get(i),true);
                        arrChecked.set(i,true);

                        if(listener != null){
                            listener.onSelected(tmp_index);
                        }
                        if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())){
                            arrTextView.get(i).announceForAccessibility(arrTextView.get(i).getText().toString()+" 선택");
                        }
                    }
                    else{
                        setBtnSelectedAndDesc(arrTextView.get(i),false);
                        arrChecked.set(i,false);
                    }
                }
            }
            else if(mode == SelectMode.MultipleSelect){
                // ---------------------------------------------------------------------------------
                //  멀티 선택 모드
                // ---------------------------------------------------------------------------------
                boolean tmp_flagIsSelected = arrTextView.get(tmp_index).isSelected();

                setBtnSelectedAndDesc(arrTextView.get(tmp_index), !tmp_flagIsSelected);
                arrChecked.set(tmp_index, !tmp_flagIsSelected);

                if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())){
                    String tmp_message = arrTextView.get(tmp_index).getText().toString()+" 선택";
                    if(tmp_flagIsSelected){
                        tmp_message = arrTextView.get(tmp_index).getText().toString()+" 선택해제";
                    }
                    arrTextView.get(tmp_index).announceForAccessibility(tmp_message);
                }
            }
        }
    }

    /**
     * enum SELECT MODE<br/>
     * {@link #OneSelect} : 단일 선택, 하나의 아이템만 선택 가능하며 선택상태의 아이템 선택해제가 불가능하다.<br/>
     * {@link #MultipleSelect} : 멀티 선택, 여럿 아이템 선택 가능하며 선택상태의 아이템 선택해제가 가능하다.<br/>
     */
    public enum SelectMode{
        OneSelect,          // 단일 선택
        MultipleSelect      // 멀티 선택
    }

    private ArrayList<TextView> arrTextView;
    private ArrayList<Boolean> arrChecked;

    private SelectMode mode;

    /**
     * 생성자
     * @param context
     */
    public ButtonGroupView(Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public ButtonGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setUIControl();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){

        mode = ButtonGroupView.SelectMode.OneSelect;

        arrTextView = new ArrayList<>();
        arrChecked = new ArrayList<>();
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){
        this.setStretchAllColumns(true);
    }

    /**
     * 버튼 선택상태 세팅 및 description 세팅
     * @param p_view
     * @param p_flagSelect
     */
    private void setBtnSelectedAndDesc(TextView p_view, boolean p_flagSelect){
        p_view.setSelected(p_flagSelect);
        if(p_flagSelect){
            p_view.setContentDescription(p_view.getText()+strSelectedState);
        }else{
            p_view.setContentDescription(p_view.getText()+strUnSelectedState);
        }
    }

    /**
     * 선택 모드 세팅 함수
     * @param p_mode {@link ButtonGroupView.SelectMode}
     */
    public void CF_setMode(SelectMode p_mode){
        mode = p_mode;
    }

    /**
     * 모드 값 반환 함수
     * @return
     */
    @SuppressWarnings("unused")
    public SelectMode CF_getMode(){
        return mode;
    }

    /**
     * 버튼 세팅 함수<br/>
     * TableRow 에 Left / Right 2개의 LinearLayout으로 ViewGroup을 구성하여 버튼(TextView)를 추가한다.
     * @param p_arrTexts  String[][], 1차 배열 수는 row 수, 2차 배열 수는 column 수
     * @param p_flagLeftRight boolean[], column의 left,right 포지션 여부로 column 수와 일치해야한다.
     */
    public void CF_setButtons(String[][] p_arrTexts, boolean[] p_flagLeftRight){

        // 초기화
        removeAllViews();
        arrChecked.clear();
        arrTextView.clear();


        // row 수 만큼 반복
        for(int i = 0 ; i < p_arrTexts.length; i++){

            // -------------------------------------------------------------------------------------
            //  TableRow 생성 : p_arrTexts 이차원 배열 수만큼 TableRow 생성
            // -------------------------------------------------------------------------------------
            TableRow tmp_tableRow = new TableRow(getContext());
            addView(tmp_tableRow);

            // -------------------------------------------------------------------------------------
            //  LayoutParam 생성
            // -------------------------------------------------------------------------------------
            TableRow.LayoutParams tmp_lpRow = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
            tmp_lpRow.weight = 1.0f;
            tmp_lpRow.setMargins(CommonFunction.CF_convertDipToPixel(getContext(),2),
                    0,
                    CommonFunction.CF_convertDipToPixel(getContext(),2),
                    0);

            // -------------------------------------------------------------------------------------
            //  Left, Right ViewGroup 생성
            // -------------------------------------------------------------------------------------
            LinearLayout tmp_linLeft = new LinearLayout(getContext());
            LinearLayout tmp_linRight = new LinearLayout(getContext());
            tmp_linLeft.setLayoutParams(tmp_lpRow);
            tmp_linRight.setLayoutParams(tmp_lpRow);

            tmp_tableRow.addView(tmp_linLeft);
            tmp_tableRow.addView(tmp_linRight);


            // column 수 만큼 반복
            for(int k = 0 ; k < p_arrTexts[i].length; k++){

                // ---------------------------------------------------------------------------------
                //  null 문자열 empty 처리
                // ---------------------------------------------------------------------------------
                String tmp_text = p_arrTexts[i][k];
                if(tmp_text == null){
                    tmp_text = "";
                }

                TextView tmp_textBtn = new TextView(getContext());
                LinearLayout.LayoutParams tmp_lpItem = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                tmp_lpItem.weight = 1.0f;

                // ---------------------------------------------------------------------------------
                //  param p_flagLeftRight를 확인하여 add 위치 확인
                // ---------------------------------------------------------------------------------
                if(p_flagLeftRight[k]){
                    if(tmp_linLeft.getChildCount() > 0){
                        tmp_lpItem.leftMargin = CommonFunction.CF_convertDipToPixel(getContext(),4);
                    }
                    tmp_linLeft.addView(tmp_textBtn);
                }else{
                    if(tmp_linRight.getChildCount() > 0){
                        tmp_lpItem.leftMargin = CommonFunction.CF_convertDipToPixel(getContext(),4);
                    }
                    tmp_linRight.addView(tmp_textBtn);
                }

                // ---------------------------------------------------------------------------------
                //  버튼 설정
                // ---------------------------------------------------------------------------------
                tmp_textBtn.setLayoutParams(tmp_lpItem);
                tmp_textBtn.setText(tmp_text);
                tmp_textBtn.setContentDescription(tmp_text+strUnSelectedState);
                tmp_textBtn.setGravity(Gravity.CENTER);
                tmp_textBtn.setMinWidth(0);
                tmp_textBtn.setMinimumWidth(0);
                tmp_textBtn.setMinHeight(0);
                tmp_textBtn.setMinimumHeight(0);
                tmp_textBtn.setClickable(true);
                tmp_textBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.button_text_small_size));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tmp_textBtn.setStateListAnimator(null);
                }
                tmp_textBtn.setBackgroundResource(R.drawable.btn_r_selector);
                tmp_textBtn.setTextColor(DeprecatedFunc.CF_getColorStateList(getContext(), R.color.text_color_btn_red,null));


                // ---------------------------------------------------------------------------------
                //  버튼 문자열이 empty가 아닌 경우 : click 이벤트 설정 및 Padding 설정
                // ---------------------------------------------------------------------------------
                if(TextUtils.isEmpty(p_arrTexts[i][k])==false) {
                    tmp_textBtn.setOnClickListener(this);
                    tmp_textBtn.setPadding(CommonFunction.CF_convertDipToPixel(getContext(), 8),
                            CommonFunction.CF_convertDipToPixel(getContext(), 12),
                            CommonFunction.CF_convertDipToPixel(getContext(), 8),
                            CommonFunction.CF_convertDipToPixel(getContext(), 12));
                }else{
                    tmp_textBtn.setEnabled(false);
                    tmp_textBtn.setVisibility(View.INVISIBLE);
                }

                arrTextView.add(tmp_textBtn);
                arrChecked.add(false);
            }

            // -------------------------------------------------------------------------------------
            //  empty View를 추가하여 row 사이 간격 추가 : Space 미사용
            // -------------------------------------------------------------------------------------
            if(i < (p_arrTexts.length-1)){
                TableLayout.LayoutParams tmp_lp = new TableLayout.LayoutParams(1, CommonFunction.CF_convertDipToPixel(getContext(),4));

                View tmp_view = new View(getContext());
                tmp_view.setLayoutParams(tmp_lp);

                addView(tmp_view);
            }
        }
    }


    /**
     * 버튼 세팅 함수<br/>
     * TableRow 에 Left / Right 2개의 LinearLayout으로 ViewGroup을 구성하여 버튼(TextView)를 추가한다.
     * @param p_arrTexts  String[], 배열 수는 column 수
     * @param p_flagLeftRight boolean[], column의 left,right 포지션 여부로 column 수와 일치해야한다.
     */
    public void CF_setButtons(String[] p_arrTexts, boolean[] p_flagLeftRight){

        // 초기화
        removeAllViews();
        arrChecked.clear();
        arrTextView.clear();

        // -------------------------------------------------------------------------------------
        //  TableRow 생성 : p_arrTexts 이차원 배열 수만큼 TableRow 생성
        // -------------------------------------------------------------------------------------
        TableRow tmp_tableRow = new TableRow(getContext());
        addView(tmp_tableRow);

        // -------------------------------------------------------------------------------------
        //  LayoutParam 생성
        // -------------------------------------------------------------------------------------
        TableRow.LayoutParams tmp_lpRow = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
        tmp_lpRow.weight = 1.0f;
        tmp_lpRow.setMargins(CommonFunction.CF_convertDipToPixel(getContext(),2),
                0,
                CommonFunction.CF_convertDipToPixel(getContext(),2),
                0);


        // -------------------------------------------------------------------------------------
        //  Left, Right ViewGroup 생성
        // -------------------------------------------------------------------------------------
        LinearLayout tmp_linLeft = new LinearLayout(getContext());
        LinearLayout tmp_linRight = new LinearLayout(getContext());
        tmp_linLeft.setLayoutParams(tmp_lpRow);
        tmp_linRight.setLayoutParams(tmp_lpRow);

        tmp_tableRow.addView(tmp_linLeft);
        tmp_tableRow.addView(tmp_linRight);


        for(int k = 0 ; k < p_arrTexts.length; k++){

            // ---------------------------------------------------------------------------------
            //  null 문자열 empty 처리
            // ---------------------------------------------------------------------------------
            String tmp_text = p_arrTexts[k];
            if(tmp_text == null){
                tmp_text = "";
            }

            TextView tmp_textBtn = new TextView(getContext());
            LinearLayout.LayoutParams tmp_lpItem = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            tmp_lpItem.weight = 1.0f;

            // ---------------------------------------------------------------------------------
            //  param p_flagLeftRight를 확인하여 add 위치 확인
            // ---------------------------------------------------------------------------------
            if(p_flagLeftRight[k%p_flagLeftRight.length]){
                if(tmp_linLeft.getChildCount() > 0){
                    tmp_lpItem.leftMargin = CommonFunction.CF_convertDipToPixel(getContext(),4);
                }
                tmp_linLeft.addView(tmp_textBtn);
            }else{
                if(tmp_linRight.getChildCount() > 0){
                    tmp_lpItem.leftMargin = CommonFunction.CF_convertDipToPixel(getContext(),4);
                }
                tmp_linRight.addView(tmp_textBtn);
            }


            // ---------------------------------------------------------------------------------
            //  버튼 설정
            // ---------------------------------------------------------------------------------
            tmp_textBtn.setLayoutParams(tmp_lpItem);
            tmp_textBtn.setText(tmp_text);
            tmp_textBtn.setContentDescription(tmp_text+strUnSelectedState);
            tmp_textBtn.setGravity(Gravity.CENTER);
            tmp_textBtn.setMinWidth(0);
            tmp_textBtn.setMinimumWidth(0);
            tmp_textBtn.setMinHeight(0);
            tmp_textBtn.setMinimumHeight(0);
            tmp_textBtn.setClickable(true);
            tmp_textBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.button_text_small_size));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tmp_textBtn.setStateListAnimator(null);
            }
            tmp_textBtn.setBackgroundResource(R.drawable.btn_r_selector);
            tmp_textBtn.setTextColor(DeprecatedFunc.CF_getColorStateList(getContext(), R.color.text_color_btn_red,null));

            // -------------------------------------------------------------------------------------
            //  버튼 문자열이 empty가 아닌 경우 : click 이벤트 설정 및 Padding 설정
            // -------------------------------------------------------------------------------------
            if(TextUtils.isEmpty(p_arrTexts[k])==false) {
                tmp_textBtn.setOnClickListener(this);
                tmp_textBtn.setPadding(CommonFunction.CF_convertDipToPixel(getContext(), 8),
                        CommonFunction.CF_convertDipToPixel(getContext(), 12),
                        CommonFunction.CF_convertDipToPixel(getContext(), 8),
                        CommonFunction.CF_convertDipToPixel(getContext(), 12));
            }else{
                tmp_textBtn.setEnabled(false);
                tmp_textBtn.setVisibility(View.INVISIBLE);
            }

            arrTextView.add(tmp_textBtn);
            arrChecked.add(false);
        }
    }

    /**
     * 체크 상태 반환 함수
     * @return
     */
    public ArrayList<Boolean> CF_getCheckdArray(){
        return arrChecked;
    }

    /**
     * 체크 상태인 버튼 index 반환 함수
     * @return
     */
    public int CF_getCheckdButtonFirstIndex(){

        int tmp_index = -1;

        for(int i = 0; i < arrChecked.size(); i++){
            if(arrChecked.get(i)==true){
                tmp_index = i;
                break;
            }
        }

        return tmp_index;
    }

    /**
     * 해당 index 버튼 체크 상태 세팅 함수
     * @param p_index
     */
    public void CF_setCheck(int p_index){

        if(p_index >=0 && p_index < arrTextView.size()) {
            setBtnSelectedAndDesc(arrTextView.get(p_index),true);
            arrChecked.set(p_index, true);

            if (mode == SelectMode.OneSelect) {
                for (int i = 0; i < arrTextView.size(); i++) {
                    if (i != p_index) {
                        setBtnSelectedAndDesc(arrTextView.get(i),false);
                        arrChecked.set(i, false);
                    }
                }
            }
        }
        else{
            for(int i = 0 ; i < arrTextView.size(); i++){
                setBtnSelectedAndDesc(arrTextView.get(i),false);
                arrChecked.set(i, false);
            }
        }
    }

    /**
     * 버튼 체크 상태 세팅 함수
     * @param p_arrCheck
     */
    public void CF_setCheck(ArrayList<Boolean> p_arrCheck){

        if(p_arrCheck.size() == arrTextView.size()) {
            for (int i = 0; i < p_arrCheck.size(); i++) {
                boolean tmp_flagCheck = p_arrCheck.get(i);

                setBtnSelectedAndDesc(arrTextView.get(i),tmp_flagCheck);
                arrChecked.set(i, tmp_flagCheck);
            }
        }
    }

    /**
     * 선택 변경 이벤트 리스너 세팅 함수
     * @param p_listener
     */
    public void CE_setOnSelectedChangeEventListener(OnSelectedChangeEventListener p_listener){
        listener = p_listener;
    }


    /**
     * 체크 상태 반환 함수<br/>
     * 체크 상태인 View가 하나라도 있으면 true를 반환 한다.
     * @return
     */
    public boolean CF_isChecked(){

        boolean tmp_flagCheck = false;

        for(int i = 0 ; i < arrChecked.size(); i++){
            if(arrChecked.get(i)){
                tmp_flagCheck = true;
                break;
            }
        }

        return tmp_flagCheck;
    }

    /**
     * 버튼 수 반환
     * @return
     */
    public int CF_getBtnCount(){
        return arrTextView.size();
    }

    public ArrayList<TextView> getArrTextView() {
        return arrTextView;
    }
}
