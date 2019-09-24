package org.linlinjava.litemall.wx.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.OrderEtmHandleOption;
import org.linlinjava.litemall.db.util.OrderEtmUtil;
import org.linlinjava.litemall.db.util.OrderHandleOption;
import org.linlinjava.litemall.db.util.OrderEtmUtil;
import org.linlinjava.litemall.wx.util.ETMHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.linlinjava.litemall.wx.util.WxResponseCode.*;

@Service
public class WxOrderEtmService {
    private final Log logger = LogFactory.getLog(WxOrderEtmService.class);
    
    @Autowired
    private LitemallOrderEtmService orderService;
    @Autowired
    private LitemallEtmPayeeService etmPayeeService;
    /**
     * 订单列表
     *
     * @param userId   用户ID
     * @param showType 订单信息：
     * 下单 已下单 101

     * 已付款  201
     * 取消订单 102
     * 系统取消 103
     *
     * 审核 已审核 301
     * 审核失败 202
     *
     * 已完成   401
     * @param page     分页页数
     * @param limit     分页大小
     * @return 订单列表
     */
    public Object list(Integer userId, Integer showType, Integer page, Integer limit, String sort, String order) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        List<Short> orderStatus = OrderEtmUtil.orderStatus(showType);
        List<LitemallOrderEtm> orderList = orderService.queryByOrderStatus(orderStatus, page, limit, sort, order);

        List<Map<String, Object>> orderVoList = new ArrayList<>(orderList.size());
        for (LitemallOrderEtm o : orderList) {
            Map<String, Object> orderVo = new HashMap<>();
            orderVo.put("id", o.getId());
            orderVo.put("orderSn", o.getOrderSn());
            orderVo.put("cost",o.getCost());
            orderVo.put("orderStatus",o.getOrderStatus());
            orderVo.put("price",o.getPrice());
            orderVo.put("size",o.getSize());
            orderVo.put("addTime",o.getAddTime());
            orderVo.put("payId",o.getPayId()==null?"":o.getPayId());
            orderVo.put("payTime",o.getPayTime()==null?"":o.getPayTime());
            orderVo.put("verifyTime",o.getVerifyTime()==null?"":o.getVerifyTime());
            orderVoList.add(orderVo);
        }

        return ResponseUtil.okList(orderVoList, orderList);
    }

    /**
     * 订单详情
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    public Object detail(Integer userId, Integer orderId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        // 订单信息
        LitemallOrderEtm order = orderService.findById(orderId);
        if (null == order) {
            return ResponseUtil.fail(ORDER_UNKNOWN, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.fail(ORDER_INVALID, "不是当前用户的订单");
        }

//        Map<String, Object> orderVo = new HashMap<String, Object>();
//        orderVo.put("id", order.getId());
//        orderVo.put("orderSn", order.getOrderSn());
//        orderVo.put("addTime", order.getAddTime());
//        orderVo.put("consignee", order.getConsignee());
//        orderVo.put("mobile", order.getMobile());
//        orderVo.put("address", order.getAddress());
//        orderVo.put("goodsPrice", order.getGoodsPrice());
//        orderVo.put("couponPrice", order.getCouponPrice());
//        orderVo.put("freightPrice", order.getFreightPrice());
//        orderVo.put("actualPrice", order.getActualPrice());
//        orderVo.put("orderStatusText", OrderEtmUtil.orderStatusText(order));
//        orderVo.put("handleOption", OrderEtmUtil.build(order));
//        orderVo.put("expCode", order.getShipChannel());
//        orderVo.put("expNo", order.getShipSn());

        LitemallEtmPayee litemallEtmPayee = etmPayeeService.findById(Long.parseLong(order.getPayId()));

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("payee", litemallEtmPayee);
//        result.put("orderGoods", orderGoodsList);

        // 订单状态为已发货且物流信息不为空
        //"YTO", "800669400640887922"
//        if (order.getOrderStatus().equals(OrderEtmUtil.STATUS_SHIP)) {
//            ExpressInfo ei = expressService.getExpressInfo(order.getShipChannel(), order.getShipSn());
//            result.put("expressInfo", ei);
//        }

        return ResponseUtil.ok(result);

    }
    public Object toOrder(Integer userId,String address){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
//        LitemallEtmPayee payee = orderService.genarate();
        Object etm_price = ETMHelp.getEtmTickOkex();
        Map balance = ETMHelp.getDappBalance(address);
//        Map<String, Object> orderVo = new HashMap<String, Object>();
//        orderVo.put("etm_price",etm_price);

        Map<String, Object> result = new HashMap<>();
        result.put("etm_price", etm_price);
        result.put("etm_balance", balance);

        return ResponseUtil.ok(result);
    }
    public Object geEtmPrice(Integer userId){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        try{
            Object etm_price = ETMHelp.getEtmTickOkex();
            Map<String, Object> result = new HashMap<>();
            result.put("etm_tick", etm_price);
            return ResponseUtil.ok(result);
        }catch (Exception e){
            return  ResponseUtil.fail(ETM_PRICE_ERROR,e.getMessage());
        }



    }
    public Object geEtmBalance(Integer userId,String address){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Object balance = ETMHelp.getDappBalance(address);
        Map<String, Object> result = new HashMap<>();
        result.put("etm_balance", balance);

        return ResponseUtil.ok(result);
    }
    /**
     * 提交订单
     * <p>
     * 1. 创建订单表项和订单商品表项;
     * 2. 购物车清空;
     * 3. 优惠券设置已用;
     * 4. 商品货品库存减少;
     * 5. 如果是团购商品，则创建团购活动表项。
     *
     * @param userId 用户ID
     * @param body   订单信息，{ cartId：xxx, addressId: xxx, couponId: xxx, message: xxx, grouponRulesId: xxx,  grouponLinkId: xxx}
     * @return 提交订单操作结果
     */
    @Transactional
    public Object submit(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        if (body == null) {
            return ResponseUtil.badArgument();
        }
        Double cost = JacksonUtil.parseDouble(body, "cost");
        Double price = JacksonUtil.parseDouble(body, "price");
        Double size = JacksonUtil.parseDouble(body, "size");
        Integer type = JacksonUtil.parseInteger(body, "type");
        String currency = JacksonUtil.parseString(body, "currency");

        Integer orderId = null;
        LitemallOrderEtm order = null;
        // 订单
        order = new LitemallOrderEtm();
        order.setUserId(userId);
        order.setOrderSn(orderService.generateOrderSn(userId));
        order.setOrderStatus(OrderEtmUtil.STATUS_CREATE);
        order.setCost(BigDecimal.valueOf(cost));
        order.setPrice(BigDecimal.valueOf(price));
        order.setSize(BigDecimal.valueOf(size));
        order.setPayId(etmPayeeService.randomPayeeByType(type).getId()+"");
        order.setCurrency(currency);
        // 添加订单表项
        orderService.add(order);
        orderId = order.getId();
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        return ResponseUtil.ok(data);
    }

    /**
     * 取消订单
     * <p>
     * 1. 检测当前订单是否能够取消；
     * 2. 设置订单取消状态；
     * 3. 商品货品库存恢复；
     * 4. TODO 优惠券；
     * 5. TODO 团购活动。
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 取消订单操作结果
     */
    @Transactional
    public Object cancel(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrderEtm order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgumentValue();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }
        // 检测是否能够取消
        OrderEtmHandleOption handleOption = OrderEtmUtil.build(order);
        if (!handleOption.isCancel()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能取消");
        }
        // 设置订单已取消状态
        order.setOrderStatus(OrderEtmUtil.STATUS_CANCEL);
        //order.setEndTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            throw new RuntimeException("更新数据已失效");
        }


        return ResponseUtil.ok();
    }

    /**
     * 确认收货
     * <p>
     * 1. 检测当前订单是否能够确认收货；
     * 2. 设置订单确认收货状态。
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    public Object confirm(Integer userId, String body) {

        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrderEtm order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        OrderEtmHandleOption handleOption = OrderEtmUtil.build(order);
        if (!handleOption.isConfirm()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能确认付款");
        }

        order.setOrderStatus(OrderEtmUtil.STATUS_CONFIRM);
        order.setVerifyTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }
        return ResponseUtil.ok();
    }
    public Object verify(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrderEtm order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        OrderEtmHandleOption handleOption = OrderEtmUtil.build(order);
        if (!handleOption.isVerify()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能确认付款");
        }

//        Short comments = orderGoodsService.getComments(orderId);
//        order.setComments(comments);

        order.setOrderStatus(OrderEtmUtil.STATUS_VERIFY);
        order.setVerifyTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }
        return ResponseUtil.ok();
    }
    public Object reject(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrderEtm order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        OrderEtmHandleOption handleOption = OrderEtmUtil.build(order);
        if (!handleOption.isReject()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能确认付款");
        }

//        Short comments = orderGoodsService.getComments(orderId);
//        order.setComments(comments);

        order.setOrderStatus(OrderEtmUtil.STATUS_REJECT);
//        order.setConfirmTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }
        return ResponseUtil.ok();
    }
    public Object pay(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        String paySn = JacksonUtil.parseString(body, "paySn");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrderEtm order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        OrderEtmHandleOption handleOption = OrderEtmUtil.build(order);
        if (!handleOption.isPay()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能确认付款");
        }

//        Short comments = orderGoodsService.getComments(orderId);
//        order.setComments(comments);

        order.setOrderStatus(OrderEtmUtil.STATUS_PAY);
        order.setPaySn(paySn);
        order.setPayTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }
        return ResponseUtil.ok();
    }
    /**
     * 删除订单
     * <p>
     * 1. 检测当前订单是否可以删除；
     * 2. 删除订单。
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 订单操作结果
     */
    public Object delete(Integer userId, String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrderEtm order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

//        OrderHandleOption handleOption = OrderEtmUtil.build(order);
//        if (!handleOption.isDelete()) {
//            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能删除");
//        }

        // 订单order_status没有字段用于标识删除
        // 而是存在专门的delete字段表示是否删除
        orderService.deleteById(orderId);

        return ResponseUtil.ok();
    }


}