package com.tiket.poc.microbatch.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@SuppressWarnings("serial")
public class MemberAccount implements Serializable {
  private String emailAddress;
  private String loginPassword;
  private String fullName;

  @lombok.Builder(builderClassName = "Builder")
  MemberAccount(String emailAddress, String loginPassword, String fullName) {
    this.emailAddress = emailAddress;
    this.loginPassword = loginPassword;
    this.fullName = fullName;
  }
}
