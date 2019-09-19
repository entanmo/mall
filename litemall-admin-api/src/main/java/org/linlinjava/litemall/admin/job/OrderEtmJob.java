package org.linlinjava.litemall.admin.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.db.domain.LitemallOrderEtm;
import org.linlinjava.litemall.db.service.LitemallOrderEtmService;
import org.linlinjava.litemall.db.util.OrderEtmUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检测订单状态
 */
@Component
public class OrderEtmJob {
    private final Log logger = LogFactory.getLog(OrderEtmJob.class);

    @Autowired
    private LitemallOrderEtmService orderEtmService;

    /**
     * 自动取消订单
     * <p>
     * 定时检查订单未付款情况，如果超时 LITEMALL_ORDER_UNPAID 分钟则自动取消订单
     * 定时时间是每次相隔半个小时。
     * <p>
     * TODO
     * 注意，因为是相隔半小时检查，因此导致订单真正超时时间是 [LITEMALL_ORDER_UNPAID, 30 + LITEMALL_ORDER_UNPAID]
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderUnpaid() {
        logger.info("系统开启任务检查ETM订单是否已经超期自动取消订单");

        List<LitemallOrderEtm> orderList = orderEtmService.queryUnpaid(SystemConfig.getOrderEtmUnpaid());
        for (LitemallOrderEtm order : orderList) {
            // 设置订单已取消状态
            order.setOrderStatus(OrderEtmUtil.STATUS_AUTO_CANCEL);
            order.setEndTime(LocalDateTime.now());
            if (orderEtmService.updateWithOptimisticLocker(order) == 0) {
                throw new RuntimeException("更新数据已失效");
            }

//            // 商品货品数量增加
//            Integer orderId = order.getId();
//            List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
//            for (LitemallOrderGoods orderGoods : orderGoodsList) {
//                Integer productId = orderGoods.getProductId();
//                Short number = orderGoods.getNumber();
//                if (productService.addStock(productId, number) == 0) {
//                    throw new RuntimeException("商品货品库存增加失败");
//                }
//            }
            logger.info("ETM订单 ID" + order.getId() + " 已经超期自动取消订单");
        }
    }


}
