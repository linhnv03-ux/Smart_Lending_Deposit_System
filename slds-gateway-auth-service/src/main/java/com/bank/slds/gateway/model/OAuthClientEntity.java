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
@Table("oauth_client_details")
public class OAuthClientEntity {

    @Id
    @Column("client_id")
    private String clientId;

    @Column("resource_ids")
    private String resourceIds;

    @Column("client_secret")
    private String clientSecret;

    private String scope;

    @Column("authorized_grant_types")
    private String authorizedGrantTypes;

    @Column("web_server_redirect_uri")
    private String webServerRedirectUri;

    private String authorities;

    @Column("access_token_validity")
    private Integer accessTokenValidity;

    @Column("refresh_token_validity")
    private Integer refreshTokenValidity;

    @Column("additional_information")
    private String additionalInformation;

    private String autoapprove;
}
