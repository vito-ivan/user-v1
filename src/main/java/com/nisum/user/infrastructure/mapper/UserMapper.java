package com.nisum.user.infrastructure.mapper;

import com.nisum.user.adapters.api.model.Phone;
import com.nisum.user.adapters.api.model.UserResponse;
import com.nisum.user.domain.User;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UserMapper {

    public static UserResponse toResponse(User user) {

        var phones = user.getPhones().stream().map(phone -> new Phone()
                .number(phone.getNumber())
                .cityCode(phone.getCityCode())
                .countryCode(phone.getCountryCode())).toList();

        return new UserResponse()
                .id(UUID.fromString(user.getId()))
                .name(user.getName())
                .email(user.getEmail())
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.isActive())
                .phones(phones);
    }
}
