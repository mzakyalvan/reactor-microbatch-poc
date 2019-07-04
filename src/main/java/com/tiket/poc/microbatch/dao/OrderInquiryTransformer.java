package com.tiket.poc.microbatch.dao;

import com.tiket.poc.microbatch.entity.OrderHistory;
import javax.validation.constraints.NotNull;
import reactor.core.publisher.Flux;

/**
 * @author zakyalvan
 */
public interface OrderInquiryTransformer {
  Flux<OrderHistory> processInquiry(@NotNull Flux<String> customerStream);
}
