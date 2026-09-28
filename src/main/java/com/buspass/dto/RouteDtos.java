package com.buspass.dto;

import com.buspass.model.BusRoute;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class RouteDtos {

    private RouteDtos() {
    }

    public record RouteRequest(
            @NotBlank(message = "Route number is required")
            @Size(max = 20, message = "Route number must be at most 20 characters")
            String routeNumber,

            @NotBlank(message = "Route name is required")
            @Size(max = 150, message = "Route name must be at most 150 characters")
            String routeName,

            @NotBlank(message = "Start point is required")
            @Size(max = 100, message = "Start point must be at most 100 characters")
            String startPoint,

            @NotBlank(message = "End point is required")
            @Size(max = 100, message = "End point must be at most 100 characters")
            String endPoint,

            @NotNull(message = "Fare is required")
            @Positive(message = "Fare must be greater than zero")
            BigDecimal fare,

            Boolean active) {
    }

    public record RouteResponse(Long id, String routeNumber, String routeName,
                                String startPoint, String endPoint, BigDecimal fare, boolean active) {

        public static RouteResponse from(BusRoute r) {
            return new RouteResponse(r.getId(), r.getRouteNumber(), r.getRouteName(),
                    r.getStartPoint(), r.getEndPoint(), r.getFare(), r.isActive());
        }
    }
}
