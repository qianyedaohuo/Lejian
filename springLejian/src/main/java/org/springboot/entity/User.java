package org.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.extern.log4j.Log4j;

@Log4j
@TableName("whiteList")
@Data
public class User {

    //自增
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;
    private String realname;

}
