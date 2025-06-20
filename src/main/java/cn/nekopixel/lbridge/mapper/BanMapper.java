package cn.nekopixel.lbridge.mapper;

import cn.nekopixel.lbridge.entity.BanRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BanMapper {
    @Select("SELECT uuid FROM litebans_history WHERE name = #{name} ORDER BY date DESC LIMIT 1")
    String findLatestUuidByName(@Param("name") String name);

    @Select("SELECT * FROM litebans_bans " +
            "WHERE uuid = #{uuid} " +
            "AND active = 1 " +
            "AND (until > #{currentTime} OR until = 0) " +
            "AND server_scope = '*' " +
            "LIMIT 1")
    BanRecord findActiveBan(@Param("uuid") String uuid, @Param("currentTime") long currentTime);
}