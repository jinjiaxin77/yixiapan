package com.jinjiaxin.yixiapan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QQInfoDto {

    private Integer ret;

    private String msg;

    private String nickName;

    private String figureUrl_qq_1;

    private String figureUrl_qq_2;

    private String gender;

}
