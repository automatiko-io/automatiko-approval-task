package io.automatiko.tekton.task.security;

import java.security.Principal;

import javax.inject.Singleton;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@Singleton
public class ReversProxyIdentityProvider implements IdentityProvider<ReverseProxyAuthenticationRequest> {

    @Override
    public Class<ReverseProxyAuthenticationRequest> getRequestType() {
        return ReverseProxyAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(ReverseProxyAuthenticationRequest request, AuthenticationRequestContext context) {

        String user = request.getAttribute(ReverseProxyAuthenticationRequest.USER);
        if (user != null && !user.isEmpty()) {
            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();

            builder.setPrincipal(new Principal() {

                @Override
                public String getName() {
                    return user;
                }
            });

            String groups = request.getAttribute(ReverseProxyAuthenticationRequest.GROUPS);

            if (groups != null) {
                for (String group : groups.split(",")) {
                    builder.addRole(group.trim());
                }
            }

            String email = request.getAttribute(ReverseProxyAuthenticationRequest.EMAIL);
            if (email != null) {
                builder.addAttribute(ReverseProxyAuthenticationRequest.EMAIL, email);
            }

            return Uni.createFrom().item(builder.build());
        }

        return Uni.createFrom().failure(new AuthenticationFailedException());
    }

}
