package com.tiket.poc.microbatch.controller;

import com.tiket.poc.microbatch.entity.OrderHistory;
import com.tiket.poc.microbatch.service.OrderHistoryLoader;
import java.security.Principal;
import java.util.List;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author zakyalvan
 */
@RestController
@RequestMapping("/orders")
public class CustomerOrderController {
  private final OrderHistoryLoader orderHistories;

  public CustomerOrderController(OrderHistoryLoader orderHistories) {
    Assert.notNull(orderHistories, "Order history loader must be provided");
    this.orderHistories = orderHistories;
  }

  @GetMapping
  Mono<List<OrderHistory>> orderHistories(Mono<Principal> principalStream) {
    return principalStream
        .flatMap(principal -> orderHistories.findOrders(principal.getName())
            .subscribeOn(Schedulers.elastic())
        );
  }
}
