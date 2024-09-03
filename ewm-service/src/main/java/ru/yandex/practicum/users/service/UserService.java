package ru.yandex.practicum.users.service;


import ru.yandex.practicum.users.dto.NewUserRequest;
import ru.yandex.practicum.users.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService  {

    UserDto createUser(NewUserRequest newUserRequest);

    Collection<UserDto> getAllUsers(List<Long> userIds, int from, int size);

    void deleteUserById(Long id);
}