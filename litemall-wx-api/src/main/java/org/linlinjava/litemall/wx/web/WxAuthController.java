package org.linlinjava.litemall.wx.web;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.notify.NotifyService;
import org.linlinjava.litemall.core.notify.NotifyType;
import org.linlinjava.litemall.core.util.*;
import org.linlinjava.litemall.core.util.bcrypt.BCryptPasswordEncoder;
import org.linlinjava.litemall.db.domain.LitemallUser;
import org.linlinjava.litemall.db.service.CouponAssignService;
import org.linlinjava.litemall.db.service.LitemallUserService;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.linlinjava.litemall.wx.dto.UserInfo;
import org.linlinjava.litemall.wx.dto.UserToken;
import org.linlinjava.litemall.wx.dto.WxLoginInfo;
import org.linlinjava.litemall.wx.service.CaptchaCodeManager;
import org.linlinjava.litemall.wx.service.UserTokenManager;
import org.linlinjava.litemall.wx.util.ETMHelp;
import org.linlinjava.litemall.wx.util.XianLiaoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.linlinjava.litemall.wx.util.WxResponseCode.*;

/**
 * 鉴权服务
 */
@RestController
@RequestMapping("/wx/auth")
@Validated
public class WxAuthController {
    private final Log logger = LogFactory.getLog(WxAuthController.class);

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private WxMaService wxService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private CouponAssignService couponAssignService;

    /**
     * 账号登录
     *
     * @param body    请求内容，{ username: xxx, password: xxx }
     * @param request 请求对象
     * @return 登录结果
     */
    @PostMapping("login")
    public Object login(@RequestBody String body, HttpServletRequest request) {
        String username = JacksonUtil.parseString(body, "username");
        String password = JacksonUtil.parseString(body, "password");
        if (username == null || password == null) {
            return ResponseUtil.badArgument();
        }

        List<LitemallUser> userList = userService.queryByUsername(username);
        LitemallUser user = null;
        if (userList.size() > 1) {
            return ResponseUtil.serious();
        } else if (userList.size() == 0) {
            return ResponseUtil.fail(AUTH_INVALID_ACCOUNT, "账号不存在");
        } else {
            user = userList.get(0);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return ResponseUtil.fail(AUTH_INVALID_ACCOUNT, "账号密码不对");
        }

        // 更新登录情况
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getIpAddr(request));
        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        // userInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatar());

        // token
        String token = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 微信登录
     *
     * @param wxLoginInfo 请求内容，{ code: xxx, userInfo: xxx }
     * @param request     请求对象
     * @return 登录结果
     */
    @PostMapping("login_by_weixin")
    public Object loginByWeixin(@RequestBody WxLoginInfo wxLoginInfo, HttpServletRequest request) {
        String code = wxLoginInfo.getCode();
        UserInfo userInfo = wxLoginInfo.getUserInfo();
        if (code == null || userInfo == null) {
            return ResponseUtil.badArgument();
        }

        String sessionKey = null;
        String openId = null;
        try {
            WxMaJscode2SessionResult result = this.wxService.getUserService().getSessionInfo(code);
            sessionKey = result.getSessionKey();
            openId = result.getOpenid();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sessionKey == null || openId == null) {
            return ResponseUtil.fail();
        }

        LitemallUser user = userService.queryByOid(openId);
        if (user == null) {
            user = new LitemallUser();
            user.setUsername(openId);
            user.setPassword(openId);
            user.setWeixinOpenid(openId);
            user.setAvatar(userInfo.getAvatarUrl());
            user.setNickname(userInfo.getNickName());
            user.setGender(userInfo.getGender());
            user.setUserLevel((byte) 0);
            user.setStatus((byte) 0);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtil.getIpAddr(request));
            user.setSessionKey(sessionKey);

            userService.add(user);

            // 新用户发送注册优惠券
            couponAssignService.assignForRegister(user.getId());
        } else {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtil.getIpAddr(request));
            user.setSessionKey(sessionKey);
            if (userService.updateById(user) == 0) {
                return ResponseUtil.updatedDataFailed();
            }
        }

        // token
        String token = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 闲聊授权页面
     */
    @RequestMapping("get_auth_page")
    public Object getAuthPage(HttpServletRequest request) {

        String html = XianLiaoRequest.getAuthPage();
        return ResponseUtil.ok(html);
    }

    /**
     * 闲聊授权回调
     */
    @RequestMapping("auth_by_xianliao")
    public void authByXianliao(@RequestParam String code, HttpServletResponse response) throws IOException {

        if (code == null) {
            response.sendRedirect("info");
        }

        response.sendRedirect("http://47.111.165.42/#/login/auth？code=" + code + "&type=3");
//        response.sendRedirect("http://192.168.2.72:6255/#/login/auth？code=" + code);
    }

    /**
     * 闲聊登录
     *
     * @param code    请求内容，{ code: xxx}
     * @param request 请求对象
     * @return 登录结果
     */
    @RequestMapping("login_by_xianliao")
    public Object loginByAuth(@RequestBody String code, HttpServletRequest request) {
        logger.info("xianliao code:" + code);

        if (code == null) {
            return ResponseUtil.badArgument();
        }

        String CODE = JacksonUtil.parseString(code, "code");
        UserInfo userInfo = XianLiaoRequest.getAuthUserInfo(CODE);
        if (userInfo == null) {
            return ResponseUtil.badArgument();
        }

        String openId = userInfo.getOpenId();
        LitemallUser user = userService.queryByOid(openId);
        if (user == null) {
            Map<String, String> etmAccount = ETMHelp.newAccount();
            logger.info("ETMHelp etmAccount:" + etmAccount);

            user = new LitemallUser();
            user.setUsername(openId);
            user.setPassword(openId);
            user.setWeixinOpenid(openId);
            user.setAvatar(userInfo.getAvatarUrl());
            user.setNickname(userInfo.getNickName());
            user.setGender(userInfo.getGender());

            user.setUserLevel((byte) 0);
            user.setStatus((byte) 0);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtil.getIpAddr(request));
            user.setSecret(etmAccount.get("secret"));
            user.setAddress(etmAccount.get("address"));

            logger.info("LitemallUser user:" + user);
            userService.add(user);

            // 新用户发送注册优惠券
            // couponAssignService.assignForRegister(user.getId());
        } else {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtil.getIpAddr(request));
            if (userService.updateById(user) == 0) {
                return ResponseUtil.updatedDataFailed();
            }
        }

        // token
        String token = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 微博授权回调
     */
    @RequestMapping("auth_by_weibo")
    public void authByWeibo(@RequestParam String code, HttpServletResponse response) throws IOException {
        if (code == null) {
            return;
        }
        response.sendRedirect("http://47.111.165.42/#/login/auth？code=" + code + "&type=1");
//        response.sendRedirect("http://192.168.2.72:9200/#/login/auth？code=" + code);
    }

    /**
     * 微博登录
     */
    @PostMapping("login_by_weibo")
    public Object loginByWeibo(@RequestBody String code, HttpServletRequest request) {
        logger.info("weibo code:" + code);
        if (code == null) {
            return ResponseUtil.badArgument();
        }
        String CODE = JacksonUtil.parseString(code, "code");
        String URL1 = "https://api.weibo.com/oauth2/access_token";
        String APPID = "3350462784";
        String APPSECRET = "8db0122cb12ea80c23f79e7e3c4bf5d0";
        String GRANT_TYPE = "authorization_code";
        Map<String, String> params1 = new HashMap<String, String>();
        params1.put("client_id", APPID);
        params1.put("client_secret", APPSECRET);
        params1.put("grant_type", GRANT_TYPE);
        params1.put("redirect_uri", "http://47.111.165.42:8080/user/auth/auth_by_weibo");
        params1.put("code", CODE);
        String access_token_json = HttpUtil.sendPost(URL1, params1);

        if (access_token_json.isEmpty()) {
            return ResponseUtil.fail(AUTH_INVALID_ACCOUNT, "授权失败");
        }
        Map<String, Object> access_map = JacksonUtil.toObjectMap(access_token_json);
        String ACCESS_TOKEN = (String) access_map.get("access_token");
        String UID = (String) access_map.get("uid");

        /*access_token换取用户信息*/
        String URL2 = "https://api.weibo.com/2/users/show.json";
        String params2 = "access_token=" + ACCESS_TOKEN + "&uid=" + UID;
        String user_json = HttpUtil.sendGet(URL2, params2);

        if (user_json.isEmpty()) {
            return ResponseUtil.fail(AUTH_INVALID_ACCOUNT, "授权失败");
        }
        Map<String, Object> user_map = JacksonUtil.toObjectMap(user_json);

        LitemallUser user = null;
        if (user_map != null) {
            String openId = (String) user_map.get("idstr");
            String nickName = (String) user_map.get("name");
            String originalAvatar = (String) user_map.get("profile_image_url");
            String user_gender = (String) user_map.get("gender");
            Byte gender = user_gender.equals("m") ? (byte) 1 : (byte) 0;

            user = userService.queryByOid(openId);
            if (user == null) {
                user = new LitemallUser();
                user.setUsername(openId);
                user.setPassword(openId);
                user.setWeixinOpenid(openId);
                user.setAvatar(originalAvatar);
                user.setNickname(nickName);
                user.setGender(gender);

                user.setUserLevel((byte) 0);
                user.setStatus((byte) 0);
                user.setLastLoginTime(LocalDateTime.now());
                user.setLastLoginIp(IpUtil.getIpAddr(request));

                userService.add(user);
                // 新用户发送注册优惠券
                couponAssignService.assignForRegister(user.getId());
            } else {
                user.setLastLoginTime(LocalDateTime.now());
                user.setLastLoginIp(IpUtil.getIpAddr(request));
                if (userService.updateById(user) == 0) {
                    return ResponseUtil.updatedDataFailed();
                }
            }
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setAvatarUrl(user.getAvatar());
        userInfo.setNickName(user.getNickname());
        userInfo.setGender(user.getGender());

        // token
        String token = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 请求注册验证码
     * <p>
     * TODO 这里需要一定机制防止短信验证码被滥用
     *
     * @param body 手机号码 { mobile }
     * @return
     */
    @PostMapping("regCaptcha")
    public Object registerCaptcha(@RequestBody String body) {
        String phoneNumber = JacksonUtil.parseString(body, "mobile");
        if (StringUtils.isEmpty(phoneNumber)) {
            return ResponseUtil.badArgument();
        }
        if (!RegexUtil.isMobileExact(phoneNumber)) {
            return ResponseUtil.badArgumentValue();
        }

        if (!notifyService.isSmsEnable()) {
            return ResponseUtil.fail(AUTH_CAPTCHA_UNSUPPORT, "小程序后台验证码服务不支持");
        }
        String code = CharUtil.getRandomNum(6);
        notifyService.notifySmsTemplate(phoneNumber, NotifyType.CAPTCHA, new String[]{code});

        boolean successful = CaptchaCodeManager.addToCache(phoneNumber, code);
        if (!successful) {
            return ResponseUtil.fail(AUTH_CAPTCHA_FREQUENCY, "验证码未超时1分钟，不能发送");
        }

        return ResponseUtil.ok();
    }

    /**
     * 账号注册
     *
     * @param body    请求内容 { username: xxx, password: xxx, mobile: xxx code: xxx }
     *                其中code是手机验证码，目前还不支持手机短信验证码
     * @param request 请求对象
     * @return 登录结果 成功则 { errno: 0, errmsg: '成功', data: { token: xxx, tokenExpire:
     * xxx, userInfo: xxx } } 失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("register")
    public Object register(@RequestBody String body, HttpServletRequest request) {
        String username = JacksonUtil.parseString(body, "username");
        String password = JacksonUtil.parseString(body, "password");
        String mobile = JacksonUtil.parseString(body, "mobile");
        String code = JacksonUtil.parseString(body, "code");
        String wxCode = JacksonUtil.parseString(body, "wxCode");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(wxCode) || StringUtils.isEmpty(code)) {
            return ResponseUtil.badArgument();
        }

        List<LitemallUser> userList = userService.queryByUsername(username);
        if (userList.size() > 0) {
            return ResponseUtil.fail(AUTH_NAME_REGISTERED, "用户名已注册");
        }

        userList = userService.queryByMobile(mobile);
        if (userList.size() > 0) {
            return ResponseUtil.fail(AUTH_MOBILE_REGISTERED, "手机号已注册");
        }
        if (!RegexUtil.isMobileExact(mobile)) {
            return ResponseUtil.fail(AUTH_INVALID_MOBILE, "手机号格式不正确");
        }
        // 判断验证码是否正确
        String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
        if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code)) {
            return ResponseUtil.fail(AUTH_CAPTCHA_UNMATCH, "验证码错误");
        }

        String openId = null;
        try {
            WxMaJscode2SessionResult result = this.wxService.getUserService().getSessionInfo(wxCode);
            openId = result.getOpenid();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail(AUTH_OPENID_UNACCESS, "openid 获取失败");
        }
        userList = userService.queryByOpenid(openId);
        if (userList.size() > 1) {
            return ResponseUtil.serious();
        }
        if (userList.size() == 1) {
            LitemallUser checkUser = userList.get(0);
            String checkUsername = checkUser.getUsername();
            String checkPassword = checkUser.getPassword();
            if (!checkUsername.equals(openId) || !checkPassword.equals(openId)) {
                return ResponseUtil.fail(AUTH_OPENID_BINDED, "openid已绑定账号");
            }
        }

        LitemallUser user = null;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        user = new LitemallUser();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setMobile(mobile);
        user.setWeixinOpenid(openId);
        user.setAvatar(
                "https://yanxuan.nosdn.127.net/80841d741d7fa3073e0ae27bf487339f.jpg?imageView&quality=90&thumbnail=64x64");
        user.setNickname(username);
        user.setGender((byte) 0);
        user.setUserLevel((byte) 0);
        user.setStatus((byte) 0);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getIpAddr(request));
        userService.add(user);

        // 给新用户发送注册优惠券
        couponAssignService.assignForRegister(user.getId());

        // userInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(username);
        userInfo.setAvatarUrl(user.getAvatar());

        // token
        String token = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 请求验证码
     * <p>
     * TODO 这里需要一定机制防止短信验证码被滥用
     *
     * @param body 手机号码 { mobile: xxx, type: xxx }
     * @return
     */
    @PostMapping("captcha")
    public Object captcha(@LoginUser Integer userId, @RequestBody String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        String phoneNumber = JacksonUtil.parseString(body, "mobile");
        String captchaType = JacksonUtil.parseString(body, "type");
        if (StringUtils.isEmpty(phoneNumber)) {
            return ResponseUtil.badArgument();
        }
        if (!RegexUtil.isMobileExact(phoneNumber)) {
            return ResponseUtil.badArgumentValue();
        }
        if (StringUtils.isEmpty(captchaType)) {
            return ResponseUtil.badArgument();
        }

        if (!notifyService.isSmsEnable()) {
            return ResponseUtil.fail(AUTH_CAPTCHA_UNSUPPORT, "小程序后台验证码服务不支持");
        }
        String code = CharUtil.getRandomNum(6);
        // TODO
        // 根据type发送不同的验证码
        notifyService.notifySmsTemplate(phoneNumber, NotifyType.CAPTCHA, new String[]{code});

        boolean successful = CaptchaCodeManager.addToCache(phoneNumber, code);
        if (!successful) {
            return ResponseUtil.fail(AUTH_CAPTCHA_FREQUENCY, "验证码未超时1分钟，不能发送");
        }

        return ResponseUtil.ok();
    }

    /**
     * 账号密码重置
     *
     * @param body    请求内容 { password: xxx, mobile: xxx code: xxx }
     *                其中code是手机验证码，目前还不支持手机短信验证码
     * @param request 请求对象
     * @return 登录结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("reset")
    public Object reset(@RequestBody String body, HttpServletRequest request) {
        String password = JacksonUtil.parseString(body, "password");
        String mobile = JacksonUtil.parseString(body, "mobile");
        String code = JacksonUtil.parseString(body, "code");

        if (mobile == null || code == null || password == null) {
            return ResponseUtil.badArgument();
        }

        // 判断验证码是否正确
        String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
        if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code))
            return ResponseUtil.fail(AUTH_CAPTCHA_UNMATCH, "验证码错误");

        List<LitemallUser> userList = userService.queryByMobile(mobile);
        LitemallUser user = null;
        if (userList.size() > 1) {
            return ResponseUtil.serious();
        } else if (userList.size() == 0) {
            return ResponseUtil.fail(AUTH_MOBILE_UNREGISTERED, "手机号未注册");
        } else {
            user = userList.get(0);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        user.setPassword(encodedPassword);

        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok();
    }

    /**
     * 账号手机号码重置
     *
     * @param body    请求内容 { password: xxx, mobile: xxx code: xxx }
     *                其中code是手机验证码，目前还不支持手机短信验证码
     * @param request 请求对象
     * @return 登录结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("resetPhone")
    public Object resetPhone(@LoginUser Integer userId, @RequestBody String body, HttpServletRequest request) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        String password = JacksonUtil.parseString(body, "password");
        String mobile = JacksonUtil.parseString(body, "mobile");
        String code = JacksonUtil.parseString(body, "code");

        if (mobile == null || code == null || password == null) {
            return ResponseUtil.badArgument();
        }

        // 判断验证码是否正确
        String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
        if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code))
            return ResponseUtil.fail(AUTH_CAPTCHA_UNMATCH, "验证码错误");

        List<LitemallUser> userList = userService.queryByMobile(mobile);
        LitemallUser user = null;
        if (userList.size() > 1) {
            return ResponseUtil.fail(AUTH_MOBILE_REGISTERED, "手机号已注册");
        }
        user = userService.findById(userId);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return ResponseUtil.fail(AUTH_INVALID_ACCOUNT, "账号密码不对");
        }

        user.setMobile(mobile);
        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok();
    }

    /**
     * 账号信息更新
     *
     * @param body    请求内容 { password: xxx, mobile: xxx code: xxx }
     *                其中code是手机验证码，目前还不支持手机短信验证码
     * @param request 请求对象
     * @return 登录结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
     */
    @PostMapping("profile")
    public Object profile(@LoginUser Integer userId, @RequestBody String body, HttpServletRequest request) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        String avatar = JacksonUtil.parseString(body, "avatar");
        Byte gender = JacksonUtil.parseByte(body, "gender");
        String nickname = JacksonUtil.parseString(body, "nickname");
        String payPassword = JacksonUtil.parseString(body, "payPassword");
        String email = JacksonUtil.parseString(body, "email");

        LitemallUser user = userService.findById(userId);
        if (!StringUtils.isEmpty(avatar)) {
            user.setAvatar(avatar);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (!StringUtils.isEmpty(nickname)) {
            user.setNickname(nickname);
        }
        if (!StringUtils.isEmpty(payPassword)) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(payPassword);
            user.setPayPassword(encodedPassword);
        }
        if (!StringUtils.isEmpty(email)) {
            user.setEmail(email);
        }

        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok();
    }

    /**
     * 微信手机号码绑定
     *
     * @param userId
     * @param body
     * @return
     */
    @PostMapping("bindPhone")
    public Object bindPhone(@LoginUser Integer userId, @RequestBody String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        LitemallUser user = userService.findById(userId);
        String encryptedData = JacksonUtil.parseString(body, "encryptedData");
        String iv = JacksonUtil.parseString(body, "iv");
        WxMaPhoneNumberInfo phoneNumberInfo = this.wxService.getUserService().getPhoneNoInfo(user.getSessionKey(),
                encryptedData, iv);
        String phone = phoneNumberInfo.getPhoneNumber();
        user.setMobile(phone);
        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        return ResponseUtil.ok();
    }

    @PostMapping("logout")
    public Object logout(@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        return ResponseUtil.ok();
    }

    @GetMapping("info")
    public Object info(@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        LitemallUser user = userService.findById(userId);
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("nickName", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("gender", user.getGender());
        data.put("mobile", user.getMobile());
        data.put("payPassword", user.getPayPassword());
        data.put("address", user.getAddress());
        return ResponseUtil.ok(data);
    }
}
