package org.linlinjava.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.linlinjava.litemall.wx.service.WxOrderEtmService;
import org.linlinjava.litemall.wx.util.ETMHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/wx/orderetm")
@Validated
public class WxOrderEtmController {
    private final Log logger = LogFactory.getLog(WxOrderEtmController.class);

    @Autowired
    private WxOrderEtmService wxOrderEtmService;

    /**
     * 订单列表
     *
     * @param userId   用户ID
     * @param showType 订单信息
     * @param page     分页页数
     * @param limit     分页大小
     * @return 订单列表
     */
    @GetMapping("list")
    public Object list(@LoginUser Integer userId,
                       @RequestParam(defaultValue = "0") Integer showType,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        return wxOrderEtmService.list(userId, showType, page, limit, sort, order);
    }


    @GetMapping("etmPrice")
    public Object getEtmPrice(@LoginUser Integer userId) {
        return wxOrderEtmService.geEtmPrice(userId);
    }
    @GetMapping("etmBalance")
    public Object getEtmBalance(@LoginUser Integer userId, @NotNull String address) {
        return wxOrderEtmService.geEtmBalance(userId,address);
    }
    /**
     * 订单详情
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("detail")
    public Object detail(@LoginUser Integer userId, @NotNull Integer orderId) {
        return wxOrderEtmService.detail(userId, orderId);
    }

    /**
     * 提交订单
     *
     * @param userId 用户ID
     * @param body   订单信息，{ cartId：xxx, addressId: xxx, couponId: xxx, message: xxx, grouponRulesId: xxx,  grouponLinkId: xxx}
     * @return 提交订单操作结果
     */
    @PostMapping("submit")
    public Object submit(@LoginUser Integer userId, @RequestBody String body) {
        return wxOrderEtmService.submit(userId, body);
    }

    /**
     * 取消订单
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 取消订单操作结果
     */
    @PostMapping("cancel")
    public Object cancel(@LoginUser Integer userId, @RequestBody String body) {
        return wxOrderEtmService.cancel(userId, body);
    }

//    /**
//     * 付款订单的预支付会话标识
//     *
//     * @param userId 用户ID
//     * @param body   订单信息，{ orderId：xxx }
//     * @return 支付订单ID
//     */
//    @PostMapping("prepay")
//    public Object prepay(@LoginUser Integer userId, @RequestBody String body, HttpServletRequest request) {
//        return wxOrderEtmService.pay(userId, body, request);
//    }
    @PostMapping("pay")
    public Object prepay(@LoginUser Integer userId, @RequestBody String body) {
        return wxOrderEtmService.pay(userId, body);
    }
//    /**
//     * 确认收货
//     *
//     * @param userId 用户ID
//     * @param body   订单信息，{ orderId：xxx }
//     * @return 订单操作结果
//     */
//    @PostMapping("confirm")
//    public Object confirm(@LoginUser Integer userId, @RequestBody String body) {
//        return wxOrderEtmService.confirm(userId, body);
//    }

    /**
     * 删除订单
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    @PostMapping("delete")
    public Object delete(@LoginUser Integer userId, @RequestBody String body) {
        return wxOrderEtmService.delete(userId, body);
    }


}