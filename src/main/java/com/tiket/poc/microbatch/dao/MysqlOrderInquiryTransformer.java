package com.tiket.poc.microbatch.dao;

import static org.springframework.util.CollectionUtils.isEmpty;

import com.tiket.poc.microbatch.entity.OrderHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author zakyalvan
 */
@Slf4j
@Validated
@Repository
class MysqlOrderInquiryTransformer implements OrderInquiryTransformer {
  private static final String DEFAULT_ORDER_QUERY = "SELECT * FROM customer_order o WHERE o.customer_email IN :customerIds";

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final String orderQuery;
  private final RowMapper<OrderHistory> orderMapper = new OrderRowMapper();

  MysqlOrderInquiryTransformer(JdbcTemplate jdbcTemplate) {
    Assert.notNull(jdbcTemplate, "Jdbc template object must be provided");
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.orderQuery = DEFAULT_ORDER_QUERY;
  }

  @Override
  public Flux<OrderHistory> processInquiry(Flux<String> customerStream) {
    return customerStream.collectList()
//        .doOnNext(customerIds -> log.debug("."))
        .filter(customerIds -> !isEmpty(customerIds))
        .doOnNext(customerIds -> log.debug("Finding customer's order for customers {}", customerIds))
        .flatMapMany(customerIds -> Flux.defer(() -> {
          MapSqlParameterSource queryParameters = new MapSqlParameterSource();
          queryParameters.addValue("customerIds", customerIds);
          return Flux.fromIterable(jdbcTemplate.query(orderQuery, queryParameters, orderMapper))
              .doOnNext(orderHistory -> log.debug("Found order history {}", orderHistory));
        }).subscribeOn(Schedulers.elastic()));
  }
}
