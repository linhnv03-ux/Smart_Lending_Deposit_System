package com.bank.slds.gateway.constant;

public final class ApiPath {

    private ApiPath() {
        // Prevent instantiation
    }

    public static final String API_BASE = "/api/v1";

    public static final class Auth {
        public static final String BASE = API_BASE + "/auth";
        public static final String LOGIN = "/login";
        public static final String VALIDATE = "/validate";
    }

    public static final class GatewayRoutes {
        public static final String LOAN_PATH_PREDICATE = API_BASE + "/loans/**";
        public static final String DEPOSIT_PATH_PREDICATE = API_BASE + "/deposits/**";
        public static final String ASSESSMENT_PATH_PREDICATE = API_BASE + "/assessment/**";
        public static final String CORE_BANKING_PATH_PREDICATE = API_BASE + "/core-banking/**";
    }
}
