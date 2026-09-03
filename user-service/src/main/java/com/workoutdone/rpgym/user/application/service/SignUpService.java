package com.workoutdone.rpgym.user.application.service;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.application.input.SignUpCommand;
import com.workoutdone.rpgym.user.application.output.SignUpResult;
import com.workoutdone.rpgym.user.domain.entity.User;
import com.workoutdone.rpgym.user.domain.exception.UserErrorCode;
import com.workoutdone.rpgym.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignUpResult signUp(SignUpCommand command) {
        validateDuplicateEmail(command.getEmail());
        validateDuplicateNickname(command.getNickname());

        String encodedPassword = passwordEncoder.encode(command.getRawPassword());

        User user = User.create(
                command.getEmail(),
                encodedPassword,
                command.getNickname(),
                command.getSlackId()
        );

        User savedUser = userRepository.save(user);

        return SignUpResult.from(savedUser);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BaseException(UserErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
            throw new BaseException(UserErrorCode.NICKNAME_DUPLICATED);
        }
    }
}
