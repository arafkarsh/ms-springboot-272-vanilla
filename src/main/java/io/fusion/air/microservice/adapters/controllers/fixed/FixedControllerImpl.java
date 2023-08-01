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
package io.fusion.air.microservice.adapters.controllers.fixed;

import io.fusion.air.microservice.domain.entities.order.ProductEntity;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.domain.ports.services.CountryService;
import io.fusion.air.microservice.domain.ports.services.ProductService;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
import io.fusion.air.microservice.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.HtmlUtils;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Country Controller for the Service
 *
 * @author arafkarsh
 * @version 1.0
 * 
 */
@CrossOrigin
@Configuration
@RestController
// "/ms-cache/api/v1"
@RequestMapping("${service.api.path}/security/fixed")
@RequestScope
@Tag(name = "Security Fixed API", description = "To Manage (Add/Update/Delete/Search) Country.")
public class FixedControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;
	private String serviceName;

	@Autowired
	private CountryService countryService;

	@Autowired
	private ProductService productServiceImpl;


	/**
	 * Cross-Site Scripting (XSS) attacks occur
	 * - when an attacker uses a web application to send the malicious script,
	 * - Generally in the form of a browser-side script to a different end user.
	 * - The end user’s browser cannot know that the script should not be trusted
	 * - and will execute the script. XSS attacks can lead to a variety of problems,
	 * - including stolen session tokens or login credentials, defacement of websites, or malicious redirection.
	 *
	 * Specification Pattern Example
	 * Search the Product by Product Name & Location and Price Greater Than
	 */
	@Operation(summary = "Search Product By Product Name & Location and Price Greater Than")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Product(s) Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find the Product(s)!",
					content = @Content)
	})
	@GetMapping("/search/product/{productName}/location/{location}/price/{productPrice}")
	public ResponseEntity<StandardResponse> findProductsAndPriceGreaterThan(
			@PathVariable("productName") String _productName,
			@PathVariable("location") String _location,
			@PathVariable("productPrice") BigDecimal _productPrice) {

		// The Inputs are NOT Sanitized and hence the Search is Vulnerable to XSS Attack.
		String productName = HtmlUtils.htmlEscape(_productName);
		String location = HtmlUtils.htmlEscape(_location);

		log.debug("|"+name()+"|Request to Search the Product By Name ... "+productName);
		List<ProductEntity> products = productServiceImpl.findProductsAndPriceGreaterThan(
				productName, location, _productPrice);

		StandardResponse stdResponse = createSuccessResponse("Products Found ("+products.size()
				+") For Search Keys: Phone = " +productName + " Price = "+_productPrice + " Location = "+location);
		stdResponse.setPayload(products);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Update the Product Details
	 * This API Can be tested for Optimistic Lock Exceptions as the Entity is a Versioned Entity
	 */
	@Operation(summary = "Update the Product")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Product Updated",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Update the Product",
					content = @Content)
	})
	@PutMapping("/product/update/")
	public ResponseEntity<StandardResponse> updateProduct(@Valid @RequestBody ProductEntity _product) {
		// The Inputs are NOT Sanitized and hence the Search is Vulnerable to XSS Attack.

		System.out.println("\n<><> BEFORE Sanitization:"+ Utils.toJsonString(_product));

		_product.setProductName(HtmlUtils.htmlEscape(_product.getProductName()));
		_product.setProductDetails(HtmlUtils.htmlEscape(_product.getProductDetails()));

		System.out.println("<><> Product Name = "+HtmlUtils.htmlEscape(_product.getProductName()));

		System.out.println("<><> AFTER Sanitization:"+Utils.toJsonString(_product)+"\n");

		log.debug("|"+name()+"|Request to Update Product Details... "+_product);
		ProductEntity prodEntity = productServiceImpl.updateProduct(_product);
		StandardResponse stdResponse = createSuccessResponse("Product Updated!");
		stdResponse.setPayload(prodEntity);
		return ResponseEntity.ok(stdResponse);
	}

 }