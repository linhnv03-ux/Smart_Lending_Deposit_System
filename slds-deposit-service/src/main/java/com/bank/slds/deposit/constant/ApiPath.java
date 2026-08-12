package com.bank.slds.deposit.constant;

public final class ApiPath {

    private ApiPath() {
        // Prevent instantiation
    }

    public static final String API_BASE = "/api/v1";

    public static final class Deposits {
        public static final String BASE = API_BASE + "/deposits";

        public static final String PRODUCTS = "/products";
        public static final String CALCULATE_INTEREST = "/schedules/calculate-interest";
        public static final String OPEN_ACCOUNT = "/accounts/open";
        public static final String CLOSE_ACCOUNT = "/accounts/close";
        public static final String CUSTOMER_ACCOUNTS = "/accounts/customer/{customerId}";
    }
}
