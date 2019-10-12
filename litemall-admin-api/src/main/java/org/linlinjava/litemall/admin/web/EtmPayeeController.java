package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.service.LogHelper;
import org.linlinjava.litemall.core.util.RegexUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallEtmPayee;
import org.linlinjava.litemall.db.service.LitemallEtmPayeeService;
import org.linlinjava.litemall.db.service.LitemallOrderEtmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.*;

@RestController
@RequestMapping("/admin/payee")
@Validated
public class EtmPayeeController {
    private final Log logger = LogFactory.getLog(EtmPayeeController.class);

    @Autowired
    private LitemallEtmPayeeService etmPayeeService;

    @Autowired
    private LogHelper logHelper;

    @RequiresPermissions("admin:payee:list")
    @RequiresPermissionsDesc(menu = {"法币交易", "支付账号"}, button = "查询")
    @GetMapping("/list")
    public Object list(String username,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallEtmPayee> adminList = etmPayeeService.querySelective(username, page, limit, sort, order);
        return ResponseUtil.okList(adminList);
    }

    private Object validate(LitemallEtmPayee admin) {
        String name = admin.getUsername();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgument();
        }
        if (!RegexUtil.isUsername(name)) {
            return ResponseUtil.fail(ADMIN_INVALID_NAME, "管理员名称不符合规定");
        }
//        String password = admin.getPassword();
//        if (StringUtils.isEmpty(password) || password.length() < 6) {
//            return ResponseUtil.fail(ADMIN_INVALID_PASSWORD, "管理员密码长度不能小于6");
//        }
        return null;
    }

    @RequiresPermissions("admin:payee:create")
    @RequiresPermissionsDesc(menu = {"法币交易", "支付账号"}, button = "添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallEtmPayee admin) {
        Object error = validate(admin);
        if (error != null) {
            return error;
        }

        String username = admin.getUsername();
        List<LitemallEtmPayee> adminList = etmPayeeService.findAdmin(username);
        if (adminList.size() > 0) {
            return ResponseUtil.fail(ADMIN_NAME_EXIST, "管理员已经存在");
        }

//        String rawPassword = admin.getPassword();
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String encodedPassword = encoder.encode(rawPassword);
//        admin.setPassword(encodedPassword);
        etmPayeeService.add(admin);
        logHelper.logAuthSucceed("添加管理员", username);
        return ResponseUtil.ok(admin);
    }

    @RequiresPermissions("admin:payee:read")
    @RequiresPermissionsDesc(menu = {"法币交易", "支付账号"}, button = "详情")
    @GetMapping("/read")
    public Object read(@NotNull Long id) {
        LitemallEtmPayee admin = etmPayeeService.findById(id);
        return ResponseUtil.ok(admin);
    }

    @RequiresPermissions("admin:payee:update")
    @RequiresPermissionsDesc(menu = {"法币交易", "支付账号"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallEtmPayee admin) {
        Object error = validate(admin);
        if (error != null) {
            return error;
        }

        Long anotherAdminId = admin.getId();
        if (anotherAdminId == null) {
            return ResponseUtil.badArgument();
        }

        // 不允许管理员通过编辑接口修改密码
//        admin.setPassword(null);

        if (etmPayeeService.updateById(admin) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        logHelper.logAuthSucceed("编辑管理员", admin.getUsername());
        return ResponseUtil.ok(admin);
    }

    @RequiresPermissions("admin:payee:delete")
    @RequiresPermissionsDesc(menu = {"法币交易", "支付账号"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallEtmPayee admin) {
          Long anotherAdminId = admin.getId();
        if (anotherAdminId == null) {
            return ResponseUtil.badArgument();
        }

        // 管理员不能删除自身账号
//        Subject currentUser = SecurityUtils.getSubject();
//        LitemallEtmPayee currentAdmin = (LitemallEtmPayee) currentUser.getPrincipal();
//        if (currentAdmin.getId().equals(anotherAdminId)) {
//            return ResponseUtil.fail(ADMIN_DELETE_NOT_ALLOWED, "管理员不能删除自己账号");
//        }

        etmPayeeService.deleteById(anotherAdminId);
        logHelper.logAuthSucceed("删除管理员", admin.getUsername());
        return ResponseUtil.ok();
    }
}
