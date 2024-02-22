package com.qthuy2k1.user.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class UserCreated implements Serializable {
    private String toEmail;
}
