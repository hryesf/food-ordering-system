package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

// Aggregate root
public class Order extends AggregateRoot<OrderId> {
    private final RestaurantId restaurantId;
    private final CustomerId customerId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    private OrderStatus orderStatus;
    private TrackingId trackingId;
    private List<String> failureMessages;

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }

    public void validateOrder() {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    public void pay() {
        if (orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException("Order status is not in correct state for pay operation");
        }
        orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order status is not in correct state for approve operation");
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages) {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order status is not in correct state for initCancel operation");
        }
        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    public void cancel(List<String> failureMessages) {
        if (!(orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CANCELLING)) {
            throw new OrderDomainException("Order status is not in correct state for Cancel operation");
        }
        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if (this.orderStatus != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages.stream().filter(String::isEmpty).toList());
        }
        if (this.failureMessages == null) {
            this.failureMessages = failureMessages;
        }
    }

    private void validateInitialOrder() {
        if (orderStatus != null && getId() != null) {
            throw new OrderDomainException("Order is not in correct state for initialization!");
        }
    }

    private void validateTotalPrice() {
        if (price != null && !price.isGreaterThanZero()) {
            throw new OrderDomainException("Total price must be greater than zero!");
        }
    }

    private void validateItemsPrice() {
        Money orderItemsTotal = items.stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO, Money::add);

        if (!price.equals(orderItemsTotal)) {
            throw new OrderDomainException("Total price " + price.getAmount() +
                    " is not equal to order items total! " + orderItemsTotal.getAmount());
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
        if (!orderItem.isPriceValid()) {
            throw new OrderDomainException("Order item price: " + orderItem.getPrice().getAmount() +
                    " is not valid for product " + orderItem.getProduct().getId().getValue());
        }
    }

    private void initializeOrderItems() {
        long itemId = 1;
        for (OrderItem item : items) {
            item.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        restaurantId = builder.restaurantId;
        customerId = builder.customerId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        orderStatus = builder.orderStatus;
        trackingId = builder.trackingId;
        failureMessages = builder.failureMessages;
    }


    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public static final class Builder {
        private OrderId orderId;
        private RestaurantId restaurantId;
        private CustomerId customerId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private OrderStatus orderStatus;
        private TrackingId trackingId;
        private List<String> failureMessages;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
