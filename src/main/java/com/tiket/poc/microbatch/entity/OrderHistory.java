package com.tiket.poc.microbatch.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
public class OrderHistory implements Serializable {
  private String customerId;
  private ProductType productType;
  private BigDecimal paidAmount;

  @JsonCreator
  @lombok.Builder(builderClassName = "Builder")
  public OrderHistory(@JsonProperty String customerId, ProductType productType, BigDecimal paidAmount) {
    this.customerId = customerId;
    this.productType = productType;
    this.paidAmount = paidAmount;
  }
}
