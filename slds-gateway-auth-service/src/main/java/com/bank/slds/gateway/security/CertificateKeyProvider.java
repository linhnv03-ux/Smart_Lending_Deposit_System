package com.bank.slds.gateway.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * KeyStore / Certificate Helper for RSA JWT signing and verification using
 * certificate/jwt.p12, certificate/cert.pem, and certificate/pubkey.txt
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
            // 1. Load KeyStore jwt.p12
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

            // 2. Load cert.pem if certificate not set
            if (this.certificate == null) {
                ClassPathResource certResource = new ClassPathResource("certificate/cert.pem");
                if (certResource.exists()) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    try (InputStream is = certResource.getInputStream()) {
                        this.certificate = (X509Certificate) cf.generateCertificate(is);
                        if (this.certificate != null && this.publicKey == null) {
                            this.publicKey = this.certificate.getPublicKey();
                        }
                    }
                }
            }

            // 3. Direct load from pubkey.txt if publicKey still null
            if (this.publicKey == null) {
                ClassPathResource pubResource = new ClassPathResource("certificate/pubkey.txt");
                if (pubResource.exists()) {
                    try (InputStream is = pubResource.getInputStream()) {
                        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        String publicKeyPEM = pem
                            .replace("-----BEGIN PUBLIC KEY-----", "")
                            .replaceAll("\r", "")
                            .replaceAll("\n", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .trim();
                        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
                        this.publicKey = keyFactory.generatePublic(keySpec);
                        log.info("Successfully loaded Public Key directly from pubkey.txt");
                    }
                }
            }

            log.info("CertificateKeyProvider successfully initialized RSA keys/certificates (PrivateKey: {}, PublicKey: {})",
                    this.privateKey != null, this.publicKey != null);
        } catch (Exception e) {
            log.warn("CertificateKeyProvider initialized with symmetric HMAC fallback due to: {}", e.getMessage());
        }
    }
}
