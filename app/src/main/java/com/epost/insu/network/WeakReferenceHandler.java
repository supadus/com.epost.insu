package com.epost.insu.network;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Context 직접 참조를 파하기 위해 WeakReference 사용</br>
 * Handler 때문에 Component GC가 안되는 문제 해결위해 WeakReference 사용</br>
 *
 * <pre>
 *     WeakReferenceHandler tmp_handler = new WeakReferenceHandler(WeakReferenceHandler.ObjectHandlerMessage);
 *     // 메시지 처리는 일반 핸들러와 동일
 * </pre>
 *
 * Created by 이수행 on 2016-06-22
 */

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.network
 * @fileName  : WeakReferenceHandler.java
 *
 * @Title     : MSG 처리를 위한 Handler
 * @author    : 이수행
 * @created   : 2017-09-07
 * @version   : 1.0
 *
 * @note      : <u>MSG 처리를 위한 Handler</u><br/>
 *               Context 직접 참조를 파하기 위해 WeakReference 사용<br/>
 *               Handler 때문에 Component GC가 안되는 문제 해결위해 WeakReference 사용</br>
 *
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-07    이수행       최초 등록
 * =======================================================================
 */
public class WeakReferenceHandler extends Handler {

    /**
     * 메시지 인터페이스
     */
    public interface ObjectHandlerMessage{
        void handleMessage(Message p_message);
    }

    /** WeakReference */
    private final WeakReference<ObjectHandlerMessage> weakReferenceObject;

    /**
     * 생성자
     * @param p_object
     */
    public WeakReferenceHandler(ObjectHandlerMessage p_object){
        weakReferenceObject = new WeakReference<ObjectHandlerMessage>(p_object);
    }

    @Override
    public void handleMessage(Message p_message)
    {
        super.handleMessage(p_message);
        ObjectHandlerMessage tmp_object = weakReferenceObject.get();

        if(tmp_object == null){
            return;
        }
        tmp_object.handleMessage(p_message);
    }

}