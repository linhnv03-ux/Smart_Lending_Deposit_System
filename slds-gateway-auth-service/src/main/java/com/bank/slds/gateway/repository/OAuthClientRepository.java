package com.bank.slds.gateway.repository;

import com.bank.slds.gateway.model.OAuthClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClientEntity, String> {
    Optional<OAuthClientEntity> findByClientId(String clientId);
}
