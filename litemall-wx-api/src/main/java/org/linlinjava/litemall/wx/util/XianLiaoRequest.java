package org.linlinjava.litemall.wx.util;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.HttpUtil;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.wx.dto.UserInfo;

public class XianLiaoRequest {

    private static final Log logger = LogFactory.getLog(XianLiaoRequest.class);

    private static final String APPID = "qBSdYLZEuaTMssbI";

    private static final String APPSECRET = "ltKX4HGMUdPIHVQI";

    private static final String URL_REDIRECT = "http://47.111.165.42:8080/wx/auth/auth_by_xianliao";

    private static final String URL_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";

    private static final String URL_ACCESSTOKEN = "https://ssgw.updrips.com/oauth2/accessToken";

    private static final String URL_USERINFO = "https://ssgw.updrips.com/resource/user/getUserInfo";

    private static String getAccessToken(String code) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", APPID);
        params.put("appsecret", APPSECRET);
        params.put("grant_type", "authorization_code");
        params.put("code", code);

        String postResult = HttpUtil.sendPost(URL_ACCESSTOKEN, params);
        logger.info("accessToken postResult:" + postResult);
        Map<String, Object> resultMap = JacksonUtil.toObjectMap(postResult);

        String accessToken = null;
        if (resultMap != null && resultMap.get("err_msg").equals("success")) {
            Map<String, Object> dataMap = (LinkedHashMap) resultMap.get("data");
            if (dataMap != null) {
                accessToken = (String) dataMap.get("access_token");
            }
        }

        return accessToken;
    }

    private static Map<String, Object> getUserInfo(String code) {

        String accessToken = getAccessToken(code);

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", accessToken);

        String postResult = HttpUtil.sendPost(URL_USERINFO, params);
        logger.info("userInfo postResult:" + postResult);
        Map<String, Object> resultMap = JacksonUtil.toObjectMap(postResult);

        Map<String, Object> userInfo = null;
        if (resultMap != null && resultMap.get("err_msg").equals("success")) {
            userInfo = (LinkedHashMap) resultMap.get("data");
        }

        return userInfo;
    }

    public static UserInfo getAuthUserInfo(String code) {
        Map<String, Object> auth = getUserInfo(code);
        UserInfo userInfo = new UserInfo();

        userInfo.setNickName((String) auth.get("nickName"));
        userInfo.setAvatarUrl((String) auth.get("originalAvatar"));
        Byte g = auth.get("gender").equals(1) ? (byte) 1 : (byte) 0;
        userInfo.setGender(g);
        userInfo.setOpenId((String) auth.get("openId"));

        logger.info("userInfo:" + userInfo);
        return userInfo;
    }

    public static String getAuthPage() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", APPID);
        params.put("redirect_uri", URL_REDIRECT);
        params.put("client_secret", "code");
        String authPage = HttpUtil.sendPost(URL_AUTHORIZE, params);

        return authPage;

    }

}
