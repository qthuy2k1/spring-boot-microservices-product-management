package com.qthuy2k1.userservice.mapper;

import com.qthuy2k1.userservice.dto.request.UserRequest;
import com.qthuy2k1.userservice.dto.request.UserUpdateRequest;
import com.qthuy2k1.userservice.dto.response.UserResponse;
import com.qthuy2k1.userservice.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserModel toUser(UserRequest request);

    UserResponse toUserResponse(UserModel user);


    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget UserModel user, UserUpdateRequest request);
}
