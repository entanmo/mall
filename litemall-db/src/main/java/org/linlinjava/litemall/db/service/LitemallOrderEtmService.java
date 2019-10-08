package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.EtmPayeeMapper;
import org.linlinjava.litemall.db.dao.LitemallEtmPayeeMapper;
import org.linlinjava.litemall.db.dao.LitemallOrderEtmMapper;
import org.linlinjava.litemall.db.dao.OrderEtmMapper;
import org.linlinjava.litemall.db.domain.LitemallEtmPayee;
import org.linlinjava.litemall.db.domain.LitemallOrderEtm;
import org.linlinjava.litemall.db.domain.LitemallOrderEtmExample;
import org.linlinjava.litemall.db.util.OrderEtmHandleOption;
import org.linlinjava.litemall.db.util.OrderEtmUtil;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class LitemallOrderEtmService {
    @Resource
    private LitemallOrderEtmMapper LitemallOrderEtmMapper;
    @Resource
    private OrderEtmMapper OrderEtmMapper;
    @Resource
    private EtmPayeeMapper etmPayeeMapper;
    @Resource
    private LitemallEtmPayeeMapper litemallEtmPayeeMapper;

    public int add(LitemallOrderEtm order) {
        order.setAddTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return LitemallOrderEtmMapper.insertSelective(order);
    }

    public int count(Integer userId) {
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.or().andUserIdEqualTo(userId).andDeletedEqualTo(false);
        return (int) LitemallOrderEtmMapper.countByExample(example);
    }

    public LitemallOrderEtm findById(Integer orderId) {
        return LitemallOrderEtmMapper.selectByPrimaryKey(orderId);
    }
    public LitemallEtmPayee genarate() {
        return etmPayeeMapper.randomPayee();
    }

    private String getRandomNum(Integer num) {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public int countByOrderSn(Integer userId, String orderSn) {
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.or().andUserIdEqualTo(userId).andOrderSnEqualTo(orderSn).andDeletedEqualTo(false);
        return (int) LitemallOrderEtmMapper.countByExample(example);
    }

    // TODO 这里应该产生一个唯一的订单，但是实际上这里仍然存在两个订单相同的可能性
    public String generateOrderSn(Integer userId) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
        String now = df.format(LocalDate.now());
        String orderSn = now + getRandomNum(6);
        while (countByOrderSn(userId, orderSn) != 0) {
            orderSn = now + getRandomNum(6);
        }
        return orderSn;
    }

    public List<LitemallOrderEtm> queryByOrderStatus( String orderSn,List<Short> orderStatus, Integer page, Integer limit, String sort, String order) {
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.setOrderByClause(LitemallOrderEtm.Column.addTime.desc());
        LitemallOrderEtmExample.Criteria criteria = example.or();

        if (!StringUtils.isEmpty(orderSn)) {
            criteria.andOrderSnEqualTo(orderSn);
        }
        if (orderStatus != null) {
            criteria.andOrderStatusIn(orderStatus);
        }
        criteria.andDeletedEqualTo(false);
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }
        PageHelper.startPage(page, limit);
        return LitemallOrderEtmMapper.selectByExample(example);
    }

    public List<LitemallOrderEtm> querySelective(Integer userId, String orderSn, List<Short> orderStatusArray, Integer page, Integer limit, String sort, String order) {
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        LitemallOrderEtmExample.Criteria criteria = example.createCriteria();

        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (!StringUtils.isEmpty(orderSn)) {
            criteria.andOrderSnEqualTo(orderSn);
        }
        if (orderStatusArray != null && orderStatusArray.size() != 0) {
            criteria.andOrderStatusIn(orderStatusArray);
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return LitemallOrderEtmMapper.selectByExample(example);
    }

    public int updateWithOptimisticLocker(LitemallOrderEtm order) {
        LocalDateTime preUpdateTime = order.getUpdateTime();
        order.setUpdateTime(LocalDateTime.now());
        return OrderEtmMapper.updateWithOptimisticLocker(preUpdateTime, order);
    }

    public void deleteById(Integer id) {
        LitemallOrderEtmMapper.logicalDeleteByPrimaryKey(id);
    }

    public int count() {
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.or().andDeletedEqualTo(false);
        return (int) LitemallOrderEtmMapper.countByExample(example);
    }

    public List<LitemallOrderEtm> queryUnpaid(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.minusMinutes(minutes);
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.or().andOrderStatusEqualTo(OrderEtmUtil.STATUS_CREATE).andAddTimeLessThan(expired).andDeletedEqualTo(false);
        return LitemallOrderEtmMapper.selectByExample(example);
    }

    public List<LitemallOrderEtm> queryUnconfirm(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.minusDays(days);
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.or().andOrderStatusEqualTo(OrderEtmUtil.STATUS_PAY).andDeletedEqualTo(false);
        return LitemallOrderEtmMapper.selectByExample(example);
    }

    public LitemallOrderEtm findBySn(String orderSn) {
        LitemallOrderEtmExample example = new LitemallOrderEtmExample();
        example.or().andOrderSnEqualTo(orderSn).andDeletedEqualTo(false);
        return LitemallOrderEtmMapper.selectOneByExample(example);
    }

    public Map<Object, Object> orderInfo(Integer orderId) {
        LitemallOrderEtm litemallOrderEtm =   LitemallOrderEtmMapper.selectByPrimaryKey(orderId);

        LitemallEtmPayee litemallEtmPayee = litemallEtmPayeeMapper.selectByPrimaryKey(Long.parseLong(litemallOrderEtm.getPayId()));

        Map<Object, Object> orderInfo = new HashMap<Object, Object>();
        orderInfo.put("id", litemallOrderEtm.getId());
        orderInfo.put("user_id", litemallOrderEtm.getUserId());
        orderInfo.put("order_sn", litemallOrderEtm.getOrderSn());
        orderInfo.put("pay_sn", litemallOrderEtm.getPaySn());
        orderInfo.put("size", litemallOrderEtm.getSize());
        orderInfo.put("price", litemallOrderEtm.getPrice());
        orderInfo.put("cost", litemallOrderEtm.getCost());
        orderInfo.put("currency", litemallOrderEtm.getCurrency());
        orderInfo.put("pay_id", litemallOrderEtm.getPayId());
        orderInfo.put("pay_time", litemallOrderEtm.getPayTime());
        orderInfo.put("verify_time", litemallOrderEtm.getVerifyTime());
        orderInfo.put("price_time", litemallOrderEtm.getPriceTime());
        orderInfo.put("order_status", litemallOrderEtm.getOrderStatus());
        orderInfo.put("add_time", litemallOrderEtm.getAddTime());
        orderInfo.put("update_time", litemallOrderEtm.getUpdateTime());
        orderInfo.put("type", litemallEtmPayee.getType());
        return orderInfo;

    }


}
