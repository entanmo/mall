package org.linlinjava.litemall.wx.util;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.linlinjava.litemall.core.util.HttpUtil;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.wx.dto.UserInfo;

public class XianLiaoRequest {

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
        Map<String, String> resultMap = JacksonUtil.toMap(postResult);

        String accessToken = null;
        if (resultMap != null && resultMap.get("err_msg").equals("success")) {
            String data = resultMap.get("data");
            Map<String, String> dataMap = JacksonUtil.toMap(data);
            if (dataMap != null) {
                accessToken = dataMap.get("access_token");
            }
        }

        return accessToken;
    }

    private static String getUserInfo(String code) {

        String accessToken = getAccessToken(code);

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", accessToken);

        String result = HttpUtil.sendPost(URL_USERINFO, params);
        Map<String, String> resultMap = JacksonUtil.toMap(result);

        String userInfo = null;
        if (resultMap != null && resultMap.get("err_msg").equals("success")) {
            userInfo = resultMap.get("data");
        }

        return userInfo;
    }

    public static UserInfo getAuthUserInfo(String code) {
        String auth = getUserInfo(code);
        UserInfo userInfo = new UserInfo();

        userInfo.setNickName(JacksonUtil.parseString(auth, "nickName"));
        userInfo.setAvatarUrl(JacksonUtil.parseString(auth, "originalAvatar"));
        userInfo.setGender(JacksonUtil.parseByte(auth, "gender"));
        userInfo.setOpenId(JacksonUtil.parseString(auth, "openId"));

        return userInfo;
    }

    public static String getAuthPage(){
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", APPID);
        params.put("redirect_uri", URL_REDIRECT);
        params.put("client_secret", "code");
        String authPage = HttpUtil.sendPost(URL_AUTHORIZE, params);

        return authPage;

    }

}
