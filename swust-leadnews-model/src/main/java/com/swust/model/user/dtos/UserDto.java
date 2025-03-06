package com.swust.model.user.dtos;

import com.swust.model.common.dtos.PageRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDto extends PageRequestDto {
    private String name;
    private Integer sex;
    private String phone;
}
