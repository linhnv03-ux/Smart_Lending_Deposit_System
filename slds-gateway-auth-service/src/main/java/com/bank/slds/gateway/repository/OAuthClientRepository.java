package com.bank.slds.gateway.model; // wait, repository package

package com.bank.slds.gateway.repository;

import com.bank.slds.gateway.model.OAuthClientEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OAuthClientRepository extends R2dbcRepository<OAuthClientEntity, String> {
    Mono<OAuthClientEntity> findByClientId(String clientId);
}
