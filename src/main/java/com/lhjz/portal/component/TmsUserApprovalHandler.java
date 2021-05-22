package com.lhjz.portal.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;

import java.util.Collection;

//@Component
@Slf4j
public class TmsUserApprovalHandler extends ApprovalStoreUserApprovalHandler {

    private boolean useApprovalStore = true;

    private ClientDetailsService clientDetailsService;

    /**
     * Service to load client details (optional) for auto approval checks.
     *
     * @param clientDetailsService a client details service
     */
    @Override
    public void setClientDetailsService(
            ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
        super.setClientDetailsService(clientDetailsService);
    }

    /**
     * @param useApprovalStore the useTokenServices to set
     */
    public void setUseApprovalStore(boolean useApprovalStore) {
        this.useApprovalStore = useApprovalStore;
    }

    /**
     * Allows automatic approval for a white list of clients in the implicit
     * grant case.
     *
     * @param authorizationRequest The authorization request.
     * @param userAuthentication   the current user authentication
     * @return An updated request if it has already been approved by the current
     * user.
     */
    @Override
    public AuthorizationRequest checkForPreApproval(
            AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {

        boolean approved = false;
        // If we are allowed to check existing approvals this will short circuit
        // the decision
        if (useApprovalStore) {
            authorizationRequest = super.checkForPreApproval(
                    authorizationRequest, userAuthentication);
            approved = authorizationRequest.isApproved();
        } else {
            if (clientDetailsService != null) {
                Collection<String> requestedScopes = authorizationRequest
                        .getScope();
                try {
                    ClientDetails client = clientDetailsService
                            .loadClientByClientId(authorizationRequest
                                    .getClientId());
                    for (String scope : requestedScopes) {
                        if (client.isAutoApprove(scope)) {
                            approved = true;
                            break;
                        }
                    }
                } catch (ClientRegistrationException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        authorizationRequest.setApproved(approved);

        return authorizationRequest;

    }

}
