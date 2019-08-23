package org.linlinjava.litemall.db.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.linlinjava.litemall.db.domain.LitemallDraw;
import org.linlinjava.litemall.db.domain.LitemallDrawExample;

public interface LitemallDrawMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    long countByExample(LitemallDrawExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int deleteByExample(LitemallDrawExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int insert(LitemallDraw record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int insertSelective(LitemallDraw record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    LitemallDraw selectOneByExample(LitemallDrawExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    LitemallDraw selectOneByExampleSelective(@Param("example") LitemallDrawExample example, @Param("selective") LitemallDraw.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    List<LitemallDraw> selectByExampleSelective(@Param("example") LitemallDrawExample example, @Param("selective") LitemallDraw.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    List<LitemallDraw> selectByExample(LitemallDrawExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    LitemallDraw selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") LitemallDraw.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    LitemallDraw selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") LitemallDraw record, @Param("example") LitemallDrawExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") LitemallDraw record, @Param("example") LitemallDrawExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(LitemallDraw record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_draw
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(LitemallDraw record);
}