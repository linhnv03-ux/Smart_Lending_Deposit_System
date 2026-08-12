package com.bank.slds.adapter.constant;

public final class ApiPath {

    private ApiPath() {
        // Prevent instantiation
    }

    public static final String API_BASE = "/api/v1";

    public static final class CoreBanking {
        public static final String BASE = API_BASE + "/core-banking";

        public static final String JOURNALS_POST = "/journals/post";
        public static final String ACCOUNT_BALANCE = "/accounts/{accountNumber}/balance";
        public static final String CIRCUIT_BREAKER_STATUS = "/circuit-breaker/status";
        public static final String CIRCUIT_BREAKER_TOGGLE = "/circuit-breaker/toggle";
    }
}
