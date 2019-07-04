package com.tiket.poc.microbatch.dao;

import com.tiket.poc.microbatch.entity.OrderHistory;
import com.tiket.poc.microbatch.entity.ProductType;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author zakyalvan
 */
class OrderRowMapper implements RowMapper<OrderHistory> {
  @Override
  public OrderHistory mapRow(ResultSet resultSet, int rowNum) throws SQLException {
    ProductType productType = ProductType.valueOf(resultSet.getString("product_type"));
    return null;
  }

  private OrderHistory mapFlightOrder(ResultSet resultSet, int rowNumber) {
    return null;
  }
  private OrderHistory mapHotelOrder(ResultSet resultSet, int rowNumber) {
    return null;
  }
  private OrderHistory mapTrainOrder(ResultSet resultSet, int rowNumber) {
    return null;
  }
  private OrderHistory mapCarRentalOrder(ResultSet resultSet, int rowNumber) {
    return null;
  }
  private OrderHistory mapEventOrder(ResultSet resultSet, int rowNumber) {
    return null;
  }
}
