package com.buspass.config;

import com.buspass.model.BusRoute;
import com.buspass.model.Role;
import com.buspass.model.Student;
import com.buspass.repository.BusRouteRepository;
import com.buspass.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Creates the default admin account and a few sample routes on first start-up. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository studentRepository;
    private final BusRouteRepository routeRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(StudentRepository studentRepository,
                           BusRouteRepository routeRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.name}") String adminName,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.studentRepository = studentRepository;
        this.routeRepository = routeRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!studentRepository.existsByEmail(adminEmail)) {
            Student admin = new Student();
            admin.setName(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setDepartment("Administration");
            admin.setRole(Role.ADMIN);
            studentRepository.save(admin);
            log.info("Default admin created: {}", adminEmail);
        }
        if (routeRepository.count() == 0) {
            routeRepository.save(route("R101", "Gandhipuram - Campus", "Gandhipuram", "Campus", "600.00"));
            routeRepository.save(route("R102", "Ukkadam - Campus", "Ukkadam", "Campus", "650.00"));
            routeRepository.save(route("R103", "Singanallur - Campus", "Singanallur", "Campus", "550.00"));
            routeRepository.save(route("R104", "Peelamedu - Campus", "Peelamedu", "Campus", "500.00"));
            log.info("Sample bus routes created");
        }
    }

    private BusRoute route(String number, String name, String start, String end, String fare) {
        BusRoute r = new BusRoute();
        r.setRouteNumber(number);
        r.setRouteName(name);
        r.setStartPoint(start);
        r.setEndPoint(end);
        r.setFare(new BigDecimal(fare));
        r.setActive(true);
        return r;
    }
}
