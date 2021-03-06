package org.linlinjava.litemall.wx.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.okcoin.commons.okex.open.api.bean.spot.result.Ticker;
import com.okcoin.commons.okex.open.api.config.APIConfiguration;
import com.okcoin.commons.okex.open.api.enums.I18nEnum;
import com.okcoin.commons.okex.open.api.service.spot.SpotProductAPIService;
import com.okcoin.commons.okex.open.api.service.spot.impl.SpotProductAPIServiceImpl;
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
	
	private static final String URL_COIN_RECHARGE = URL_HOST + "/api/dapps/transaction";
	
	private static final String URL_ETM_TRANSACTIONS = URL_HOST + "/api/transactions";
	
	private static final String URL_ETM_DAPP_TRANSACTIONS_GET = URL_HOST + "/api/dapps/"+DAPP_ID+"/transactions/";
	
	private static final String URL_ETM_ACCOUNT_CHEACK=URL_HOST + "/api/accounts";
	
	public static final String COLLECTIONS_ADDRESS = "AFgqezrZEhLWUftzvyZTt6UWAGddGuYkNk";
	
	public static final String ADMIN_SECRET = "idle despair fence doctor claw market scare squeeze group custom divorce similar";
	
	public static final String ADMIN_ADDRESS = "A7BCSUYQe8wShBUaAyw9YqQ5kiZ7qi5SAJ";
	
	public static final long  TRANSACTION_COST = 10000000L;
	
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
	 * 验证etm address
	 * @param address
	 * @return
	 */
	public static boolean checkAccount(String address) {
		String result = HttpUtil.sendGet(URL_ETM_ACCOUNT_CHEACK+"?address="+address, null);
		Map<String,Object> map = JacksonUtil.toObjectMap(result);
		return(Boolean)map.get("success");
	}
	
	/**
	 * 验证etm address
	 * @param transaction
	 * @return
	 */
	public static boolean checkTransactions(String transaction) {
		String result = HttpUtil.sendGet(URL_ETM_DAPP_TRANSACTIONS_GET+transaction, null);
		Map<String,Object> map = JacksonUtil.toObjectMap(result);
		return(Boolean)map.get("success");
	}	
	
	/**
	 * etm 支付
	 * @param address
	 * @return
	 */
	public static Map<String,String> pay(String secret, String address, String amount ){
		
		
		List<Object> arg_list = new ArrayList<>();
		arg_list.add(address);
		arg_list.add(amount);
		arg_list.add(1);
		arg_list.add("pay");
		Gson gson = new Gson();
		String arg_json = gson.toJson(arg_list);
	
		String result = unsigned(secret,"0",1000,arg_json);
		
		return JacksonUtil.toMap(result);
	}
	
	/**
	 * 提取 setp 1
	 * @param amount
	 * @return
	 */
	public static Map<String,String> draw1(String secret,  String amount ,String address ){
		
		
		List<Object> arg_list = new ArrayList<>();
		arg_list.add("ETM");
		arg_list.add(amount);
		arg_list.add(address);
		Gson gson = new Gson();
		String arg_json = gson.toJson(arg_list);
	
		String result = unsigned(secret,"0",3,arg_json);
		
		return JacksonUtil.toMap(result);
	}
	
	
	/**
	 * 提取 setp 2
	 * @param secret
	 * @return
	 */
	public static Map<String,String> draw2(String secret,  String amount ){
		
		
		List<Object> arg_list = new ArrayList<>();
		arg_list.add("ETM");
		arg_list.add(amount);
	
		Gson gson = new Gson();
		String arg_json = gson.toJson(arg_list);
	
		String result = unsigned(secret,"0",2,arg_json);
		
		return JacksonUtil.toMap(result);
	}
	
	/**
	 * 提取 setp 3
	 * @param recipientAddress
	 * @return
	 */
	public static Map<String,String> draw3(String secret, String recipientAddress, long amount ){
		
		Map<String,Object> params = new LinkedHashMap<String, Object>();
		params.put("secret", secret);
		params.put("recipientId", recipientAddress);
		params.put("amount", amount);
		params.put("message", "提取");
		
		Gson gson = new Gson();
		String req_body = gson.toJson(params);
		
		//System.out.println(req_body);
		String result = HttpUtil.sendPut(URL_ETM_TRANSACTIONS, req_body);
		
		return JacksonUtil.toMap(result);
	}
	
	
	/**
	 * 提取 
	 * @param recipientAddress
	 * @return
	 */
	public static Map<String,String> draw(String secret, String recipientAddress, String amount ){
		
		List<Object> arg_list = new LinkedList<>();
		arg_list.add(recipientAddress);
		arg_list.add(amount);
		Gson gson = new Gson();
		String req_body = gson.toJson(arg_list);
	
			
		//System.out.println(req_body);
		String result = unsigned(secret,"0",1001,req_body);
	
		
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
	
	/**
	 * 充值
	 * @param secret
	 * @param dappId
	 * @param amount
	 * @param secondSecret
	 * @return
	 */
	public static Map<String,String> recharge(String secret, String dappId, long amount ,String secondSecret){
		
		
		Map<String,Object> params = new LinkedHashMap<String, Object>();
	
		params.put("secret", secret);
		params.put("dappId", dappId);
		params.put("amount", amount);
		params.put("currency","ETM");
		if(secondSecret != null) {
			params.put("secondSecret", secondSecret);
		}
		Gson gson = new Gson();
		String req_body = gson.toJson(params);
//		System.out.println(req_body);
		String result = HttpUtil.sendPut(URL_COIN_RECHARGE, req_body);
		
		return JacksonUtil.toMap(result);
	}
	
	/**
	 * 主链余额
	 * @param address
	 * @return
	 */
	public static Map<String,String> getBalance(String address) {
		
		
		String result = HttpUtil.sendGet(URL_HOST + "/api/accounts/getBalance?address="+ address, "");
		
		return JacksonUtil.toMap(result);
	}
	
	/**
	 * Dapp余额
	 * @param address
	 * @return
	 */
	public static Map<String,String> getDappBalance(String address) {
		
		
		String result = HttpUtil.sendGet(URL_HOST + "/api/dapps/"+DAPP_ID+"/sugram/balance?address="+ address, "");
		
		return JacksonUtil.toMap(result);
	}
	
	
	/**
	 * 主链充值记录
	 * @param address
	 * @return
	 */
	public static Map<String,Object> getBalanceList(String address) {
		
		String url = URL_ETM_TRANSACTIONS + "?type=6&senderId=" + address+"&orderBy=t_timestamp&and=1";
		//System.out.println(url);
		String result = HttpUtil.sendGet(url , "");
		
		return JacksonUtil.toObjectMap(result);
	}
	
	public static Object getEtmTickOkex(){
		final APIConfiguration config = new APIConfiguration();

		config.setEndpoint("https://www.okex.com/");
		config.setApiKey("");
		config.setSecretKey("");
		config.setPassphrase("");
		config.setPrint(true);
		config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);
		SpotProductAPIService spotProductAPIService = new SpotProductAPIServiceImpl(config);
		Ticker ticker = null;
		try {
			 ticker = spotProductAPIService.getTickerByProductId("ETM-USDT");
		}catch (Exception e){
			System.out.println(e);
		}

		// eg: {"epoch":"1520848286.633","iso":"2018-03-12T09:51:26.633Z"}
		return ticker;
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

//		System.out.println(newAccount().get("secret"));
		
	}

}