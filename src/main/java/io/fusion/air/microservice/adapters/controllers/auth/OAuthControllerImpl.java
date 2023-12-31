/**
 * (C) Copyright 2023 Araf Karsh Hamid
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
package io.fusion.air.microservice.adapters.controllers.auth;
// Custom
import com.fasterxml.jackson.databind.JsonNode;
import io.fusion.air.microservice.adapters.security.*;
import io.fusion.air.microservice.domain.exceptions.DataNotFoundException;
import io.fusion.air.microservice.domain.exceptions.SecurityException;
import io.fusion.air.microservice.domain.models.auth.Token;
import io.fusion.air.microservice.domain.models.auth.UserCredentials;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.security.SecureData;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
// Swagger Open API 3.0
import io.fusion.air.microservice.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
// Java
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
// SLF4J
import org.slf4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * KeyCloak OAuth Controller
 *
 * @author arafkarsh
 * @version 1.0
 */
@CrossOrigin
@Configuration
@RestController
// "/ms-cache/api/v1"
@RequestMapping("${service.api.path}/auth")
@RequestScope
@Tag(name = "Authentication", description = "KeyCloak, OAuth2 Security Authentication, JWT Access, Refresh & Tx Tokens, Remember Me Functionality")
public class OAuthControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;

	@Autowired
	private AuthKeyCloakService keyCloakService;

	@Autowired
	private AuthLocalService authLocalService;

	@Autowired
	private TokenManager tokenManager;

	@Operation(summary = "Authenticate User Locally")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Authenticated Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Authenticate User",
					content = @Content)
	})
	@PostMapping("/login")
	public ResponseEntity<StandardResponse> authenticateUser(@Valid @RequestBody UserCredentials _user) {
		log.info("|"+name()+"|Request to Authenticate User Locally "+_user.getUserId());

		// Validate the User ID & Password with DB
		// .....
		// If Validation is Successful then Create the Tokens (Auth & Refresh Tokens)
		HttpHeaders headers = new HttpHeaders();
		HashMap<String, String> tokens = tokenManager.createAuthorizationToken(_user.getUserId(), headers);
		// TX - Token
		String txToken = tokenManager.createTXToken(_user.getUserId());
		tokens.put("TX-Token", txToken);
		headers.add("TX-Token", txToken);

		StandardResponse stdResponse = createSuccessResponse("User Authenticated (Locally) Successfully");
		stdResponse.setPayload(tokens);

		System.out.println("Pass 1: Refresh Token : "+tokens.get("refresh_token"));
		// Store the Token in Cookies (Encrypted) for "Remember Me" functionality
		Utils.createSecureCookieHeaders(headers, "RSH", SecureData.encrypt(tokens.get("refresh_token")));
		Utils.createSecureCookieHeaders(headers, "TSH", SecureData.encrypt(txToken));

		// Return the response entity with the custom headers and body
		return new ResponseEntity<StandardResponse>(stdResponse, headers, HttpStatus.OK);
	}

	/**
	 * Authenticate User
	 */
	@Operation(summary = "Authenticate User using KeyCloak")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Authenticated Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Authenticate User",
					content = @Content)
	})
	@PostMapping("/keycloak/login")
	public ResponseEntity<StandardResponse> authenticateUserUsingKeyCloak(@Valid @RequestBody UserCredentials _user) {
		log.info("|"+name()+"|Request to Authenticate User using KeyCloak"+_user.getUserId());
		Token token = keyCloakService.authenticateUser(_user.getUserId(), _user.getPassword());
		StandardResponse stdResponse = createSuccessResponse("User Authenticated Successfully");
		stdResponse.setPayload(token);

		String txToken = tokenManager.createTXToken(_user.getUserId());

		// Add Tokens (from KeyCloak) to Headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer "+token.getAccessToken());
		headers.add("Refresh-Token", token.getRefreshToken());
		headers.add("TX-Token", txToken);

		// Create the TX Token (As part of User Service) and set it in the Header

		// Store the Token in Cookies (Encrypted) for "Remember Me" functionality
		// Utils.createSecureCookieHeaders(headers, "ABC", SecureData.encrypt(token.getAccessToken()));
		Utils.createSecureCookieHeaders(headers, "RSH", SecureData.encrypt(token.getRefreshToken()));
		Utils.createSecureCookieHeaders(headers, "TSH", SecureData.encrypt(txToken));

		// Return the response entity with the custom headers and body
		return new ResponseEntity<StandardResponse>(stdResponse, headers, HttpStatus.OK);
	}

	@Operation(summary = "Authenticate User - Remember Me")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Remembered Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Remember User",
					content = @Content)
	})
	@GetMapping("/keycloak/login/remember/me")
	public ResponseEntity<StandardResponse> authenticateRememberMe(HttpServletRequest request) {
		log.info("|"+name()+"|Request to Authenticate User based on Reemember Me");
		StandardResponse stdResponse = createSuccessResponse("User Remembered Successfully");
		try {
			HashMap cookies = Utils.getCookieMap(request);
			String refreshToken = (String) cookies.get("RSH");
			String txToken = (String) cookies.get("TSH");

			// Refresh The token using KeyCloak Service
			Token token = keyCloakService.refreshToken(SecureData.decrypt(refreshToken));

			// Add Tokens (from KeyCloak) to Headers
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer "+token.getAccessToken());
			headers.add("Refresh-Token", token.getRefreshToken());
			headers.add("TX-Token", SecureData.decrypt(txToken));

			// Store the Token in Cookies (Encrypted) for "Remember Me" functionality
			// Utils.createSecureCookieHeaders(headers, "ABC", SecureData.encrypt(token.getAccessToken()));
			Utils.createSecureCookieHeaders(headers, "RSH", SecureData.encrypt(token.getRefreshToken()));
			Utils.createSecureCookieHeaders(headers, "TSH", SecureData.encrypt(txToken));

			// Return the response entity with the custom headers and body
			return new ResponseEntity<StandardResponse>(stdResponse, headers, HttpStatus.OK);
		} catch(ClassCastException e) {
			throw new SecurityException("Invalid Cookie Data!");
		} catch(NullPointerException e) {
			throw new DataNotFoundException("Cookie Not Found!");
		} catch(Exception e) {
			throw new SecurityException("Unable to Remember the User! Please Re-Login Again with credentials.");
		}
	}

	@Operation(summary = "Refresh Token when Auth Token expires")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Token Refreshed Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Refresh Token",
					content = @Content)
	})
	@GetMapping("/keycloak/refresh/token")
	public ResponseEntity<StandardResponse> refreshToken(HttpServletRequest request) {
		log.info("|"+name()+"|Request to Authenticate User based on Reemember Me");
		StandardResponse stdResponse = createSuccessResponse("User Remembered Successfully");
		try {
			String refreshToken = request.getHeader("Refresh-Token");
			// Refresh The token
			Token token = keyCloakService.refreshToken(refreshToken);

			// Add Tokens (from KeyCloak) to Headers
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer "+token.getAccessToken());
			headers.add("Refresh-Token", token.getRefreshToken());
			// headers.add("TX-Token", SecureData.decrypt(txToken));

			Utils.createSecureCookieHeaders(headers, "RSH", SecureData.encrypt(token.getRefreshToken()));
			// Utils.createSecureCookieHeaders(headers, "TSH", SecureData.encrypt(txToken));

			// Return the response entity with the custom headers and body
			return new ResponseEntity<StandardResponse>(stdResponse, headers, HttpStatus.OK);
		} catch(ClassCastException e) {
			throw new SecurityException("Invalid Cookie Data");
		} catch(Exception e) {
			throw new SecurityException("Unable to Refresh Token! Please Re-Login Again with credentials.");
		}
	}

	/**
	 * Test if the User is Authorized
	 */
	// @SingleTokenAuthorizationRequired(role="user")
	@AuthorizationRequired(role = "user")
	@Operation(summary = "Test the KeyCloak Token Validation using Public Key", security = { @SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Token Tested Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Test the token",
					content = @Content)
	})
	@GetMapping("/keycloak/token/test")
	public ResponseEntity<StandardResponse> testToken() {
		log.info("|"+name()+"|Request to Test the KeyCloak Token ");
		StandardResponse stdResponse = createSuccessResponse("Token Tested Successfully");
		stdResponse.setPayload("Token Tested Successfully");
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Logout User
	 * @return
	 */
	@Operation(summary = "Logout User")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Logout Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Logout User",
					content = @Content)
	})
	@PostMapping("/keycloak/logout")
	public ResponseEntity<StandardResponse> logoutUser() {
		log.info("|"+name()+"|Request to Logout User ");
		StandardResponse stdResponse = createSuccessResponse("User Logged Successfully");
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Get the Public Key from KeyCloak to verify the JWT Token
	 * @return
	 */
	@Operation(summary = "Get the Public Key from KeyCloak")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Public Key Retrieved Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Retrieve User Public Key",
					content = @Content)
	})
	@GetMapping("/keycloak/public/key")
	public ResponseEntity<StandardResponse> getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		log.info("|"+name()+"|Request to Get the Public Key ");
		JsonNode publicKey = keyCloakService.getPublicKeyFromKeycloak();
		StandardResponse stdResponse = createSuccessResponse("Retrieved Public Key Successfully");
		stdResponse.setPayload(publicKey);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Get the Public Key (PEM) from KeyCloak to verify the JWT Token
	 * @return
	 */
	@Operation(summary = "Get the Public Key from KeyCloak in PEM Format")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Public Key Retrieved Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Retrieve User Public Key",
					content = @Content)
	})
	@GetMapping("/keycloak/public/key/pem")
	public ResponseEntity<StandardResponse> getPublicKeyPEM() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		log.info("|"+name()+"|Request to Get the Public Key ");
		String publicKey = keyCloakService.getPublicKeyPEMFormat();
		StandardResponse stdResponse = createSuccessResponse("Retrieved Public Key Successfully");
		stdResponse.setPayload(publicKey);
		return ResponseEntity.ok(stdResponse);
	}

 }