package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.service.AdminOrderService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.service.LitemallOrderEtmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/admin/orderetm")
@Validated
public class AdminOrderEtmController {
    private final Log logger = LogFactory.getLog(AdminOrderEtmController.class);

    @Autowired
    private LitemallOrderEtmService orderEtmService;
    @Autowired
    private AdminOrderService adminOrderService;
    /**
     * 查询订单
     *
     * @param orderStatusArray
     * @param page
     * @param limit
     * @param sort
     * @param order
     * @return
     */
    @RequiresPermissions("admin:orderetm:list")
    @RequiresPermissionsDesc(menu = {"法币交易", "订单管理"}, button = "查询")
    @GetMapping("/list")
    public Object list(
                         String orderSn,
                       @RequestParam(required = false) List<Short> orderStatusArray,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        return ResponseUtil.okList(orderEtmService.queryByOrderStatus( orderSn, orderStatusArray, page, limit, sort, order));
    }

    /**
     * 订单详情
     *
     * @param id
     * @return
     */
    @RequiresPermissions("admin:orderetm:read")
    @RequiresPermissionsDesc(menu = {"法币交易", "订单管理"}, button = "详情")
    @GetMapping("/detail")
    public Object detail(@NotNull Integer id) {
        return ResponseUtil.ok(orderEtmService.orderInfo(id));
    }

    /**
     * 订单退款
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单退款操作结果
     */
//    @RequiresPermissions("admin:order:refund")
//    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "订单退款")
//    @PostMapping("/refund")
//    public Object refund(@RequestBody String body) {
//        return orderEtmService.deleteById(body);
//    }
    @RequiresPermissions("admin:orderetm:verify")
    @RequiresPermissionsDesc(menu = {"法币交易", "订单管理"}, button = "审核")
    @PostMapping("/verify")
    public Object confirm(@RequestBody String body) {
        return adminOrderService.verify(body);
    }
    @RequiresPermissions("admin:orderetm:reject")
    @RequiresPermissionsDesc(menu = {"法币交易", "订单管理"}, button = "打回")
    @PostMapping("/reject")
    public Object reject(@RequestBody String body) {
        return adminOrderService.reject(body);
    }
}
