/**
 * (C) Copyright 2021 Araf Karsh Hamid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fusion.air.microservice.security;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;

/**
 * JsonWebToken Key Manager
 * Loads Secret Key, Public Keys depends upon the configuration.
 *
 * @author arafkarsh
 *
 */
@Service
public final class JsonWebTokenKeyManager {

	// Set Logger -> Lookup will automatically determine the class name.
	//  private static final Logger log = getLogger(lookup().lookupClass());

	private static String TOKEN = "<([1234567890SecretKey!!To??Encrypt##Data@12345%6790])>";

	public static final int SECRET_KEY 				= 1;
	public static final int PUBLIC_KEY				= 2;
	public static final int KEYCLOAK_PUBLIC_KEY		= 3;

	@Autowired
	private JsonWebTokenConfig jwtConfig;

	@Autowired
	private KeyCloakConfig keycloakConfig;

	@Autowired
	private CryptoKeyGenerator cryptoKeys;

	private int tokenType;

	private Key signingKey;
	private Key validatorKey;
	private Key validatorLocalKey;

	private SignatureAlgorithm algorithm;
	public final static SignatureAlgorithm defaultAlgo = SignatureAlgorithm.HS512;

	private String issuer;

	/**
	 * Initialize the JWT with the Signature Algorithm based on Secret Key or Public / Private Key
	 */
	public JsonWebTokenKeyManager() {
	}

	/**
	 * Initialize the JsonWebToken with Token Type Secret Keys and other default claims
	 * settings.
	 * @return
	 */
	public JsonWebTokenKeyManager init() {
		return init(SECRET_KEY);
	}

	/**
	 * Initialize the JsonWebToken with Token Type (Secret or Public/Private Keys) and other default claims
	 * settings.
	 * @return
	 */
	public JsonWebTokenKeyManager init(int _tokenType) {
		tokenType 			= _tokenType;
		// Set the Algo Symmetric (Secret) OR Asymmetric (Public/Private) based on the Configuration
		algorithm 			= (tokenType == SECRET_KEY) ? SignatureAlgorithm.HS512 : SignatureAlgorithm.RS256;

		System.out.println("Token Type = "+tokenType+" Algorithm = "+algorithm);
		// Create the Key based on Secret Key or Private Key
		createSigningKey();

		issuer				= (jwtConfig != null) ? jwtConfig.getServiceOrg() : "fusion-air";
		return this;
	}

	/**
	 * Create the Key based on  Secret Key or Public / Private Key
	 *
	 * @return
	 */
	private void createSigningKey() {
		switch(tokenType) {
			case SECRET_KEY:
				signingKey = new SecretKeySpec(getTokenKeyBytes(), algorithm.getJcaName());
				validatorKey = signingKey;
				validatorLocalKey = signingKey;
				break;
			case PUBLIC_KEY:
				getCryptoKeyGenerator()
				.setKeyFiles(getCryptoPublicKeyFile(), getCryptoPrivateKeyFile())
				.iFPublicPrivateKeyFileNotFound().THEN()
					.createRSAKeyFiles()
				.ELSE()
					.readRSAKeyFiles()
				.build();
				signingKey = getCryptoKeyGenerator().getPrivateKey();
				validatorKey = getCryptoKeyGenerator().getPublicKey();
				validatorLocalKey = validatorKey;
				System.out.println("Public key format: " + getCryptoKeyGenerator().getPublicKey().getFormat());
				System.out.println(getCryptoKeyGenerator().getPublicKeyPEMFormat());
				break;
		}
	}

	/**
	 * This is set when the Applications Boots Up from the Servlet Event Listener
	 * Servlet Event Listener ensures that the public key is downloaded from the KeyCloak Server
	 * Set the Validator Key as KeyCloak Public Key if the Public Key downloaded from KeyCloak.
	 */
	public void setKeyCloakPublicKey() {
		if(keycloakConfig.isKeyCloakEnabled()) {
			Path filePath = Paths.get(keycloakConfig.getKeyCloakPublicKey());
			RSAPublicKey key = null;
			String keyName = "RSA PUBLIC KEY";
			if (Files.exists(filePath)) {
				try {
					getCryptoKeyGenerator()
						.setPublicKeyFromKeyCloak(
							getCryptoKeyGenerator()
							.readPublicKey(new File(keycloakConfig.getKeyCloakPublicKey()))
						);
					issuer = keycloakConfig.getTokenIssuer();
					validatorKey = getCryptoKeyGenerator().getPublicKey();
					String pem = getCryptoKeyGenerator().convertKeyToText(getValidatorKey(), keyName);

					System.out.println("KeyCloak Public Key Set. Issuer = "+issuer);
					System.out.println(pem);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}
	}

	/**
	 * Returns Crypto Public Key File
	 * @return
	 */
	private String getCryptoPublicKeyFile() {
		return (jwtConfig != null) ? jwtConfig.getCryptoPublicKeyFile() : "publicKey.pem";
	}

	/**
	 * Returns Crypto Private Key File
	 * @return
	 */
	private String getCryptoPrivateKeyFile() {
		return (jwtConfig != null) ? jwtConfig.getCryptoPrivateKeyFile() : "privateKey.pem";
	}

	/**
	 * Returns Token Key -
	 * In SpringBooT Context from ServiceConfiguration
	 * Else from Static TOKEN Key
	 * @return
	 */
	private String getTokenKey() {
		return (jwtConfig != null) ? jwtConfig.getTokenKey() : TOKEN;
	}

	/**
	 * Returns the Token Key in Bytes
	 * @return
	 */
	private byte[] getTokenKeyBytes() {
		return HashData.base64Encoder(getTokenKey()).getBytes();
	}

	/**
	 * Returns CryptoKeyGenerator
	 * @return
	 */
	private CryptoKeyGenerator getCryptoKeyGenerator() {
		if(cryptoKeys == null) {
			cryptoKeys = new CryptoKeyGenerator();
		}
		return cryptoKeys;
	}

	/**
	 * Set the Issuer
	 * @param _issuer
	 * @return
	 */
	public JsonWebTokenKeyManager setIssuer(String _issuer) {
		issuer = _issuer;
		return this;
	}

	/**
	 * Returns the Algorithm
	 * @return
	 */
	public SignatureAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * Returns the Signing Key
	 * @return
	 */
	public Key getKey() {
		return signingKey;
	}

	/**
	 * Returns KeyCloak Validator (Public) Key
	 * @return
	 */
	public Key getValidatorKey() {
		return validatorKey;
	}

	/**
	 * Returns Validator Local Key
	 * @return
	 */
	public Key getValidatorLocalKey() {
		return validatorLocalKey;
	}
}
