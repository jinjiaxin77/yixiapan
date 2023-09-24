package com.jinjiaxin.yixiapan.mappers;

import com.jinjiaxin.yixiapan.entity.pojo.EmailCode;
import org.apache.ibatis.annotations.Param;

public interface EmailCodeMapper {

    void add(@Param("code")EmailCode code);

    EmailCode selectEmailCode(@Param("email") String email);

    void update(@Param("code") EmailCode code);

    void disableEmailCode(@Param("email") String email);
}
