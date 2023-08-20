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
import io.fusion.air.microservice.adapters.security.AuthorizationRequired;
import io.fusion.air.microservice.adapters.security.KeyCloakService;
import io.fusion.air.microservice.adapters.security.SingleTokenAuthorizationRequired;
import io.fusion.air.microservice.adapters.security.TokenManager;
import io.fusion.air.microservice.domain.models.auth.Token;
import io.fusion.air.microservice.domain.models.auth.UserCredentials;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
// Swagger Open API 3.0
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
import javax.validation.Valid;
// SLF4J
import org.slf4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
@Tag(name = "Authentication", description = "KeyCloak, OAuth2 Security Authentication, JWT Access, Refresh & Tx Tokens")
public class OAuthControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;

	@Autowired
	private KeyCloakService keyCloakService;

	@Autowired
	private TokenManager tokenManager;


	/**
	 * Authenticate User
	 */
	@Operation(summary = "Authenticate User")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "User Authenticated Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Authenticate User",
					content = @Content)
	})
	@PostMapping("/keycloak/login")
	public ResponseEntity<StandardResponse> authenticateUser(@Valid @RequestBody UserCredentials _user) {
		log.debug("|"+name()+"|Request to Authenticate User "+_user.getUserId());
		Token token = keyCloakService.authenticateUser(_user.getUserId(), _user.getPassword());
		StandardResponse stdResponse = createSuccessResponse("User Authenticated Successfully");
		stdResponse.setPayload(token);
		// Add Tokens (from KeyCloak) to Headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer "+token.getAccessToken());
		headers.add("Refresh-Token", "Bearer "+token.getRefreshToken());

		// Create the TX Token (As part of User Service) and set it in the Header
		tokenManager.createTXToken(_user.getUserId(), headers);

		// Return the response entity with the custom headers and body
		return new ResponseEntity<StandardResponse>(stdResponse, headers, HttpStatus.OK);	}

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
		log.debug("|"+name()+"|Request to Logout User ");
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
		log.debug("|"+name()+"|Request to Get the Public Key ");
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
		log.debug("|"+name()+"|Request to Get the Public Key ");
		String publicKey = keyCloakService.getPublicKeyPEMFormat();
		StandardResponse stdResponse = createSuccessResponse("Retrieved Public Key Successfully");
		stdResponse.setPayload(publicKey);
		return ResponseEntity.ok(stdResponse);
	}

	@SingleTokenAuthorizationRequired(role="user")
	// @AuthorizationRequired(role = "user")
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
	public ResponseEntity<StandardResponse> testToken() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		log.debug("|"+name()+"|Request to Test the KeyCloak Token ");
		StandardResponse stdResponse = createSuccessResponse("Token Tested Successfully");
		stdResponse.setPayload("Token Tested Successfully");
		return ResponseEntity.ok(stdResponse);
	}
 }