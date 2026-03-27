package com.whylog.server.global.auth.jwt.application;

import com.whylog.server.domain.user.exception.AuthErrorStatus;
import com.whylog.server.global.auth.jwt.dao.TokenRepository;
import com.whylog.server.global.auth.redis.Token;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    @Transactional
    public void saveRefreshToken(Long memberId, String refreshToken) {
        tokenRepository.save(Token.of(memberId, refreshToken));
    }

    @Transactional(readOnly = true)
    public Long findIdByRefreshToken(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken)
                .map(Token::getId)
                .orElseThrow(() -> new ErrorHandler(AuthErrorStatus.REFRESH_TOKEN_NOT_FOUND));
    }

    @Transactional
    public void deleteRefreshToken(Long memberId) {
        Token token = tokenRepository.findById(memberId)
                .orElseThrow(() -> new ErrorHandler(AuthErrorStatus.REFRESH_TOKEN_NOT_FOUND));
        tokenRepository.delete(token);
    }
}
