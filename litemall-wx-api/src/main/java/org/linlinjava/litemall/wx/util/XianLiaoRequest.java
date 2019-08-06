package org.linlinjava.litemall.wx.util;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.linlinjava.litemall.core.util.HttpUtil;
import org.linlinjava.litemall.core.util.JacksonUtil;

public class XianLiaoRequest {

	private static final String APPID = "";

	private static final String APPSECRET = "";

	private static final String URL_ACCESSTOKEN = "https://ssgw.updrips.com/oauth2/accessToken";

	private static final String URL_USERINFO = "https://ssgw.updrips.com/resource/user/getUserInfo";

	public Map<String, String> getAccessToken(String code) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", APPID);
		params.put("appsecret", APPSECRET);
		params.put("grant_type", "authorization_code");
		params.put("code", code);

		String result = HttpUtil.sendPost(URL_ACCESSTOKEN, params);
		Map<String, String> resultMap = JacksonUtil.toMap(result);
		String data = resultMap.get("data");
		return JacksonUtil.toMap(data);

	}

	public Map<String, String> getUserInfo(String code) {
		
		Map<String, String> token = getAccessToken(code);
		String access_token = token.get("access_token");
				

		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", access_token);
		
		String result = HttpUtil.sendPost(URL_USERINFO, params);
		Map<String, String> resultMap = JacksonUtil.toMap(result);
		String data = resultMap.get("data");
		return JacksonUtil.toMap(data);

	}

}
