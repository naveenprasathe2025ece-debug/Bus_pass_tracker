package com.buspass.repository;

import com.buspass.model.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    boolean existsByRouteNumber(String routeNumber);

    List<BusRoute> findAllByOrderByRouteNumberAsc();

    List<BusRoute> findByActiveTrueOrderByRouteNumberAsc();
}
