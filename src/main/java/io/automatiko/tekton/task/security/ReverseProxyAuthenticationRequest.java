package io.automatiko.tekton.task.security;

import io.quarkus.security.identity.request.BaseAuthenticationRequest;

public class ReverseProxyAuthenticationRequest extends BaseAuthenticationRequest {

    public static final String USER = "user";
    public static final String GROUPS = "groups";
    public static String EMAIL = "email";

}
