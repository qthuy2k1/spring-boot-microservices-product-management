package com.qthuy2k1.userservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.userservice.dto.request.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.response.ApiResponse;
import com.qthuy2k1.userservice.dto.response.AuthenticationResponse;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
public abstract class BaseControllerTest {
    static final int REDIS_PORT = 6379;
    @Container
    static final RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine"))
                    .withExposedPorts(REDIS_PORT);
    static final int DEFAULT_CODE_RESPONSE = 1000;
    static final String USER_PASSWORD = "123123";
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    @Container
    private static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            "confluentinc/cp-kafka:7.4.0"
    );
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    UserModel userSaved1;
    UserModel userSaved2;
    Role userRoleSaved;
    Role adminRoleSaved;
    Permission permissionSaved;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // for postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // for kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        // for redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        permissionSaved = permissionRepository.save(Permission.builder()
                .name("READ_DATA")
                .description("update data description")
                .build());
        userRoleSaved = roleRepository.save(Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permissionSaved))
                .build());
        adminRoleSaved = roleRepository.save(Role.builder()
                .name("ADMIN")
                .description("admin role")
                .permissions(Set.of(permissionSaved))
                .build());
        userSaved1 = userRepository.save(UserModel.builder()
                .name("user 9991")
                .email("user9991@gmail.com")
                .password(passwordEncoder.encode(USER_PASSWORD))
                .roles(Set.of(userRoleSaved, adminRoleSaved))
                .build());
        userSaved2 = userRepository.save(UserModel.builder()
                .name("user 2222")
                .email("user9992@gmail.com")
                .password(passwordEncoder.encode(USER_PASSWORD))
                .roles(Set.of(userRoleSaved))
                .build());
    }

    @Test
    void testContainersAreRunning() {
        assertThat(redisContainer.isRunning()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
    }


    // for admin role
    String login() throws Exception {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email(userSaved1.getEmail())
                .password(USER_PASSWORD)
                .build();
        MvcResult authResult = mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize the JSON response into ApiResponse<AuthenticationResponse>
        ApiResponse<AuthenticationResponse> authResponse = objectMapper.readValue(
                authResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<AuthenticationResponse>>() {
                }
        );
        assertThat(authResponse).isNotNull();

        return authResponse.getResult().getToken();
    }


    // for user role
    String login(UserModel user) throws Exception {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email(user.getEmail())
                .password(USER_PASSWORD)
                .build();
        MvcResult authResult = mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // Get the response content as a byte array
        byte[] authResponseBytes = authResult.getResponse().getContentAsByteArray();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<AuthenticationResponse> authResponse = objectMapper.readValue(
                authResponseBytes,
                new TypeReference<ApiResponse<AuthenticationResponse>>() {
                }
        );
        assertThat(authResponse).isNotNull();

        return authResponse.getResult().getToken();
    }
}