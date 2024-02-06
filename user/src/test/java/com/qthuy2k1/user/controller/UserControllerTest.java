package com.qthuy2k1.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.user.dto.UserRequest;
import com.qthuy2k1.user.dto.UserResponse;
import com.qthuy2k1.user.model.Role;
import com.qthuy2k1.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @InjectMocks
    private UserController userController;


    @Test
    void shouldSignup() throws Exception {
        // given
        UserRequest userRequest = getUserRequest();
        String userRequestString = objectMapper.writeValueAsString(userRequest);

        // when
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isCreated())
                .andExpect(content().string("success"))
                .andDo(print());

        // then
        verify(userService).createUser(any());
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        // given
        List<UserResponse> users = new ArrayList<>();
        users.add(
                new UserResponse(1, "John Doe", "johndoe@gmail.com", Role.USER)
        );
        users.add(
                new UserResponse(2, "Jane Doe", "janedoe@gmail.com", Role.USER)
        );
        given(userService.getAllUsers()).willReturn(users);

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(userService).getAllUsers();

        assertThat(result.getResponse().getContentAsString()).
                isEqualTo(objectMapper.writeValueAsString(users));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // given
        String id = "1";

        // when
        mockMvc.perform(delete("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("success"))
                .andDo(print());

        // then
        verify(userService).deleteUserById(any());
    }


    private UserRequest getUserRequest() {
        return UserRequest.builder()
                .name("John Doe")
                .email("doe@gmail.com")
                .password("123123")
                .build();
    }
}