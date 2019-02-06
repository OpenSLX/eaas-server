package de.bwl.bwfla.emil.filters;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.Secured;
import org.apache.tamaya.inject.api.Config;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    @Config(value = "emil.authEnabled")
    private boolean authEnabled;

    @Inject
    @Config(value = "emil.authSecret")
    private String authSecret;

    @Inject
    @AuthenticatedUser
    Event<DecodedJWT> userAuthenticatedEvent;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if(!authEnabled)
            return;
        // Get the HTTP Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        String token = null;

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring("Bearer".length()).trim();
        }
        else
        {
            MultivaluedMap<String, String> map = requestContext.getUriInfo().getQueryParameters();
            token = map.getFirst("access_token");
        }
        // String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";

        if(token == null)
            throw new NotAuthorizedException("Authorization token must be provided");

        try {
            // Validate the token
            validateToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private void validateToken(String token) throws Exception {

        if(authSecret == null)
            throw new BWFLAException("no auth secret configured. configure 'emil.authSecret'");

        try {
            Algorithm algorithm = Algorithm.HMAC256(authSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            userAuthenticatedEvent.fire(jwt);
        } catch (JWTVerificationException exception){
            throw exception;
        }
    }
}