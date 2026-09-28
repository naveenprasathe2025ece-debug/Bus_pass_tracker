package com.buspass.service;

import com.buspass.dto.RouteDtos.RouteRequest;
import com.buspass.dto.RouteDtos.RouteResponse;
import com.buspass.exception.BusinessRuleException;
import com.buspass.exception.ResourceNotFoundException;
import com.buspass.model.BusRoute;
import com.buspass.repository.BusRouteRepository;
import com.buspass.repository.PassApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusRouteService {

    private final BusRouteRepository routeRepository;
    private final PassApplicationRepository applicationRepository;

    public BusRouteService(BusRouteRepository routeRepository, PassApplicationRepository applicationRepository) {
        this.routeRepository = routeRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> list(boolean includeInactive) {
        List<BusRoute> routes = includeInactive
                ? routeRepository.findAllByOrderByRouteNumberAsc()
                : routeRepository.findByActiveTrueOrderByRouteNumberAsc();
        return routes.stream().map(RouteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RouteResponse get(Long id) {
        return RouteResponse.from(find(id));
    }

    @Transactional
    public RouteResponse create(RouteRequest request) {
        String number = request.routeNumber().trim().toUpperCase();
        if (routeRepository.existsByRouteNumber(number)) {
            throw new BusinessRuleException("A route with number " + number + " already exists.");
        }
        BusRoute route = new BusRoute();
        apply(route, request, number);
        return RouteResponse.from(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse update(Long id, RouteRequest request) {
        BusRoute route = find(id);
        String number = request.routeNumber().trim().toUpperCase();
        if (!number.equals(route.getRouteNumber()) && routeRepository.existsByRouteNumber(number)) {
            throw new BusinessRuleException("A route with number " + number + " already exists.");
        }
        apply(route, request, number);
        return RouteResponse.from(routeRepository.save(route));
    }

    @Transactional
    public void delete(Long id) {
        BusRoute route = find(id);
        if (applicationRepository.existsByRouteId(id)) {
            throw new BusinessRuleException(
                    "This route already has pass applications and cannot be deleted. Mark it inactive instead.");
        }
        routeRepository.delete(route);
    }

    private BusRoute find(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + id));
    }

    private void apply(BusRoute route, RouteRequest r, String number) {
        route.setRouteNumber(number);
        route.setRouteName(r.routeName().trim());
        route.setStartPoint(r.startPoint().trim());
        route.setEndPoint(r.endPoint().trim());
        route.setFare(r.fare());
        route.setActive(r.active() == null || r.active());
    }
}
