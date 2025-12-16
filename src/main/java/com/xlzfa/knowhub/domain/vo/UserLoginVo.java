package com.xlzfa.knowhub.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVo implements Serializable {

    private Long id;

    private String username;

    private String email;

    private String avatar;

    private String bio;

    private String token;

}
