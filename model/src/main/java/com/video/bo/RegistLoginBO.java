package com.video.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistLoginBO {
    
    @NotBlank(message = "phone number can not be empty")
    @Length(min = 11, max = 11, message = "phone number length not valided")
    private String mobile;

    @NotBlank(message = "verifyCode can not be empty")
    private String smsCode;
}
