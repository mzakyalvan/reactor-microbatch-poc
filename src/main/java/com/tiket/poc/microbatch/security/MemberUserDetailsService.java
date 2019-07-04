package com.tiket.poc.microbatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiket.poc.microbatch.dao.MemberAccountFinder;
import com.tiket.poc.microbatch.entity.MemberAccount;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.util.Collections.*;

/**
 * @author zakyalvan
 */
@Slf4j
@Component
class MemberUserDetailsService implements ReactiveUserDetailsService {
  private final MemberAccountFinder memberAccounts;
  private final ReactiveRedisTemplate<Object, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final Duration cacheDuration = Duration.ofMinutes(10);

  MemberUserDetailsService(MemberAccountFinder memberAccounts, ReactiveRedisTemplate<Object, Object> redisTemplate, ObjectMapper objectMapper) {
    this.memberAccounts = memberAccounts;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<UserDetails> findByUsername(String memberEmail) {
    return resolveCached(memberEmail)
        .switchIfEmpty(memberAccounts.findMember(memberEmail)
            .map(memberAccount -> new User(memberAccount.getEmailAddress(), memberAccount.getLoginPassword(), emptySet()))
            .flatMap(userDetails -> populateCache(userDetails)
                .then(Mono.just(userDetails))
            )
        )
        .doOnSubscribe(subscription -> log.debug("Find member details (for login purpose) with email address '{}'", memberEmail));
  }

  private Mono<Void> populateCache(User userDetails) {
    return Mono.empty();
  }

  private Mono<UserDetails> resolveCached(String customerEmail) {
    return Mono.empty();
  }
}
