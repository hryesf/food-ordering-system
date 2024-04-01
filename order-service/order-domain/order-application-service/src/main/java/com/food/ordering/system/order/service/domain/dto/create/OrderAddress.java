package com.food.ordering.system.order.service.domain.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
public class OrderAddress {
    @NotNull
    @Max(50)
    private final String street;
    @NotNull
    @Max(10)
    private final String postalCode;
    @NotNull
    @Max(50)
    private final String city;
}
