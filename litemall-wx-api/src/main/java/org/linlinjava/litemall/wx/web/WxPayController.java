package org.linlinjava.litemall.wx.web;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallAddress;
import org.linlinjava.litemall.db.service.LitemallAddressService;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	private LitemallAddressService addressService;



	/**
	 * Etm 链支付
	 *
	 * @param userId 用户ID
	 * @return 收货地址列表
	 */
	@GetMapping("etm")
	public Object etm(@LoginUser Integer userId) {
		if (userId == null) {
			return ResponseUtil.unlogin();
		}
		List<LitemallAddress> addressList = addressService.queryByUid(userId);
		return ResponseUtil.okList(addressList);
	}

	


	
}