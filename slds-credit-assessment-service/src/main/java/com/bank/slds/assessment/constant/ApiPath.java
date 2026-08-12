package com.bank.slds.assessment.constant;

public final class ApiPath {

    private ApiPath() {
        // Prevent instantiation
    }

    public static final String API_BASE = "/api/v1";

    public static final class CreditAssessment {
        public static final String BASE = API_BASE + "/assessment";

        public static final String EVALUATE = "/evaluate";
        public static final String CIC_HISTORY = "/cic/{applicantCip}";
        public static final String LOGS = "/logs";
    }
}
