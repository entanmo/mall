package org.linlinjava.litemall.admin.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.HttpUtil;
import org.linlinjava.litemall.core.util.JacksonUtil;

import com.google.gson.Gson;

public class ETMHelp {
	
	private static final String URL_HOST = "http://47.111.160.173:4096";
	//private static final String URL_HOST = "http://192.168.2.58:4096";
	
	public static final String DAPP_ID ="f304169790def442a0f0451347f13b6aadbe12305da59fc2a7c2d81cb94cb27a";
	//public static final String DAPP_ID ="351eeae3766ad08b5219a5fc707cbd54ab2f1c97e94d2c57e65576a357806190";
	
	private static final String URL_NEW_ACCOUNT = URL_HOST + "/api/accounts/new";
	
	private static final String URL_DAPP_TRANSACTIONS=URL_HOST + "/api/dapps/"+DAPP_ID+"/transactions/unsigned";
	
	
	public static final String COLLECTIONS_ADDRESS = "AFgqezrZEhLWUftzvyZTt6UWAGddGuYkNk";
	
	private static final String COLLECTIONS_SECRET = "inmate ceiling wear never breeze exit economy citizen false tape category stereo";
	
	
	private final Log logger = LogFactory.getLog(ETMHelp.class);
	
	/**
	 * 生成etm帐户
	 * @return
	 */
	public static Map<String,String> newAccount(){
		

		String result = HttpUtil.sendGet(URL_NEW_ACCOUNT, null);
		
		return JacksonUtil.toMap(result);
		
	}
	
	
	/**
	 * etm 退款
	 * @param address
	 * @return
	 */
	public static Map<String,String> refund(String address, String amount ){
		
		
		List<Object> arg_list = new ArrayList<>();
		arg_list.add(address);
		arg_list.add(amount);
		arg_list.add(1);
		arg_list.add("pay");
		Gson gson = new Gson();
		String arg_json = gson.toJson(arg_list);
	
		String result = unsigned(COLLECTIONS_SECRET,"0",1000,arg_json);
		
		return JacksonUtil.toMap(result);
	}
	


	private static String unsigned(String secret , String fee, int type,String args) {
		Map<String,Object> params = new LinkedHashMap<String, Object>();
		params.put("secret", secret);
		params.put("fee", fee);
		params.put("type", type);
		params.put("args", args);
		
		Gson gson = new Gson();
		String req_body = gson.toJson(params);
		System.out.println(req_body);
		String result = HttpUtil.sendPut(URL_DAPP_TRANSACTIONS, req_body);
		
		return result;
	}
	

	
	
	public static void main(String[] args) {
		
		
		 //Map<String,String> response = pay("cycle gesture wink street sight arena quiz goose fossil girl doctor mirror","A5AbJXqZtx5R9xEnU6cS4KpGGq4cAAUyxX","2000000000");
		//String result = HttpUtil.sendGet(URL_NEW_ACCOUNT, null);
		//Map<String,String> result = recharge("cycle gesture wink street sight arena quiz goose fossil girl doctor mirror", "f304169790def442a0f0451347f13b6aadbe12305da59fc2a7c2d81cb94cb27a", 2000000000, null);
	
		//提取
	/*	Map<String, String> result1 = ETMHelp.draw1("cycle gesture wink street sight arena quiz goose fossil girl doctor mirror", "2500000000", ADMIN_ADDRESS);
		if("true".equals(result1.get("success"))) {
			
			String transactionId1 = result1.get("transactionId");
			
			System.out.println(transactionId1);
			Map<String, String> result2 = ETMHelp.draw2(ADMIN_SECRET, "2500000000");
			
			if("true".equals(result2.get("success"))) {
				
				String transactionId2 = result2.get("transactionId");
				System.out.println(transactionId2);
				Map<String, String> result3 = ETMHelp.draw3(ADMIN_SECRET, "A5J8ofziMprcqEktm5i2nKoRNZwLADMN9q", 2500000000L);
				
				if("true".equals(result3.get("success"))) {
					
					String transactionId3 = result3.get("transactionId");
					System.out.println(transactionId3);
				}else {
					
				}
			}else {
				
			}
		} 
		*/
		//String result = HttpUtil.sendGet("http://47.111.160.173:4096/api/accounts/getBalance?address=A5J8ofziMprcqEktm5i2nKoRNZwLADMN9q", "");
		 //Map<String,Object> result = getBalanceList("A5GfRnWWBZL5EAQMQvpfmhJP65J3rTaW2Z");
		
//		System.out.println(checkAccount("A5J8ofziMprcqEktm5i2nKoRNZwLADMN9q"));
//		getEtmPriceOkex();
		//String result = HttpUtil.sendGet("http://47.111.160.173:4096/api/accounts/getBalance?address=A5J8ofziMprcqEktm5i2nKoRNZwLADMN9q", "");

		System.out.println(newAccount());
		
	}

}