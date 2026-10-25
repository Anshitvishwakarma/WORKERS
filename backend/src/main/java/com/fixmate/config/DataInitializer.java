package com.fixmate.config;

import com.fixmate.entity.Booking;
import com.fixmate.entity.ProviderService;
import com.fixmate.entity.Service;
import com.fixmate.entity.User;
import com.fixmate.enums.BookingStatus;
import com.fixmate.enums.Role;
import com.fixmate.repository.BookingRepository;
import com.fixmate.repository.ProviderServiceRepository;
import com.fixmate.repository.ServiceRepository;
import com.fixmate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderServiceRepository providerServiceRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fixmate.admin.email:admin@fixmate.com}")
    private String adminEmail;

    @Value("${fixmate.admin.password:Admin@123456}")
    private String adminPassword;

    @Value("${fixmate.admin.name:FixMate Administrator}")
    private String adminName;

    @Value("${fixmate.admin.phone:1800-FIX-MATE}")
    private String adminPhone;

    @Value("${fixmate.admin.address:FixMate Global HQ, 100 Innovation Way}")
    private String adminAddress;

    public DataInitializer(UserRepository userRepository,
                           ServiceRepository serviceRepository,
                           ProviderServiceRepository providerServiceRepository,
                           BookingRepository bookingRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.providerServiceRepository = providerServiceRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing FixMate system data...");
        seedAdminUser();
        seedStandardServices();
        seedSampleUsersAndBookings();
        log.info("FixMate data initialization complete.");
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            User admin = new User();
            admin.setFullName(adminName);
            admin.setEmail(adminEmail);
            admin.setPhone(adminPhone);
            admin.setAddress(adminAddress);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            log.info("Initialized default administrator: {}", adminEmail);
        }
    }

    private void seedStandardServices() {
        if (serviceRepository.count() == 0) {
            List<Service> coreServices = Arrays.asList(
                    new Service(null, "Plumbing", "Expert pipe fixing, drain cleaning, leak repair, and bathroom fixture installation.", "bi-wrench-adjustable", true),
                    new Service(null, "Electrician", "Wiring, fuse repair, switchboard installations, lighting fixtures, and electrical safety audits.", "bi-lightning-charge", true),
                    new Service(null, "Home Cleaning", "Deep home sanitization, kitchen and bathroom cleaning, floor polishing, and post-renovation cleanup.", "bi-stars", true),
                    new Service(null, "AC Repair", "Air conditioning servicing, gas refilling, filter cleaning, cooling diagnostics, and installation.", "bi-snow", true),
                    new Service(null, "Carpenter", "Custom wood repairs, furniture assembly, door & window hinge fixes, and cabinet making.", "bi-hammer", true),
                    new Service(null, "Painting", "Interior and exterior wall painting, waterproof coating, primer application, and texture finishing.", "bi-brush", true),
                    new Service(null, "Appliance Repair", "Washing machine, refrigerator, microwave, and dishwasher diagnostic and repair services.", "bi-plug", true),
                    new Service(null, "RO & Water Purifier", "Filter replacement, membrane cleaning, water testing, and installation of all purifier brands.", "bi-droplet-half", true)
            );
            serviceRepository.saveAll(coreServices);
            log.info("Seeded 8 standard home services into catalog.");
        }
    }

    private void seedSampleUsersAndBookings() {
        // Sample Customer
        String customerEmail = "customer@fixmate.com";
        User customer;
        if (!userRepository.existsByEmailIgnoreCase(customerEmail)) {
            customer = new User(null, "Sarah Jenkins", customerEmail, "+1-555-0199",
                    "742 Evergreen Terrace, Springfield", passwordEncoder.encode("Customer@123"), Role.CUSTOMER, true);
            customer = userRepository.save(customer);
            log.info("Seeded demo customer: {}", customerEmail);
        } else {
            customer = userRepository.findByEmailIgnoreCase(customerEmail).orElse(null);
        }

        // Sample Provider 1
        String provider1Email = "plumber@fixmate.com";
        User provider1;
        if (!userRepository.existsByEmailIgnoreCase(provider1Email)) {
            provider1 = new User(null, "David Miller (Master Plumbing)", provider1Email, "+1-555-0144",
                    "120 Water St, Brooklyn, NY", passwordEncoder.encode("Provider@123"), Role.PROVIDER, true);
            provider1 = userRepository.save(provider1);
            log.info("Seeded demo provider: {}", provider1Email);
        } else {
            provider1 = userRepository.findByEmailIgnoreCase(provider1Email).orElse(null);
        }

        // Sample Provider 2
        String provider2Email = "electric@fixmate.com";
        User provider2;
        if (!userRepository.existsByEmailIgnoreCase(provider2Email)) {
            provider2 = new User(null, "Elena Rostova (Spark Electric)", provider2Email, "+1-555-0188",
                    "45 Volt Avenue, Queens, NY", passwordEncoder.encode("Provider@123"), Role.PROVIDER, true);
            provider2 = userRepository.save(provider2);
            log.info("Seeded demo provider: {}", provider2Email);
        } else {
            provider2 = userRepository.findByEmailIgnoreCase(provider2Email).orElse(null);
        }

        // Seed Provider Services
        if (provider1 != null && provider2 != null && providerServiceRepository.count() == 0) {
            Service plumbing = serviceRepository.findByNameIgnoreCase("Plumbing").orElse(null);
            Service electrician = serviceRepository.findByNameIgnoreCase("Electrician").orElse(null);
            Service appliance = serviceRepository.findByNameIgnoreCase("Appliance Repair").orElse(null);
            Service acRepair = serviceRepository.findByNameIgnoreCase("AC Repair").orElse(null);

            ProviderService ps1 = null;
            ProviderService ps2 = null;

            if (plumbing != null) {
                ps1 = providerServiceRepository.save(new ProviderService(null, provider1, plumbing,
                        new BigDecimal("85.00"), 8, "Mon-Sat 8AM-7PM"));
            }
            if (appliance != null) {
                providerServiceRepository.save(new ProviderService(null, provider1, appliance,
                        new BigDecimal("95.00"), 5, "Mon-Fri 9AM-5PM"));
            }
            if (electrician != null) {
                ps2 = providerServiceRepository.save(new ProviderService(null, provider2, electrician,
                        new BigDecimal("90.00"), 10, "Mon-Sun 24/7 Emergency"));
            }
            if (acRepair != null) {
                providerServiceRepository.save(new ProviderService(null, provider2, acRepair,
                        new BigDecimal("110.00"), 6, "Mon-Sat 9AM-6PM"));
            }

            // Seed Sample Bookings
            if (customer != null && ps1 != null && ps2 != null && bookingRepository.count() == 0) {
                Booking b1 = new Booking(null, customer, ps1,
                        LocalDate.now().plusDays(2), LocalTime.of(10, 0),
                        "742 Evergreen Terrace, Springfield", "Bathroom pipe leaking under the sink.",
                        ps1.getPrice(), BookingStatus.PENDING);
                bookingRepository.save(b1);

                Booking b2 = new Booking(null, customer, ps2,
                        LocalDate.now().minusDays(3), LocalTime.of(14, 30),
                        "742 Evergreen Terrace, Springfield", "Master bedroom main switchboard sparked and tripped circuit breaker.",
                        ps2.getPrice(), BookingStatus.COMPLETED);
                bookingRepository.save(b2);

                log.info("Seeded sample bookings for demo customer.");
            }
        }
    }
}
