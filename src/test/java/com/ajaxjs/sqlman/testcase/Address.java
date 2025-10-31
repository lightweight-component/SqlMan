package com.ajaxjs.sqlman.testcase;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.annotation.Transient;
import lombok.Data;

@Data
@Table("shop_address")
public class Address {
    private Integer id;

    private String name;

    private String address;

    private String phone;

    @Transient
    private String phone2;

    @Column(name = "receiver")
    private String re;
}