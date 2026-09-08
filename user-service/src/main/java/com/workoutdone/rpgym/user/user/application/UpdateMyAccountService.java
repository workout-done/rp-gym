package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.common.exception.CommonErrorCode;
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
public class UpdateMyAccountService {

    // V1__create_users_table.sql에 정의된 부분 유니크 인덱스 이름과 동일해야 한다.
    private static final String NICKNAME_UNIQUE_CONSTRAINT = "ux_users_nickname_active";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateMyAccountResult updateMyAccount(UpdateMyAccountCommand command) {
        User user = userRepository.findByIdAndDeletedAtIsNull(command.getUserId())
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 요청 바디에 없는(null) 필드는 기존 값을 그대로 유지하는 부분 수정
        if (command.getNickname() != null) {
            validateDuplicateNickname(command.getNickname(), user);
            user.changeNickname(command.getNickname());
        }

        if (command.getSlackId() != null) {
            user.changeSlackId(command.getSlackId());
        }

        // currentPassword만 있고 newPassword가 없으면 비밀번호 변경 없이 다른 필드만 수정
        if (command.getNewPassword() != null) {
            validateCurrentPasswordProvided(command.getCurrentPassword());
            validateCurrentPasswordMatches(command.getCurrentPassword(), user);
            user.changePassword(passwordEncoder.encode(command.getNewPassword()));
        }

        // updatedAt(@LastModifiedDate)은 실제 UPDATE가 flush될 때 갱신되는데, 트랜잭션 커밋 시점까지
        // flush를 미루면 응답에는 갱신 전 값이 담기므로 여기서 즉시 flush해서 최신 값을 읽는다.
        // 사전에 닉네임 중복을 확인했더라도 동시 요청으로 그 사이 다른 요청이 먼저 커밋됐을 수 있어
        // 최종 방어선인 DB 부분 유니크 인덱스 위반을 여기서 바로 확인한다.
        User updatedUser;
        try {
            updatedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw resolveDuplicateException(e);
        }

        return UpdateMyAccountResult.from(updatedUser);
    }

    private void validateDuplicateNickname(String nickname, User user) {
        if (userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull(nickname, user.getId())) {
            throw new BaseException(UserErrorCode.NICKNAME_DUPLICATED);
        }
    }

    // 어떤 유니크 제약조건이 위반됐는지 확인해서 알맞은 중복 에러로 변환
    // 예상하지 못한 제약조건 위반이면 그대로 다시 던져서 500으로 처리되게 둠
    private BaseException resolveDuplicateException(DataIntegrityViolationException e) {
        if (e.getCause() instanceof ConstraintViolationException cve) {
            if (NICKNAME_UNIQUE_CONSTRAINT.equals(cve.getConstraintName())) {
                return new BaseException(UserErrorCode.NICKNAME_DUPLICATED);
            }
        }

        throw e;
    }

    // newPassword를 보낼 때 currentPassword가 없으면 INVALID_INPUT으로 처리
    private void validateCurrentPasswordProvided(String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BaseException(CommonErrorCode.INVALID_INPUT);
        }
    }

    //비밀번호 변경 시, 입력한 현재 비밀번호와 회원의 실제 비밀번호가 같은지 검증
    private void validateCurrentPasswordMatches(String currentPassword, User user) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BaseException(UserErrorCode.LOGIN_FAILED);
        }
    }
}
