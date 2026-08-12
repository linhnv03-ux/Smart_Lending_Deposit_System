package com.bank.slds.loan.constant;

public final class ApiPath {

    private ApiPath() {
        // Prevent instantiation
    }

    public static final String API_BASE = "/api/v1";

    public static final class Loans {
        public static final String BASE = API_BASE + "/loans";
        
        public static final String APPLICATIONS = "/applications";
        public static final String APPLICATION_BY_NO = "/applications/{applicationNo}";
        
        public static final String DISBURSE = "/disburse";
        public static final String REPAY = "/repay";
        public static final String SCHEDULES_PREVIEW = "/schedules/preview";
        public static final String CIRCUIT_BREAKER_STATUS = "/circuit-breaker/status";
        public static final String CIRCUIT_BREAKER_TOGGLE = "/circuit-breaker/toggle";
        public static final String AUDIT_LOGS = "/audit-logs";
    }
}
