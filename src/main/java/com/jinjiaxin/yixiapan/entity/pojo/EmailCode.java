package com.jinjiaxin.yixiapan.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author jjx
 * @Description
 * @create 2023/9/16 15:02
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCode {

    private String email;

    private String code;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer status;

}
