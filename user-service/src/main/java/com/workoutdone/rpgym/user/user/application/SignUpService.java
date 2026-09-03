package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
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

    //회원가입
    public SignUpResult signUp(SignUpCommand command) {
        // 이메일 및 닉네임 중복 여부 확인
        validateDuplicateEmail(command.getEmail());
        validateDuplicateNickname(command.getNickname());

        // 입력받은 평문 비밀번호를 BCrypt로 해싱하여 저장
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

    //이메일 중복 확인 메서드
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BaseException(UserErrorCode.EMAIL_DUPLICATED);
        }
    }

    //닉네임 중복 확인 메서드
    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
            throw new BaseException(UserErrorCode.NICKNAME_DUPLICATED);
        }
    }
}
