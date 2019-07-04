package com.tiket.poc.microbatch.dao;

import com.tiket.poc.microbatch.entity.MemberAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author zakyalvan
 */
@Validated
@Repository
class MysqlMemberAccountFinder implements MemberAccountFinder {
  private static final String MEMBER_EXISTS_QUERY = "SELECT CASE WHEN EXISTS(SELECT email_address FROM member_account WHERE email_address=:emailAddress) THEN 1 ELSE 0 END AS member_exists";
  private static final String MEMBER_ACCOUNT_QUERY = "SELECT * FROM member_account AS a WHERE a.email_address=:emailAddress";

  private final RowMapper<Boolean> existsMapper = (resultSet, rowNumber) -> resultSet.getBoolean("member_exists");
  private final RowMapper<MemberAccount> memberMapper = (resultSet, rowNumber) ->
      MemberAccount.builder()
          .emailAddress(resultSet.getString("email_address"))
          .loginPassword(resultSet.getString("login_password"))
          .fullName(resultSet.getString("full_name"))
          .build();

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public MysqlMemberAccountFinder(JdbcTemplate jdbcTemplate) {
    Assert.notNull(jdbcTemplate, "Jdbc template must be provided");
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  @Override
  public Mono<MemberAccount> findMember(String memberEmail) {
    return Mono
        .create(emailSink -> {
          if(StringUtils.hasText(memberEmail)) {
            emailSink.success(memberEmail);
          }
          else {
            emailSink.error(new IllegalArgumentException("No member email provided"));
          }
        })
        .filterWhen(emailAddress -> Mono
            .fromCallable(() -> jdbcTemplate.queryForObject(MEMBER_EXISTS_QUERY, new MapSqlParameterSource().addValue("emailAddress", emailAddress), existsMapper))
            .subscribeOn(Schedulers.elastic())
        )
        .flatMap(emailAddress -> Mono
            .fromCallable(() -> jdbcTemplate.queryForObject(MEMBER_ACCOUNT_QUERY, new MapSqlParameterSource().addValue("emailAddress", memberEmail), memberMapper)))
            .subscribeOn(Schedulers.elastic()
        );
  }
}
