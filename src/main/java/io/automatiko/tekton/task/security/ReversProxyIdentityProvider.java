package io.automatiko.tekton.task.security;

import java.security.Principal;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@Singleton
public class ReversProxyIdentityProvider implements IdentityProvider<ReverseProxyAuthenticationRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReversProxyIdentityProvider.class);

    @Override
    public Class<ReverseProxyAuthenticationRequest> getRequestType() {
        return ReverseProxyAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(ReverseProxyAuthenticationRequest request, AuthenticationRequestContext context) {

        String user = request.getAttribute(ReverseProxyAuthenticationRequest.USER);
        if (user != null && !user.isEmpty()) {
            LOGGER.debug("Request contains user information as part of 'X-Forwarded-User' header with value {}", user);
            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();

            builder.setPrincipal(new Principal() {

                @Override
                public String getName() {
                    return user;
                }
            });

            String groups = request.getAttribute(ReverseProxyAuthenticationRequest.GROUPS);

            if (groups != null) {
                LOGGER.debug("Request contains group information as part of 'X-Forwarded-Groups' header with value {}", groups);
                for (String group : groups.split(",")) {
                    builder.addRole(group.trim());
                }
            }

            String email = request.getAttribute(ReverseProxyAuthenticationRequest.EMAIL);
            if (email != null) {
                LOGGER.debug("Request contains email information as part of 'X-Forwarded-Email' header with value {}", groups);
                builder.addAttribute(ReverseProxyAuthenticationRequest.EMAIL, email);
            }

            return Uni.createFrom().item(builder.build());
        }
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder().setAnonymous(true).build());
    }

}
