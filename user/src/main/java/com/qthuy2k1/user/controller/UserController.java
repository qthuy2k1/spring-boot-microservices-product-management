package com.qthuy2k1.user.controller;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String signup(@RequestBody @Valid UserRequest userRequest) throws UserAlreadyExistsException {
        userService.createUser(userRequest);
        return "success";
    }

}
