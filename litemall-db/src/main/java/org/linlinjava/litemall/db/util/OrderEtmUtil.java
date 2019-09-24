package org.linlinjava.litemall.db.util;

import org.linlinjava.litemall.db.domain.LitemallOrderEtm;

import java.util.ArrayList;
import java.util.List;

/*
 * 订单流程：下单成功－》支付订单－》发货－》收货
 * 订单状态：
 * 下单 已下单 101
取消订单 102
系统取消 103

已付款  201

审核 已审核 301
审核失败 302

已完成   401

 */
public class OrderEtmUtil {

    public static final Short STATUS_CREATE = 101;
    public static final Short STATUS_CANCEL = 102;
    public static final Short STATUS_AUTO_CANCEL = 103;

    public static final Short STATUS_PAY = 201;
    public static final Short STATUS_VERIFY = 301;
    public static final Short STATUS_REJECT = 302;

    public static final Short STATUS_CONFIRM = 401;

    public static String orderStatusText(LitemallOrderEtm order) {
        int status = order.getOrderStatus().intValue();

        if (status == 101) {
            return "未付款";
        }

        if (status == 102) {
            return "已取消";
        }

        if (status == 103) {
            return "已取消(系统)";
        }

        if (status == 201) {
            return "待审核";
        }

        if (status == 301) {
            return "已审核";
        }

        if (status == 302) {
            return "审核未通过";
        }

        if (status == 401) {
            return "完成";
        }

        throw new IllegalStateException("orderEtmStatus不支持");
    }


    public static OrderEtmHandleOption build(LitemallOrderEtm order) {
        int status = order.getOrderStatus().intValue();
        OrderEtmHandleOption handleOption = new OrderEtmHandleOption();

        if (status == 101) {
            // 如果订单没有被取消，且没有支付，则可支付，可取消
            handleOption.setCancel(true);
            handleOption.setPay(true);
        } else if (status == 102 || status == 103) {
            // 如果订单已经取消或是已完成，则可删除
            handleOption.setDelete(true);
        } else if (status == 201) {
            // 如果订单已付款，没有发货，则可退款
            handleOption.setVerify(true);
            handleOption.setReject(true);
        } else if (status == 302) {
            // 如果订单已经退款，则可删除
            handleOption.setDelete(true);
        } else if (status == 301) {
            // 如果订单已经发货，没有收货，则可收货操作,
            // 此时不能取消订单
            handleOption.setConfirm(true);
        } else if (status == 401  ) {
            // 如果订单已经支付，且已经收货，则可删除、去评论和再次购买
            handleOption.setDelete(true);

        } else {
            throw new IllegalStateException("status不支持");
        }

        return handleOption;
    }

    public static List<Short> orderStatus(Integer showType) {
        // 全部订单
        if (showType == 0) {
            return null;
        }

        List<Short> status = new ArrayList<Short>(2);

        if (showType.equals(1)) {
            // 待付款订单
            status.add((short) 101);
        } else if (showType.equals(2)) {
            // 待发货订单
            status.add((short) 201);
        } else if (showType.equals(3)) {
            // 待收货订单
            status.add((short) 301);
        } else if (showType.equals(4)) {
            // 待评价订单
            status.add((short) 401);
//            系统超时自动取消，此时应该不支持评价
//            status.add((short)402);
        } else {
            return null;
        }

        return status;
    }


    public static boolean isCreateStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_CREATE == litemallOrder.getOrderStatus().shortValue();
    }

    public static boolean isPayStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_PAY == litemallOrder.getOrderStatus().shortValue();
    }

//    public static boolean isShipStatus(LitemallOrderEtm litemallOrder) {
//        return OrderEtmUtil.STATUS_SHIP == litemallOrder.getOrderStatus().shortValue();
//    }

    public static boolean isConfirmStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_CONFIRM == litemallOrder.getOrderStatus().shortValue();
    }

    public static boolean isCancelStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_CANCEL == litemallOrder.getOrderStatus().shortValue();
    }

    public static boolean isAutoCancelStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_AUTO_CANCEL == litemallOrder.getOrderStatus().shortValue();
    }

    public static boolean isVerifyStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_VERIFY == litemallOrder.getOrderStatus().shortValue();
    }

    public static boolean isRejectStatus(LitemallOrderEtm litemallOrder) {
        return OrderEtmUtil.STATUS_REJECT == litemallOrder.getOrderStatus().shortValue();
    }

//    public static boolean isAutoConfirmStatus(LitemallOrderEtm litemallOrder) {
//        return OrderEtmUtil.STATUS_AUTO_CONFIRM == litemallOrder.getOrderStatus().shortValue();
//    }
}
