package com.tiket.poc.microbatch.dao;

import com.tiket.poc.microbatch.entity.MemberAccount;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
public interface MemberAccountFinder {
  Mono<MemberAccount> findMember(String memberEmail);
}
