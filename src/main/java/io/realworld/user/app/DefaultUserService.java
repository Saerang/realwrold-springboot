package io.realworld.user.app;

import io.realworld.common.exception.PasswordNotMatchedException;
import io.realworld.common.exception.UserAlreadyExistException;
import io.realworld.common.exception.UserNotFoundException;
import io.realworld.user.api.dto.UserCreateRequestDto;
import io.realworld.user.api.dto.UserLoginRequestDto;
import io.realworld.user.api.dto.UserUpdateRequestDto;
import io.realworld.user.domain.User;
import io.realworld.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static io.realworld.user.app.enumerate.LoginType.EMAIL;
import static io.realworld.user.app.enumerate.LoginType.USER_ID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    final private UserRepository userRepository;
    final private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserCreateRequestDto dto) {
        Optional<User> optionalUser = userRepository.findByEmailOrUsername(dto.getUserDto().getEmail(), dto.getUserDto().getUsername());
        if (optionalUser.isPresent()) {
            throw new UserAlreadyExistException(optionalUser.get().getId());
        }

        User user = User.builder()
                .email(dto.getUserDto().getEmail())
                .password(passwordEncode(dto.getUserDto().getPassword()))
                .username(dto.getUserDto().getUsername())
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        // ToDo: service 분리하는게 좋아보임.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserNotFoundException();
        }

        return getUserByEmail(authentication.getName());
    }

    @Override
    public User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(USER_ID.getMessage() + userId));
    }

    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findByIdIn(userIds);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public User updateUser(UserUpdateRequestDto dto, long userId) {
        Optional<User> findUser = userRepository.findByEmailOrUsername(dto.getUserDto().getEmail(), dto.getUserDto().getUsername());

        if (findUser.isPresent() && findUser.get().getId() != userId) {
            throw new UserAlreadyExistException(findUser.get().getId());
        }

        User user = getCurrentUser();
        user.updateUserInfo(
                dto.getUserDto().getEmail(), dto.getUserDto().getUsername(),
                dto.getUserDto().getPassword(), passwordEncoder,
                dto.getUserDto().getImage(), dto.getUserDto().getBio()
        );
        return user;
    }

    @Override
    public User login(UserLoginRequestDto dto) {
        User user = getUserByEmail(dto.getUserDto().getEmail());

        if (!passwordEncoder.matches(dto.getUserDto().getPassword(), user.getPassword())) {
            throw new PasswordNotMatchedException(user.getId());
        }

        return user;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(EMAIL.getMessage() + email));
    }

    private String passwordEncode(String password) {
        return passwordEncoder.encode(password);
    }


}
