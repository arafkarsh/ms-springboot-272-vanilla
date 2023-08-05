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
// Custom
import io.fusion.air.microservice.adapters.security.SingleTokenAuthorizationRequired;
import io.fusion.air.microservice.domain.entities.order.CartEntity;
import io.fusion.air.microservice.domain.entities.order.ProductEntity;
import io.fusion.air.microservice.domain.exceptions.DataNotFoundException;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.domain.models.order.Cart;
import io.fusion.air.microservice.domain.ports.services.CartService;
import io.fusion.air.microservice.domain.ports.services.ProductService;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
import io.fusion.air.microservice.utils.Utils;
// Swagger Annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// Spring Annotations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.HtmlUtils;
// Java
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
// SLF4J
import org.slf4j.Logger;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Code with Vulnerabilities Fixed
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
@Tag(name = "Security Vulnerability Fixed", description = "XSS, Input Validation, Buffer Overflow, SQL Injection, CSRF, Shell Injection, Directory Traversal, Http Response Splitting etc.")
public class FixedControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;
	private String serviceName;

	@Autowired
	private ProductService productServiceImpl;

	@Autowired
	private CartService cartService;

	@Autowired
	private EntityManager entityManager;

	/**
	 * Cross-Site Scripting (XSS) Vulnerability
	 * Cross-Site Scripting (XSS) attacks occur
	 * - when an attacker uses a web application to send the malicious script,
	 * - Generally in the form of a browser-side script to a different end user.
	 * - The end user’s browser cannot know that the script should not be trusted
	 * - and will execute the script. XSS attacks can lead to a variety of problems,
	 * - including stolen session tokens or login credentials, defacement of websites, or malicious redirection.
	 *
	 * Exploit
	 * {
	 *   "version": 0,
	 *   "uuid": "54f841fc-edb2-4095-acf2-0e6b84954787",
	 *   "productName": "<script>alert('Hello World!');</script>",
	 *   "productDetails": "<script>document.write('Hello World!');</script>",
	 *   "productPrice": 0,
	 *   "productLocationZipCode": "35266",
	 *   "active": true
	 * }
	 *
	 * Update the Product Details
	 * This API Can be tested for Optimistic Lock Exceptions as the Entity is a Versioned Entity
	 */
	@Operation(summary = "Cross-Site Scripting (XSS) Vulnerability: Update the Product")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Product Updated",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Update the Product",
					content = @Content)
	})
	@PutMapping("/xss/product/update/")
	public ResponseEntity<StandardResponse> updateProduct(@Valid @RequestBody ProductEntity _product) {
		// The Inputs are NOT Sanitized and hence the Search is Vulnerable to XSS Attack.

		System.out.println("\n<><> BEFORE Sanitization:"+ Utils.toJsonString(_product));

		_product.setProductName(HtmlUtils.htmlEscape(_product.getProductName()));
		_product.setProductDetails(HtmlUtils.htmlEscape(_product.getProductDetails()));

		System.out.println("<><> Product Name = "+HtmlUtils.htmlEscape(_product.getProductName()));
		System.out.println("<><> AFTER Sanitization:"+Utils.toJsonString(_product)+"\n");

		log.debug("|"+name()+"|Security Fixed: Request to Update Product Details... "+_product);
		ProductEntity prodEntity = productServiceImpl.updateProduct(_product);
		StandardResponse stdResponse = createSuccessResponse("Product Updated!");
		stdResponse.setPayload(prodEntity);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Cross-Site Scripting (XSS) Vulnerability
	 * Cross-Site Scripting (XSS) attacks occur
	 * - when an attacker uses a web application to send the malicious script,
	 * - Generally in the form of a browser-side script to a different end user.
	 * - The end user’s browser cannot know that the script should not be trusted
	 * - and will execute the script. XSS attacks can lead to a variety of problems,
	 * - including stolen session tokens or login credentials, defacement of websites, or malicious redirection.
	 *
	 * Add Cart Item to Cart
	 */
	@Operation(summary = "XSS Input Validation Vulnerability: Add Item to Cart")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Add the Cart Item",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "404",
					description = "Unable to Add the Cart Item",
					content = @Content)
	})
	@PostMapping("/xss/input/validation/cart/add")
	public ResponseEntity<StandardResponse> addToCart(@Valid @RequestBody Cart _cart) {

		// The Inputs (Cart Converted to CartEntity) validated using Regex Patterns and
		// Add To Cart is NOT Vulnerable to XSS Attack.

		log.debug("|"+name()+"|Security Fixed: Request to Add Cart Item... "+_cart.getProductName());
		CartEntity cartItem = cartService.save(_cart);
		StandardResponse stdResponse = createSuccessResponse("Cart Item Added!");
		stdResponse.setPayload(cartItem);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Directory traversal Vulnerability
	 * Directory traversal attacks involve exploiting insufficient security validation / sanitization of user-supplied
	 * input file names, so that characters representing "traverse to parent directory" are passed through to the file
	 * APIs. This could potentially allow the attacker to read or write files outside of the intended directory.
	 *
	 * Countermeasures involve validating and sanitizing input, implementing secure error handling, and adhering to
	 * the principle of least privilege.
	 *
	 * Read the File from the Resource Folder
	 * @param _fileName
	 * @return
	 */
	@Operation(summary = "Directory traversal Vulnerability: Read File")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "File Read",
					content = {@Content(mediaType = "application/text")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Read File",
					content = @Content)
	})
	@GetMapping("/directory/traversal/readFile")
	public ResponseEntity<InputStreamResource> readFile(@RequestParam("_fileName") String _fileName) {
		log.debug("|"+name()+"|Security Fixed: Request to Read File "+_fileName);

		// Check For Directory Traversal Attack
		String cleanPath = StringUtils.cleanPath(_fileName);
		if (cleanPath.contains("..")) {
			// Reject the request
			throw new DataNotFoundException("Invalid path for File: "+_fileName);
		}
		// Read Data From a Specific Location
		try {
			ClassPathResource htmlFile = new ClassPathResource("static/files/" + _fileName);
			return ResponseEntity
					.ok()
					.contentType(MediaType.valueOf(MediaType.TEXT_HTML_VALUE))
					.body(new InputStreamResource(htmlFile.getInputStream()));
		} catch (Exception ex) {
			throw new DataNotFoundException("Invalid path for File: "+_fileName);
		}
	}

	/**
	 * Command Injection Vulnerability
	 * Command Injection Vulnerability, also known as Shell Injection or OS Command Injection, is a type of injection
	 * vulnerability where an attacker is able to execute arbitrary commands on the host operating system through a
	 * vulnerable application. This kind of vulnerability arises when input provided by the user is improperly sanitized
	 * before being passed to a system command.
	 *
	 * @param _fileName
	 * @return
	 */
	@Operation(summary = "Command Injection Vulnerability: Read File")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "File Read",
					content = {@Content(mediaType = "application/text")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Read File",
					content = @Content)
	})
	@GetMapping("/cmd/injection/readFile")
	public ResponseEntity<String> commandExecute(@RequestParam("_fileName") String _fileName) {
		log.debug("|"+name()+"|Security Fixed: Request to Read File using CMD "+_fileName);
		try {
			// Check For Directory Traversal Attack
			String cleanPath = StringUtils.cleanPath(_fileName);
			if (cleanPath.contains("..")) {
				// Reject the request
				throw new DataNotFoundException("Invalid path for File: "+_fileName);
			}
			// Read Data From a Specific Location
			ClassPathResource dataFile = new ClassPathResource("static/files/" + _fileName);
			log.info("Sanitized File Name: "+dataFile.getFilename());
			// Using ProcessBuilder to avoid Shell Injection and safely pass filename as an argument
			ProcessBuilder builder = new ProcessBuilder("cat", dataFile.getFilename());
			Process process = builder.start();
			// Rest of the code to read the process's output
			return ResponseEntity.ok(readData(process));
		} catch (Exception e) {
			throw new DataNotFoundException("Invalid CMD! For File: "+_fileName);
		}
	}

	private String readData(Process process) {
		// Read the output from the process
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
				content.append(System.lineSeparator());
			}
		} catch (IOException e) {
			// Handle the exception
			e.printStackTrace();
		} finally {
			if(content.toString().length() > 1) {
				return content.toString();
			}
			return "No Data Available";
		}
	}

	/**
	 * HTTP Response Splitting Vulnerability
	 * HTTP Response Splitting is an attack that takes advantage of the way HTTP headers are processed. By injecting
	 * newline characters into HTTP header values, an attacker can create additional HTTP headers and even completely
	 * separate HTTP responses, thereby manipulating the HTTP process on the client-side.
	 *
	 * Set the Cookie with Patient's Name
	 *
	 * @param response
	 * @param patientName
	 */
	public void setPatientCookie(HttpServletResponse response, String patientName) {
		// Ensure patientName doesn't contain newline characters
		if (patientName.matches("[\\r\\n]")) {
			throw new IllegalArgumentException("Invalid characters in patient name");
		}
		response.addHeader("Set-Cookie", "patient=" + patientName);
	}

	/**
	 * HTTP Response Splitting Vulnerability
	 * HTTP Response Splitting is an attack that takes advantage of the way HTTP headers are processed. By injecting
	 * newline characters into HTTP header values, an attacker can create additional HTTP headers and even completely
	 * separate HTTP responses, thereby manipulating the HTTP process on the client-side.
	 *
	 * Exploit
	 * jane.doe\r\nContent-Length: 0\r\n\r\n<script>alert('Hacked!');</script>
	 *
	 * Set the Cookie with Customer's ID
	 *
	 * @param response
	 * @param customerId
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "HTTP Response Splitting Vulnerability: Get The Cart")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Customer ID",
					content = @Content)
	})
	@GetMapping("/http/split/cart/customer/{customerId}")
	@ResponseBody
	public ResponseEntity<StandardResponse> splitCookieValue(HttpServletResponse response,
															 @PathVariable("customerId") String customerId) throws Exception {
		log.debug("|"+name()+"|Request to Get Cart For the Customer "+customerId);
		if(customerId == null || customerId.isEmpty()) {
			throw new IllegalArgumentException("Invalid Customer ID");
		}
		if (customerId.matches("[\\r\\n]")) {
			throw new IllegalArgumentException("Invalid characters in Customer Id");
		}
		List<CartEntity> cart = cartService.findByCustomerId(customerId);
		StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  "+cart.size());
		stdResponse.setPayload(cart);
		response.addHeader("Set-Cookie", "customerId=" + customerId);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Parameter Manipulation Vulnerability
	 * Parameter Manipulation is an attack where an attacker alters parameters sent between the client and the
	 * server to gain unauthorized access to data or perform actions they aren't permitted to perform.
	 *
	 * GET Method Call to Get Cart for the Customer
	 * @return
	 */
	@SingleTokenAuthorizationRequired(role = "User")
	@Operation(summary = "Parameter Manipulation Vulnerability: Get The Cart", security = { @SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Customer ID",
					content = @Content)
	})
	@GetMapping("/parameter/manipulation/cart/customer/{customerId}")
	@ResponseBody
	public ResponseEntity<StandardResponse> fetchCart(@PathVariable("customerId") String customerId) throws Exception {
		if(customerId == null || customerId.isEmpty()) {
			throw new DataNotFoundException("Invalid Customer ID "+customerId);
		}
		if(!customerId.equalsIgnoreCase(super.getClaims().getSubject())) {
			log.info("|"+name()+"|Security breach: Un-Authorised Access: "+customerId);
			throw new SecurityException("Security Exception: Access Denied for "+customerId);
		}
		log.debug("|"+name()+"|Request to Get Cart For the Customer "+customerId);
		List<CartEntity> cart = cartService.findByCustomerId(customerId);
		StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  "+cart.size());
		stdResponse.setPayload(cart);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * SQL Injection Vulnerability
	 * SQL Injection is one of the most common and dangerous web application vulnerabilities. It occurs when an
	 * attacker can inject arbitrary SQL code into a query, which is then executed by the database. This can lead
	 * to various malicious outcomes, including unauthorized viewing of data, corrupting or deleting data, and in
	 * some cases, even complete control over the host machine.
	 *
	 * Exploit
	 * jane.doe' OR '1'='1
	 *
	 * GET Method Call to Get Cart for the Customer
	 * @param customerId
	 * @return
	 */
	@Operation(summary = "SQL Injection Vulnerability: Get The Cart")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Customer ID",
					content = @Content)
	})
	@GetMapping("/sql/injection/cart/customer/{customerId}")
	public ResponseEntity<StandardResponse> fetchCartSQLi(@PathVariable("customerId") String customerId) {
		// SQL Injection Vulnerability Fixed
		TypedQuery<CartEntity> query = entityManager.createQuery("SELECT c FROM CartEntity c " +
				"WHERE c.customerId = :customerId",  CartEntity.class);
		query.setParameter("customerId", customerId);
		List<CartEntity> cart = query.getResultList();
		StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  "+cart.size());
		stdResponse.setPayload(cart);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * In-Band SQL Injection (Classic SQL Injection)
	 * In an In-Band SQL Injection attack, the attacker uses the same communication channel to both launch the
	 * attack and gather results. Error-based and Union-based SQL Injections are considered types of In-Band SQL
	 * Injections.
	 *
	 * GET Method Call to Get Cart for the Customer
	 * @param customerId
	 * @return
	 */
	@Operation(summary = "In-Band SQL Injection (Classic SQL Injection): Get The Cart")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Customer ID",
					content = @Content)
	})
	@GetMapping("/sql/injection/inband/cart/customer/{customerId}")
	public ResponseEntity<StandardResponse> fetchCartSQLiErrorBased(@PathVariable("customerId") String customerId) {
		// SQL Injection Vulnerability Fixed
		Query query = entityManager.createNativeQuery("SELECT * FROM carts_tx  WHERE customerId = :customerId", CartEntity.class);
		query.setParameter("customerId", customerId);
		List<CartEntity> cart = query.getResultList();
		StandardResponse stdResponse = createSuccessResponse("Cart Retrieved. Items =  "+cart.size());
		stdResponse.setPayload(cart);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Out-of-Band SQL Injection
	 * In Out-of-Band SQL Injection, the attacker is unable to use the same channel to launch the attack and gather
	 * results and must use different means to receive the response, such as DNS requests or HTTP requests to an
	 * attacker-controlled server.
	 *
	 * Example: Exploiting via Database Notification Functionality
	 * Imagine an application that uses a query to send email notifications based on user input:
	 *
	 * Exploit
	 * jane.doe'; DROP TABLE ms_schema.critical_table_tx; --
	 *
	 * GET Method Call to Get Cart for the Customer
	 * @param customerId
	 * @return
	 */
	@Operation(summary = "SQL Injection Vulnerability OUT-Band: Executing Stored Procedure.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Cart Retrieved!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Invalid Customer ID",
					content = @Content)
	})
	@GetMapping("/sql/injection/outband/cart/customer/{customerId}")
	@Transactional
	public ResponseEntity<StandardResponse> sqlOutBandTest(@PathVariable("customerId") String customerId) {
		// SQL Injection Vulnerability Fixed
		Query query = entityManager.createNativeQuery("SELECT ms_schema.sendNotificationFunc(?)");
		query.setParameter(1, customerId);
		Object result = query.getSingleResult();

		StandardResponse stdResponse = createSuccessResponse("Stored Procedure executed: Result = "+result);
		return ResponseEntity.ok(stdResponse);
	}

 }