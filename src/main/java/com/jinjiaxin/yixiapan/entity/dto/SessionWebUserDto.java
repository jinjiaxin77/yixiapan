package com.jinjiaxin.yixiapan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jjx
 * @Description
 * @create 2023/9/19 16:21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionWebUserDto {

    private String nickName;

    private String userId;

    private String avatar;

    private Boolean admin;

}
