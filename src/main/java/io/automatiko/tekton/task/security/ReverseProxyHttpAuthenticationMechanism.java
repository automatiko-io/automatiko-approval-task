package io.automatiko.tekton.task.security;

import java.util.Collections;
import java.util.Set;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Priority(1)
@ApplicationScoped
public class ReverseProxyHttpAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final String X_FORWARDED_USER_HEADER = "X-Forwarded-User";
    private static final String X_FORWARDED_GROUPS_HEADER = "X-Forwarded-Groups";
    private static final String X_FORWARDED_EMAIL_HEADER = "X-Forwarded-Email";

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {

        ReverseProxyAuthenticationRequest request = new ReverseProxyAuthenticationRequest();
        request.setAttribute(ReverseProxyAuthenticationRequest.USER, context.request().headers().get(X_FORWARDED_USER_HEADER));
        request.setAttribute(ReverseProxyAuthenticationRequest.GROUPS,
                context.request().headers().get(X_FORWARDED_GROUPS_HEADER));
        request.setAttribute(ReverseProxyAuthenticationRequest.EMAIL,
                context.request().headers().get(X_FORWARDED_EMAIL_HEADER));
        return identityProviderManager.authenticate(request);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData challengeData = new ChallengeData(HttpResponseStatus.FORBIDDEN.code(), null, null);
        return Uni.createFrom().item(challengeData);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(ReverseProxyAuthenticationRequest.class);
    }

    @Override
    public HttpCredentialTransport getCredentialTransport() {
        return new HttpCredentialTransport(HttpCredentialTransport.Type.OTHER_HEADER, X_FORWARDED_USER_HEADER);
    }

}
