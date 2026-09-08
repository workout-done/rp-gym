package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {

    // V1__create_users_table.sql에 정의된 부분 유니크 인덱스 이름과 동일해야 한다.
    private static final String EMAIL_UNIQUE_CONSTRAINT = "ux_users_email_active";
    private static final String NICKNAME_UNIQUE_CONSTRAINT = "ux_users_nickname_active";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    public SignUpResult signUp(SignUpCommand command) {
        // 이메일은 대소문자를 구분하지 않으므로, 중복 확인·저장 모두 정규화된 값 기준으로 처리
        String normalizedEmail = User.normalizeEmail(command.getEmail());

        // 이메일 및 닉네임 중복 여부 확인
        validateDuplicateEmail(normalizedEmail);
        validateDuplicateNickname(command.getNickname());

        // 입력받은 평문 비밀번호를 BCrypt로 해싱하여 저장
        String encodedPassword = passwordEncoder.encode(command.getRawPassword());

        User user = User.create(
                normalizedEmail,
                encodedPassword,
                command.getNickname(),
                command.getSlackId()
        );

        // 사전 중복 확인을 통과했더라도 동시 요청으로 인해 그 사이 다른 요청이 먼저 커밋됐을 수 있음
        // 최종 방어선은 DB 부분 유니크 인덱스이며, saveAndFlush로 즉시 INSERT를 실행해서
        // (트랜잭션 커밋 시점까지 미루지 않고) 여기서 바로 위반 여부를 확인함
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw resolveDuplicateException(e);
        }

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

    // 어떤 유니크 제약조건이 위반됐는지 확인해서 알맞은 중복 에러로 변환
    // 예상하지 못한 제약조건 위반이면 그대로 다시 던져서 500으로 처리되게 둠
    private BaseException resolveDuplicateException(DataIntegrityViolationException e) {
        ConstraintViolationException cve = findConstraintViolationException(e);

        if (cve != null) {
            String constraintName = cve.getConstraintName();

            if (EMAIL_UNIQUE_CONSTRAINT.equals(constraintName)) {
                return new BaseException(UserErrorCode.EMAIL_DUPLICATED);
            }
            if (NICKNAME_UNIQUE_CONSTRAINT.equals(constraintName)) {
                return new BaseException(UserErrorCode.NICKNAME_DUPLICATED);
            }
        }

        throw e;
    }

    // 드라이버/커넥션 풀 등에 의해 예외가 한 단계 이상 더 감싸져 들어오는 환경도 대비해,
    // e.getCause() 한 단계만 보지 않고 원인 체인을 끝까지 순회하며 찾는다.
    private ConstraintViolationException findConstraintViolationException(Throwable throwable) {
        Throwable cause = throwable;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return cve;
            }
            cause = cause.getCause();
        }

        return null;
    }
}
