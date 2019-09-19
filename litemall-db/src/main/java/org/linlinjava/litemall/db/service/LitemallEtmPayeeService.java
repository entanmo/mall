package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.EtmPayeeMapper;
import org.linlinjava.litemall.db.dao.LitemallEtmPayeeMapper;
import org.linlinjava.litemall.db.domain.LitemallEtmPayee;
import org.linlinjava.litemall.db.domain.LitemallEtmPayee.Column;
import org.linlinjava.litemall.db.domain.LitemallEtmPayeeExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallEtmPayeeService {
    private final Column[] result = new Column[]{Column.id, Column.username, Column.size, Column.account,
            Column.alipaypic,Column.wepaypic,Column.addTime};//,Column.type
    @Resource
    private LitemallEtmPayeeMapper adminMapper;
    @Resource
    EtmPayeeMapper etmPayeeMapper;

    public List<LitemallEtmPayee> findAdmin(String username) {
        LitemallEtmPayeeExample example = new LitemallEtmPayeeExample();
        example.or().andUsernameEqualTo(username).andDeletedEqualTo(false);
        return adminMapper.selectByExample(example);
    }

    public LitemallEtmPayee findAdmin(Long id) {
        return adminMapper.selectByPrimaryKey(id);
    }



    public LitemallEtmPayee findRandomPayee() {
        return etmPayeeMapper.randomPayee();
    }
    public LitemallEtmPayee randomPayeeByType(Integer type) {
        return etmPayeeMapper.randomPayeeByType(type);
    }
    public List<LitemallEtmPayee> querySelective(String username, Integer page, Integer limit, String sort, String order) {
        LitemallEtmPayeeExample example = new LitemallEtmPayeeExample();
        LitemallEtmPayeeExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(username)) {
            criteria.andUsernameLike("%" + username + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return adminMapper.selectByExampleSelective(example, result);
    }

    public int updateById(LitemallEtmPayee admin) {
        admin.setUpdateTime(LocalDateTime.now());
        return adminMapper.updateByPrimaryKeySelective(admin);
    }

    public void deleteById(Long id) {
        adminMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(LitemallEtmPayee admin) {
        admin.setAddTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        adminMapper.insertSelective(admin);
    }

    public LitemallEtmPayee findById(Long id) {
        return adminMapper.selectByPrimaryKeySelective(id, result);
    }

    public List<LitemallEtmPayee> all() {
        LitemallEtmPayeeExample example = new LitemallEtmPayeeExample();
        example.or().andDeletedEqualTo(false);
        return adminMapper.selectByExample(example);
    }
}
