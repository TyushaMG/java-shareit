package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    UserDto edit(UserDto user, Long userId);

    void delete(Long userId);

    List<UserDto> getAll();

    UserDto get(Long userId);
}
