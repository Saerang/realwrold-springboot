package io.realworld.user.app;

import io.realworld.common.exception.UserNotFoundException;
import io.realworld.user.app.enumerate.LoginType;
import io.realworld.user.domain.User;
import io.realworld.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.realworld.user.app.enumerate.LoginType.*;

@Service
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    final private UserRepository userRepository;

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        if (authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UserNotFoundException(EMAIL.getMessage() + authentication.getName()));

        return Optional.of(user);
    }

    @Override
    public Long getCurrentUserId() {
        Optional<User> currentUser = this.getCurrentUser();

        if (currentUser.isEmpty()) {
            return null;
        }

        return currentUser.get().getId();
    }

}
