package com.buspass.controller;

import com.buspass.dto.RouteDtos.RouteRequest;
import com.buspass.dto.RouteDtos.RouteResponse;
import com.buspass.service.BusRouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** GET is open to any logged-in user; POST/PUT/DELETE are ADMIN only (see SecurityConfig). */
@RestController
@RequestMapping("/api/routes")
public class BusRouteController {

    private final BusRouteService routeService;

    public BusRouteController(BusRouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return routeService.list(includeInactive);
    }

    @GetMapping("/{id}")
    public RouteResponse get(@PathVariable Long id) {
        return routeService.get(id);
    }

    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.create(request));
    }

    @PutMapping("/{id}")
    public RouteResponse update(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return routeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
