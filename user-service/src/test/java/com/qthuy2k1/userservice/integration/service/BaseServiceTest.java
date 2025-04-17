package com.qthuy2k1.userservice.service;

import com.qthuy2k1.userservice.mapper.PermissionMapper;
import com.qthuy2k1.userservice.mapper.RoleMapper;
import com.qthuy2k1.userservice.model.Permission;
import com.qthuy2k1.userservice.model.Role;
import com.qthuy2k1.userservice.model.UserModel;
import com.qthuy2k1.userservice.repository.InvalidatedRepository;
import com.qthuy2k1.userservice.repository.PermissionRepository;
import com.qthuy2k1.userservice.repository.RoleRepository;
import com.qthuy2k1.userservice.repository.UserRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
public abstract class BaseServiceTest {
    protected static final int REDIS_PORT = 6379;
    @Container
    protected static final RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine"))
                    .withExposedPorts(REDIS_PORT);
    protected static final String USER_PASSWORD = "123123";
    @Container
    protected static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );
    protected final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);
    protected final PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);
    @Autowired
    protected PasswordEncoder passwordEncoder;
    protected UserModel userSaved;
    protected Role userRoleSaved;
    protected Role adminRoleSaved;
    protected Permission permissionReadSaved;
    protected Permission permissionWriteSaved;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected PermissionRepository permissionRepository;
    @Autowired
    protected InvalidatedRepository invalidatedRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        // for redis
        // for spring boot 3 must be spring.data.redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
    }

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        invalidatedRepository.deleteAll();

        permissionReadSaved = permissionRepository.save(Permission.builder()
                .name("READ_DATA")
                .description("read data description")
                .build());
        permissionWriteSaved = permissionRepository.save(Permission.builder()
                .name("WRITE_DATA")
                .description("write data description")
                .build());
        userRoleSaved = roleRepository.save(Role.builder()
                .name("USER")
                .description("user role")
                .permissions(Set.of(permissionReadSaved))
                .build());
        adminRoleSaved = roleRepository.save(Role.builder()
                .name("ADMIN")
                .description("admin role")
                .permissions(Set.of(permissionReadSaved, permissionWriteSaved))
                .build());
        userSaved = userRepository.save(UserModel.builder()
                .name("user 9999")
                .email("user9999@gmail.com")
                .password(passwordEncoder.encode(USER_PASSWORD))
                .roles(Set.of(userRoleSaved))
                .build());
    }

    @Test
    public void testConnection() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
    }
}