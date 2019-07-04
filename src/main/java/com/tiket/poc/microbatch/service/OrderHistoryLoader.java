package com.tiket.poc.microbatch.service;

import com.tiket.poc.microbatch.entity.OrderHistory;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
public interface OrderHistoryLoader {
  Mono<List<OrderHistory>> findOrders(String customerId);
}
