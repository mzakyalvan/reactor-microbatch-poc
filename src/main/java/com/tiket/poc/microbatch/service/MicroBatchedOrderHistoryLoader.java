package com.tiket.poc.microbatch.service;

import com.tiket.poc.microbatch.dao.OrderInquiryTransformer;
import com.tiket.poc.microbatch.entity.OrderHistory;
import java.time.Duration;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;

/**
 * @author zakyalvan
 */
@Slf4j
@Service
@Validated
class MicroBatchedOrderHistoryLoader implements OrderHistoryLoader, InitializingBean, DisposableBean {

  private final FluxSink<String> inquirySink;

  private final Flux<OrderHistory> processStream;

  @Min(1)
  @NotNull
  private Integer windowSize = 100;

  @NotNull
  private Duration windowTimeout = Duration.ofMillis(1000);

  private Disposable processDisposable;

  public MicroBatchedOrderHistoryLoader(OrderInquiryTransformer inquiryTransformer) {
    Assert.notNull(inquiryTransformer, "Order inquiry transformer must be provided");

    UnicastProcessor<String> findProcessor = UnicastProcessor.create();
    this.inquirySink = findProcessor.sink(OverflowStrategy.ERROR);
    this.processStream = findProcessor
        .windowTimeout(windowSize, windowTimeout)
        .flatMap(identityStream -> inquiryTransformer.processInquiry(identityStream).subscribeOn(Schedulers.elastic()))
        .publish().autoConnect(1);
  }

  public void setWindowSize(Integer windowSize) {
    this.windowSize = windowSize;
  }

  public void setWindowTimeout(Duration windowTimeout) {
    this.windowTimeout = windowTimeout;
  }

  @Override
  public Mono<List<OrderHistory>> findOrders(String customerId) {
    return Mono.fromRunnable(() -> inquirySink.next(customerId))
        .then(processStream.groupBy(orderHistory -> orderHistory.getCustomerId())
            .filter(groupedStream -> groupedStream.key().equalsIgnoreCase(customerId))
            .take(1).next()
            .flatMap(GroupedFlux::collectList)
        );
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    processDisposable = processStream.subscribe();
  }

  @Override
  public void destroy() throws Exception {
    if(processDisposable != null && processDisposable.isDisposed()) {
      processDisposable.dispose();
    }
  }
}
