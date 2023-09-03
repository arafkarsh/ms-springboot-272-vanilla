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
package io.fusion.air.microservice.adapters.controllers.ratelimit;
// Custom
import io.fusion.air.microservice.adapters.service.RateLimitServiceImpl;
import io.fusion.air.microservice.domain.entities.order.CartEntity;
import io.fusion.air.microservice.domain.exceptions.LimitExceededException;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.domain.ports.services.CartService;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
// Java
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
// SLF4J
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
// Bucket4J
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Cart Controller for the Service
 * Rate Limit Examples
 *
 * @author arafkarsh
 * @version 1.0
 * 
 */
@CrossOrigin
@Configuration
@RestController
// "/ms-cache/api/v1"
@RequestMapping("${service.api.path}/cart/rate/limit")
@RequestScope
@Tag(name = "Cart API - Rate Limit", description = "Rate Limit Examples")
public class RateLimitControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;
	private String serviceName;

	@Autowired
	private CartService cartService;

	@Autowired
	private RateLimitServiceImpl rateLimitService;

	/**
	 * GET Method Call to ALL CARTS
	 *
	 * @return
	 */
	@Operation(summary = "Get The Carts with Rate Limit")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Cart ID",
					content = @Content)
	})
	@GetMapping("/all")
	@ResponseBody
	public ResponseEntity<StandardResponse> fetchCarts() throws Exception {
		log.info("|"+name()+"|Request to Get Cart For the Customers using RATE-LIMIT");
		if(rateLimitService.tryConsume()) {
			List<CartEntity> cart = cartService.findAll();
			StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  " + cart.size());
			stdResponse.setPayload(cart);
			return ResponseEntity.ok(stdResponse);
		} else {
			throw new LimitExceededException("For Fetch Carts API!");
		}
	}

	@Operation(summary = "Test the Rate Limit")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Rate Limit Tested Successfully",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Test the Rate Limit",
					content = @Content)
	})
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<StandardResponse> rateLimitTest(@RequestParam("customerId") String customerId) {
		log.info("|"+name()+"|Request to Test the rate limit ");
		List<CartEntity> cart = cartService.findByCustomerId(customerId);
		StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  "+cart.size());
		stdResponse.setPayload(cart);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * GET Method Call to Get Cart for the Customer for the Price Greater Than
	 *
	 * @param customerId
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "Get The Cart For Items Price Greater Than")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Cart ID",
					content = @Content)
	})
	@GetMapping("/customer/{customerId}/price/{price}")
	@ResponseBody
	public ResponseEntity<StandardResponse> fetchCartForItems(@PathVariable("customerId") String customerId,
															  @PathVariable("price") BigDecimal price) throws Exception {
		log.debug("|"+name()+"|Request to Get Cart For the Customer "+customerId);
		List<CartEntity> cart = cartService.fetchProductsByPriceGreaterThan(customerId, price);
		StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  "+cart.size());
		stdResponse.setPayload(cart);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * De-Activate the Cart Item
	 */
	@Operation(summary = "De-Activate Cart Item")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Item De-Activated",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to De-Activate the Cart item",
					content = @Content)
	})
	@PutMapping("/deactivate/customer/{customerId}/cartitem/{cartid}")
	public ResponseEntity<StandardResponse> deActivateCartItem(@PathVariable("customerId") String customerId,
									@PathVariable("cartid") UUID _cartid) {
		log.debug("|"+name()+"|Request to De-Activate the Cart item... "+_cartid);
		CartEntity product = cartService.deActivateCartItem(customerId, _cartid);
		StandardResponse stdResponse = createSuccessResponse("Cart Item De-Activated");
		stdResponse.setPayload(product);
		return ResponseEntity.ok(stdResponse);
	}


	/**
	 * Delete the Cart Item
	 */
	@Operation(summary = "Delete Cart Item")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Item Deleted",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Delete the Cart item",
					content = @Content)
	})
	@DeleteMapping("/delete/customer/{customerId}/cartitem/{cartid}")
	public ResponseEntity<StandardResponse> deleteCartItem(@PathVariable("customerId") String customerId,
															 @PathVariable("cartid") UUID _cartid) {
		log.debug("|"+name()+"|Request to Delete the Cart item... "+_cartid);
		cartService.deleteCartItem(customerId, _cartid);
		StandardResponse stdResponse = createSuccessResponse("Cart Item Deleted");
		return ResponseEntity.ok(stdResponse);
	}

 }