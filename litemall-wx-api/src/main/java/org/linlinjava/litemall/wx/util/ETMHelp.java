package org.linlinjava.litemall.wx.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.linlinjava.litemall.core.util.HttpUtil;
import org.linlinjava.litemall.core.util.JacksonUtil;

import com.google.gson.Gson;

public class ETMHelp {
	
	private static final String URL_NEW_ACCOUNT = "http://47.111.160.173:4096/api/accounts/new";
	
	private static final String URL_PAY="http://47.111.160.173:4096/api/dapps/f304169790def442a0f0451347f13b6aadbe12305da59fc2a7c2d81cb94cb27a/transactions/unsigned";
	
	/**
	 * 生成etm帐户
	 * @return
	 */
	public static Map<String,String> newAccount(){
		

		String result = HttpUtil.sendGet(URL_NEW_ACCOUNT, null);
		
		return JacksonUtil.toMap(result);
		
	}
	
	/**
	 * etm 支付
	 * @param params
	 * @return
	 */
	public static Map<String,String> pay(String secret, String address, String amount ){
		
		
		Map<String,String> params = new LinkedHashMap<String, String>();
		List<Object> arg_list = new ArrayList<>();
		arg_list.add(address);
		arg_list.add(amount);
		arg_list.add(1);
		arg_list.add("pay");
		Gson gson = new Gson();
		String arg_json = gson.toJson(arg_list);
		params.put("secret", secret);
		params.put("fee", "0");
		params.put("type", "1000");
		params.put("args", arg_json);
		
		String req_body = gson.toJson(params);
//		System.out.println(req_body);
		String result = HttpUtil.sendPut(URL_PAY, req_body);
		
		return JacksonUtil.toMap(result);
	}
	
	
	public static void main(String[] args) {
		
		
		 Map<String,String> response = pay("cycle gesture wink street sight arena quiz goose fossil girl doctor mirror","A5AbJXqZtx5R9xEnU6cS4KpGGq4cAAUyxX","2000000000");
		 
		 System.out.println(response);
		
	}

}