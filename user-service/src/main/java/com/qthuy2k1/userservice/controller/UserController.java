package com.qthuy2k1.userservice.controller;

import com.qthuy2k1.userservice.dto.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.UserRequest;
import com.qthuy2k1.userservice.dto.UserResponse;
import com.qthuy2k1.userservice.exception.UserAlreadyExistsException;
import com.qthuy2k1.userservice.exception.UserNotFoundException;
import com.qthuy2k1.userservice.service.JwtService;
import com.qthuy2k1.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<String> signup(@RequestBody @Valid UserRequest userRequest) throws UserAlreadyExistsException {
        userService.createUser(userRequest);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
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

    @GetMapping("/token")
    public String getToken(@RequestBody AuthenticationRequest authRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(), authRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getEmail());
        } else {
            throw new UsernameNotFoundException("invalid user request!");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        jwtService.validateToken(token);
        return "Token is valid";
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
    }
}
