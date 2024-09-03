package ru.yandex.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.users.User;
import ru.yandex.practicum.users.UserRepository;
import ru.yandex.practicum.users.dto.NewUserRequest;
import ru.yandex.practicum.users.dto.UserDto;
import ru.yandex.practicum.users.dto.UserMapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.users.dto.UserMapper.toUser;
import static ru.yandex.practicum.users.dto.UserMapper.toUserDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("В сервисе создаем пользователя");
        User user = toUser(newUserRequest);
        return toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers(List<Long> userIds, int from, int size) {
        log.info("В сервисе получаем пользователей");
        Pageable pageable = PageRequest.of(from / size, size);
        if (userIds != null) {
            return userRepository.findAllByIdIn(userIds, pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAll(pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void deleteUserById(Long id) {
        log.info("В сервисе удаляем пользователя");
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с данным id не найден");
        }
        userRepository.deleteById(id);
    }
}