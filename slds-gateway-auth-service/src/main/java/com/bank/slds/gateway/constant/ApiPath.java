package com.bank.slds.gateway.constant;

public final class ApiPath {

    private ApiPath() {}

    public static final class Auth {
        private Auth() {}
        public static final String BASE = "/api/v1/auth";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String VALIDATE = "/validate";
        public static final String REFRESH = "/refresh";
        public static final String INTROSPECT = "/introspect";
        public static final String PUBLIC_KEY = "/public-key";
        public static final String CLIENT_TOKEN = "/oauth/token";
    }
}
