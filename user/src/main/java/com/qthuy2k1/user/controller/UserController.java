package com.qthuy2k1.user.controller;

import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.dto.UserResponse;
import com.qthuy2k1.user.exception.UserAlreadyExistsException;
import com.qthuy2k1.user.exception.UserNotFoundException;
import com.qthuy2k1.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteUser(@PathVariable("id") String id) throws UserNotFoundException, NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        userService.deleteUserById(parsedId);
        return "success";
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateUser(@PathVariable("id") String id, @RequestBody @Valid UserRequest userRequest)
            throws UserNotFoundException, UserAlreadyExistsException, NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        userService.updateUserById(parsedId, userRequest);
        return "success";
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUser(@PathVariable("id") String id) throws UserNotFoundException, NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        return userService.getUserById(parsedId);
    }

    @GetMapping("{id}/is-exists")
    @ResponseStatus(HttpStatus.OK)
    public Boolean existsById(@PathVariable("id") String id) throws NumberFormatException {
        Integer parsedId = Integer.valueOf(id);
        return userService.existsById(parsedId);
    }
}
