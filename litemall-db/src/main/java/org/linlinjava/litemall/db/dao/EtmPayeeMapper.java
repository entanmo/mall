package org.linlinjava.litemall.db.dao;

import org.apache.ibatis.annotations.Param;
import org.linlinjava.litemall.db.domain.LitemallEtmPayee;

public interface EtmPayeeMapper {
    LitemallEtmPayee randomPayee();
    LitemallEtmPayee randomPayeeByType(@Param("type") Integer type);
}