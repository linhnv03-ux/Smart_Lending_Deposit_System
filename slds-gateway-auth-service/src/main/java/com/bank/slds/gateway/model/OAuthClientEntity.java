package com.bank.slds.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("OAUTH_CLIENT_DETAILS")
public class OAuthClientEntity {

    @Id
    @Column("CLIENT_ID")
    private String clientId;

    @Column("RESOURCE_IDS")
    private String resourceIds;

    @Column("CLIENT_SECRET")
    private String clientSecret;

    @Column("SCOPE")
    private String scope;

    @Column("AUTHORIZED_GRANT_TYPES")
    private String authorizedGrantTypes;

    @Column("WEB_SERVER_REDIRECT_URI")
    private String webServerRedirectUri;

    @Column("AUTHORITIES")
    private String authorities;

    @Column("ACCESS_TOKEN_VALIDITY")
    private Integer accessTokenValidity;

    @Column("REFRESH_TOKEN_VALIDITY")
    private Integer refreshTokenValidity;

    @Column("ADDITIONAL_INFORMATION")
    private String additionalInformation;

    @Column("AUTOAPPROVE")
    private String autoapprove;
}
