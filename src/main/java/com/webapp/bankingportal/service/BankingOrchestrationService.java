package com.webapp.bankingportal.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class BankingOrchestrationService {

    private final AccountService accountService;
    private final AuthService authService;
    private final CacheService cacheService;
    private final DashboardService dashboardService;
    private final EmailService emailService;
    private final GeolocationService geolocationService;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final TransactionService transactionService;
    private final UserService userService;

    private final Map<String, Object> services;

    public BankingOrchestrationService(
            AccountService accountService,
            AuthService authService,
            CacheService cacheService,
            DashboardService dashboardService,
            EmailService emailService,
            GeolocationService geolocationService,
            OtpService otpService,
            TokenService tokenService,
            TransactionService transactionService,
            UserService userService) {

        this.accountService = accountService;
        this.authService = authService;
        this.cacheService = cacheService;
        this.dashboardService = dashboardService;
        this.emailService = emailService;
        this.geolocationService = geolocationService;
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.userService = userService;

        Map<String, Object> serviceMap = new LinkedHashMap<>();

        serviceMap.put("account", accountService);
        serviceMap.put("auth", authService);
        serviceMap.put("cache", cacheService);
        serviceMap.put("dashboard", dashboardService);
        serviceMap.put("email", emailService);
        serviceMap.put("geolocation", geolocationService);
        serviceMap.put("otp", otpService);
        serviceMap.put("token", tokenService);
        serviceMap.put("transaction", transactionService);
        serviceMap.put("user", userService);

        this.services = Collections.unmodifiableMap(serviceMap);
    }

    /**
     * Calls a public method on one of the registered services.
     *
     * Example:
     * invokeService("account", "getAccount",
     *               new Class<?>[]{Long.class}, 1001L);
     */
    public Object invokeService(
            String serviceName,
            String methodName,
            Class<?>[] parameterTypes,
            Object... arguments) {

        Object target = services.get(serviceName);

        if (target == null) {
            throw new IllegalArgumentException(
                    "Unknown service: " + serviceName);
        }

        try {
            Method method = target.getClass().getMethod(
                    methodName, parameterTypes);

            return method.invoke(target, arguments);

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Method not found: " + methodName
                            + " on service: " + serviceName, e);

        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot access method: " + methodName, e);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            throw new IllegalStateException(
                    "Service method failed: " + methodName, cause);
        }
    }

    public Map<String, Object> getRegisteredServices() {
        return services;
    }
}
