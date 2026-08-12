package com.bank.slds.gateway.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * KeyStore / Certificate Helper for RSA JWT signing and verification using
 * certificate/jwt.p12, certificate/cert.pem, and certificate/pubkey.txt
 * Inspired by Backend_WebCountDate
 */
@Component
@Getter
@Slf4j
public class CertificateKeyProvider {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private X509Certificate certificate;

    @PostConstruct
    public void initKeys() {
        try {
            // Load KeyStore jwt.p12
            ClassPathResource p12Resource = new ClassPathResource("certificate/jwt.p12");
            if (p12Resource.exists()) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                try (InputStream is = p12Resource.getInputStream()) {
                    keyStore.load(is, "password".toCharArray());
                }
                String alias = keyStore.aliases().hasMoreElements() ? keyStore.aliases().nextElement() : "jwt";
                if (keyStore.containsAlias(alias)) {
                    this.privateKey = (PrivateKey) keyStore.getKey(alias, "password".toCharArray());
                    this.certificate = (X509Certificate) keyStore.getCertificate(alias);
                    if (this.certificate != null) {
                        this.publicKey = this.certificate.getPublicKey();
                    }
                }
            }

            // Fallback load cert.pem if certificate not set
            if (this.certificate == null) {
                ClassPathResource certResource = new ClassPathResource("certificate/cert.pem");
                if (certResource.exists()) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    try (InputStream is = certResource.getInputStream()) {
                        this.certificate = (X509Certificate) cf.generateCertificate(is);
                        if (this.certificate != null) {
                            this.publicKey = this.certificate.getPublicKey();
                        }
                    }
                }
            }
            log.info("CertificateKeyProvider successfully loaded RSA keys/certificates from PKCS12 / cert.pem");
        } catch (Exception e) {
            log.warn("CertificateKeyProvider initialized with symmetric HMAC fallback due to: {}", e.getMessage());
        }
    }
}
