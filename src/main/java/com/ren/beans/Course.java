package com.ren.beans;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class Course implements Serializable {

    @NotNull(message = "id不能为空")
    private Integer id;

    private String name;

    private Integer status;

    private Integer orgId;

    private String updateUser;


}
