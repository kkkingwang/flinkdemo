package com.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
@Data
//注在类上，提供类的全参构造
@AllArgsConstructor
//注在类上，提供类的无参构造
@NoArgsConstructor
public class User {
    private String id;
    private String userName;
    private String passWord;
    private String trueName;
    private String idCardNo;
    private String isValid;
    private String updateTime;
    private String createTime;
}
