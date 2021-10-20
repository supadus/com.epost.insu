package com.epost.insu.common;

import com.epost.insu.EnvConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <br/> copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * <br/> project   : 모바일슈랑스 구축
 * <br/> Title     : UrlUtils
 *
 * <pre>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2019-12-16    노지민     : 최초 등록
 * =======================================================================
 * </pre>
 *
 * @version   : 1.0
 * @author    : 노지민
 */
public class UrlUtils {

    /**
     * host url 유무 체크 후 url 세팅 리턴
     * @param url   String
     * @return      String
     */
    public static String hostUrlCheck(String url) {
        // host URL이 없으면
        if(!url.startsWith("https:") && !url.startsWith("http")) {
            url = EnvConfig.host_url + url;
        }
        return url;
    }


    /**
     * URL에서 파라미터를 파싱한다.
     * @param pQuery
     * @return
     */
    public static Map<String, String> getUrlQueryMap(String pQuery) {
        if(pQuery == null) return null;

        int pos1= pQuery.indexOf("?");
        if (pos1 >= 0) {
            pQuery = pQuery.substring(pos1+1);
        }

        String[] params = pQuery.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name  = param.split("=")[0];
            String value = param.split("=")[1];

            try {
                value	= URLDecoder.decode(value, "UTF-8");		// 리턴URL
            } catch (UnsupportedEncodingException e) {
                e.getMessage();
            }
            map.put(name, value);
        }

        // -- 로그
        if(map != null) {
            Set<String> keys = map.keySet();
            int index = 0;
            for(String key : keys ) {
                LogPrinter.CF_debug("[" + index + "]" + " name: " + keys + " | value: " + map.get(key));
            }
        }

        return map;
    }
}
