package org.linlinjava.litemall.wx.web;

import static org.linlinjava.litemall.wx.util.WxResponseCode.PAY_CODE_ERROR_PASSWORD;
import static org.linlinjava.litemall.wx.util.WxResponseCode.PAY_CODE_FAIL;
import static org.linlinjava.litemall.wx.util.WxResponseCode.PAY_CODE_ORDERSN_UNKNOWN;
import static org.linlinjava.litemall.wx.util.WxResponseCode.PAY_CODE_UPDATE_FAIL;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.util.bcrypt.BCryptPasswordEncoder;
import org.linlinjava.litemall.db.domain.LitemallDraw;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.domain.LitemallUser;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.service.LitemallUserService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.linlinjava.litemall.wx.util.ETMHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收货地址服务
 */
@RestController
@RequestMapping("/wx/pay")
@Validated
public class WxPayController {
	private final Log logger = LogFactory.getLog(WxPayController.class);

	@Autowired
	private LitemallUserService userService;

	@Autowired
	private LitemallOrderService orderService;

	/**
	 * Etm 链支付
	 *
	 * @param userId
	 *            用户ID
	 * @return 收货地址列表
	 */
	@PostMapping("etm")
	public Object etm(@LoginUser Integer userId, @RequestBody String body) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}

		LitemallUser user = userService.findById(userId);
		String secret = user.getSecret();
		String payPassword = JacksonUtil.parseString(body, "payPassword");
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		if( !encoder.matches(payPassword, user.getPayPassword())){

			return ResponseUtil.fail(PAY_CODE_ERROR_PASSWORD,"支付密码错误");
		}
		
		
		String amount = JacksonUtil.parseString(body, "amount");
		Map<String, String> result = ETMHelp.pay(secret, ETMHelp.COLLECTIONS_ADDRESS, amount);
		
		if("true".equals(result.get("success"))) {
			
			String orderSn = JacksonUtil.parseString(body, "orderSn");
			LitemallOrder order = orderService.findBySn(orderSn);
			if (order == null) {
				return ResponseUtil.fail(PAY_CODE_ORDERSN_UNKNOWN,"订单不存在 sn=" + orderSn);
			}
			
			// 检查这个订单是否已经处理过
			if (OrderUtil.isPayStatus(order) && order.getPayId() != null) {
				return ResponseUtil.ok("订单已经处理成功!");
			}
			
			
			
			order.setPayId(result.get("transactionId"));
			order.setPayTime(LocalDateTime.now());
			order.setOrderStatus(OrderUtil.STATUS_PAY);
			if (orderService.updateWithOptimisticLocker(order) == 0) {
				// 这里可能存在这样一个问题，用户支付和系统自动取消订单发生在同时
				// 如果数据库首先因为系统自动取消订单而更新了订单状态；
				// 此时用户支付完成回调这里也要更新数据库，而由于乐观锁机制这里的更新会失败
				// 因此，这里会重新读取数据库检查状态是否是订单自动取消，如果是则更新成支付状态。
				order = orderService.findBySn(orderSn);
				int updated = 0;
				if (OrderUtil.isAutoCancelStatus(order)) {
					order.setPayId(result.get("transactionId"));
					order.setPayTime(LocalDateTime.now());
					order.setOrderStatus(OrderUtil.STATUS_PAY);
					updated = orderService.updateWithOptimisticLocker(order);
				}
				
				// 如果updated是0，那么数据库更新失败
				if (updated == 0) {
					return ResponseUtil.fail(PAY_CODE_UPDATE_FAIL,"更新数据已失效");
				}
			}
			return ResponseUtil.ok("处理成功!");
		}else {
			logger.error(result);
			String msg = "";
			 if("Error: Apply transaction error: Error: Insufficient balance".equals(result.get("error"))) {
				 msg = "余额不足";
			 }
			 return ResponseUtil.fail(PAY_CODE_FAIL,"etm 支付失败: " + msg);
		}
		
	}
	
	
	/**
	 * Etm 充值
	 *
	 * @param userId
	 *            用户ID
	 * @return 收货地址列表
	 */
	@PostMapping("recharge")
	public Object recharge(@LoginUser Integer userId, @RequestBody String body) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}

		LitemallUser user = userService.findById(userId);
		String secret = user.getSecret();
		String amount = JacksonUtil.parseString(body, "amount");
		Map<String, String> result = ETMHelp.recharge(secret, ETMHelp.DAPP_ID, Long.valueOf(amount), null);
		
		if("true".equals(result.get("success"))) {
			
						
			return ResponseUtil.ok("处理成功!");
		}else {
			logger.error(result);
			
			return ResponseUtil.fail(PAY_CODE_FAIL,"etm 充值失败! ");
		}
		
	}
	
/*	*//**
	 * Etm 提取
	 *
	 * @param userId
	 *            用户ID
	 * @return 
	 *//*
	@PostMapping("draw")
	public Object draw(@LoginUser Integer userId, @RequestBody String body) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}

		LitemallUser user = userService.findById(userId);
		String secret = user.getSecret();
		String amount = JacksonUtil.parseString(body, "amount");
		String address = JacksonUtil.parseString(body, "address");
		
		if(!ETMHelp.checkAccount(address)) {
			return ResponseUtil.fail(PAY_CODE_FAIL,"地址不符合要求!");
		}
		
		
		String transactionId1 = "";
		String transactionId2 = "";
		String transactionId3 = "";		
		LitemallDraw draw = new LitemallDraw();
		draw.setAddress(address);
		draw.setUserid(userId);
		draw.setSecret(secret);
		draw.setAmount(amount);
		
		Map<String, String> result1 = ETMHelp.draw1(secret, amount, ETMHelp.ADMIN_ADDRESS);
		if("true".equals(result1.get("success"))) {
			
			transactionId1 = result1.get("transactionId");
			//查交易是否打包	
			WaitingTransaction(transactionId1);	
			Map<String, String> result2 = ETMHelp.draw2(ETMHelp.ADMIN_SECRET, amount);
			
			if("true".equals(result2.get("success"))) {
				
				transactionId2 = result2.get("transactionId");
				//查交易是否打包								
				WaitingTransaction(transactionId2);				
				
				if(Long.parseLong(amount) > ETMHelp.TRANSACTION_COST) {
					Map<String, String> result3 = ETMHelp.draw3(ETMHelp.ADMIN_SECRET, address, Long.parseLong(amount) - ETMHelp.TRANSACTION_COST);
					
					if("true".equals(result3.get("success"))) {
						
						transactionId3 = result3.get("transactionId");
						
					}else {
						logger.error("draw step 3...");
						logger.error(result3);
					}
				}else {
					return ResponseUtil.fail(PAY_CODE_FAIL,"etm 手续费不足! ");
				}
				
			}else {
				logger.error("draw step 2...");
				logger.error(result2);
			}
		}else {
			logger.error("draw step 1...");
			logger.error(result1);
			
			return ResponseUtil.fail(PAY_CODE_FAIL,"etm 提取失败! ");
		}
		
		draw.setTransactionid1(transactionId1);
		draw.setTransactionid2(transactionId2);
		draw.setTransactionid3(transactionId3);
		userService.addDrawRecord(draw);
		return ResponseUtil.ok("处理成功!");
		
	}
	*/
	
	/**
	 * Etm 提取
	 *
	 * @param userId
	 *            用户ID
	 * @return 
	 */
	@PostMapping("draw")
	public Object draw(@LoginUser Integer userId, @RequestBody String body) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}

		LitemallUser user = userService.findById(userId);
		String secret = user.getSecret();
		String amount = JacksonUtil.parseString(body, "amount");
		String address = JacksonUtil.parseString(body, "address");
		
		if(!ETMHelp.checkAccount(address)) {
			return ResponseUtil.fail(PAY_CODE_FAIL,"地址不符合要求!");
		}
		if(Long.parseLong(amount) <= ETMHelp.TRANSACTION_COST) {
			return ResponseUtil.fail(PAY_CODE_FAIL,"etm 手续费不足! ");
		}
		
		String transactionId1 = "";
	
		LitemallDraw draw = new LitemallDraw();
		draw.setAddress(address);
		draw.setUserid(userId);
		draw.setSecret(secret);
		draw.setAmount(amount);
		
		Map<String, String> result = ETMHelp.draw(secret,address , amount);
		if("true".equals(result.get("success"))) {
			
			transactionId1 = result.get("transactionId");
			draw.setTransactionid1(transactionId1);
			userService.addDrawRecord(draw);
		}else {
			logger.error(result);
			return ResponseUtil.fail(PAY_CODE_FAIL,"etm 提取失败! ");
		}			
			
				
		
		return ResponseUtil.ok("处理成功!");
		
	}
	
	/**
	 * 等待查交打包	
	 * @param transactionId
	 */
	private void WaitingTransaction(String transactionId) {
		
		while (!ETMHelp.checkTransactions(transactionId)) {
			try {
				logger.info("Waiting for transaction confirmation ......["+transactionId+"]");
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Etm 余额
	 *
	 * @param userId
	 *            用户ID
	 * @return 收货地址列表
	 */
	@GetMapping("balance")
	public Object balance(@LoginUser Integer userId) {
		
		if (userId == null) {
			return ResponseUtil.unlogin();
		}

		LitemallUser user = userService.findById(userId);
		Map<String,String> result = ETMHelp.getBalance(user.getAddress());
		if("true".equals(result.get("success"))) {
			
			return ResponseUtil.ok(result.get("balance"));
		}
		return ResponseUtil.fail(PAY_CODE_FAIL,result.get("error"));
		
	}
	
	/**
	 * dapp 余额
	 * @param userId
	 * @param body
	 * @return
	 */
	@GetMapping("dapp/balance")
	public Object dappBalance(@LoginUser Integer userId) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}
		LitemallUser user = userService.findById(userId);
		//String address = JacksonUtil.parseString(body, "address");
		Map<String,String> result = ETMHelp.getDappBalance(user.getAddress());
		if("true".equals(result.get("success"))) {
			
			return ResponseUtil.ok(result.get("balance"));
		}
		return ResponseUtil.fail(PAY_CODE_FAIL,result.get("error"));
	}
	
	/**
	 * 充历史记录
	 * @param userId
	 * @param body
	 * @return
	 */
	@GetMapping("balance/list")
	public Object BalanceList(@LoginUser Integer userId,
			@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}
		List<LitemallDraw> list = userService.queryDrawRecord(userId, page, limit);
		
		
			
		return ResponseUtil.okList(list);
		
	}
	
	

	/**
	 * 充历史记录
	 * @param userId
	 * @param body
	 * @return
	 */
	@GetMapping("recharge/list")
	public Object rechargeList(@LoginUser Integer userId) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}
		LitemallUser user = userService.findById(userId);
		Map<String,Object> reslut = ETMHelp.getBalanceList(user.getAddress());
		
			
		return ResponseUtil.ok(reslut);
		
	}
	

}