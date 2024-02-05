package com.qthuy2k1.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldSignup() throws Exception {
        UserRequest userRequest = getUserRequest();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("success")));
    }

    private UserRequest getUserRequest() {
        return UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
    }
}