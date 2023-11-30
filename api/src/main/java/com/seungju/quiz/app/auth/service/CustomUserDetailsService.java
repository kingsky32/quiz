package com.seungju.quiz.app.auth.service;

import com.seungju.quiz.app.user.domain.Account;
import com.seungju.quiz.app.user.domain.AccountRepository;
import com.seungju.quiz.app.auth.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User 가 존재하지 않습니다"));

        return new CustomUserDetails(account);
    }

}
