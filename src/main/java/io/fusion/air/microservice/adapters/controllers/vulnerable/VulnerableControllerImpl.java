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
package io.fusion.air.microservice.adapters.controllers.vulnerable;
// Custom
import io.fusion.air.microservice.domain.entities.order.CartEntity;
import io.fusion.air.microservice.domain.entities.order.ProductEntity;
import io.fusion.air.microservice.domain.exceptions.DataNotFoundException;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.domain.ports.services.CartService;
import io.fusion.air.microservice.domain.ports.services.ProductService;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
// Swagger API
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
// Java
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
// SLF4J
import org.slf4j.Logger;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Code with Vulnerabilities
 * Checkout FixedControllerImpl for the Fixed Code for the vulnerabilities mentioned in this code.
 *
 * @author arafkarsh
 * @version 1.0
 * @See FixedControllerImpl
 */
@CrossOrigin
@Configuration
@RestController
// "/ms-cache/api/v1"
@RequestMapping("${service.api.path}/security/vulnerable")
@RequestScope
@Tag(name = "Security Vulnerability", description = "XSS, Input Validation, Buffer Overflow, SQL Injection, CSRF, Shell Injection, Directory Traversal, Http Response Splitting etc.")
public class VulnerableControllerImpl extends AbstractController {

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
	public ResponseEntity<StandardResponse> updateProduct(@RequestBody ProductEntity _product) {
		// No Request  Body Validation is done.
		// The Inputs are NOT Sanitized and hence the Search is Vulnerable to XSS Attack.

		log.debug("|"+name()+"|Security Vulnerable: Request to Update Product Details... "+_product);
		ProductEntity prodEntity = productServiceImpl.updateProduct(_product);
		StandardResponse stdResponse = createSuccessResponse("Product Updated!");
		stdResponse.setPayload(prodEntity);
		return ResponseEntity.ok(stdResponse);
	}

	/**
	 * Directory Traversal Vulnerability
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
	@Operation(summary = "Directory Traversal Vulnerability: Read File")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "File Read",
					content = {@Content(mediaType = "application/text")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Read File",
					content = @Content)
	})
	@GetMapping("/directory/traversal/readFile")
	public ResponseEntity<String> readFile(@RequestParam("_fileName") String _fileName) {
		log.debug("|"+name()+"|Security Vulnerable: Request to Read File "+_fileName);
		String fileContent = readFromFile(_fileName);
		return ResponseEntity.ok(fileContent);
	}

	/**
	 * Vulnerable to Directory Traversal Attack
	 *
	 * Read the File from the Resource Folder
	 * @param _fileName
	 * @return
	 */
	private String readFromFile(String _fileName) {
		File file = new File(_fileName);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				sb.append(line).append(System.lineSeparator());
			}
		} catch (FileNotFoundException e) {
			throw new DataNotFoundException("Error: File Not Found! "+_fileName, e);
		} catch (IOException e) {
			throw new DataNotFoundException("Error: IO Exception! "+_fileName,e);
		} catch (Exception e) {
			throw new DataNotFoundException("Error: Exception! "+_fileName,e);
		}
		return sb.toString();
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
		log.debug("|"+name()+"|Security Vulnerable: Request to Read File "+_fileName);
		try {
			// Concatenating filename directly into command
			// Process process = Runtime.getRuntime().exec("cat " + _fileName);
			// Rest of the code to read the process's output
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok("File content");
	}

	/**
	 * HTTP Response Splitting Vulnerability
	 * HTTP Response Splitting is an attack that takes advantage of the way HTTP headers are processed. By injecting
	 * newline characters into HTTP header values, an attacker can create additional HTTP headers and even completely
	 * separate HTTP responses, thereby manipulating the HTTP process on the client-side.
	 *
	 * Set the Cookie with Customer ID
	 *
	 * @param response
	 * @param customerId
	 */
	public void setPatientCookie(HttpServletResponse response, String customerId) {
		response.addHeader("Set-Cookie", "customerId=" + customerId);
	}

	/**
	 * HTTP Response Splitting Vulnerability
	 * HTTP Response Splitting is an attack that takes advantage of the way HTTP headers are processed. By injecting
	 * newline characters into HTTP header values, an attacker can create additional HTTP headers and even completely
	 * separate HTTP responses, thereby manipulating the HTTP process on the client-side.
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
	@Operation(summary = "Parameter Manipulation Vulnerability: Get The Cart")
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
		// SQL Injection Vulnerability by directly concatenating the input
		TypedQuery<CartEntity> query = entityManager.createQuery("SELECT c FROM CartEntity c WHERE c.customerId = '" + customerId + "'", CartEntity.class);
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
	 * Exploit
	 * jane.doe' OR '1'='1
	 *
	 * GET Method Call to Get Cart for the Customer
	 * @param customerId
	 * @return
	 */
	@Operation(summary = "SQL Injection Vulnerability IN-Band: Get The Cart")
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
		// SQL Injection Vulnerability by directly concatenating the input
		Query query = entityManager.createNativeQuery("SELECT * FROM carts_tx  WHERE customerId = '" + customerId + "'", CartEntity.class);
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
	 * jane.doe' OR '1'='1
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
	public ResponseEntity<StandardResponse> sqlOutBandTest(@PathVariable("customerId") String customerId) {
		// SQL Injection Vulnerability by directly concatenating the input
		Object result = "No Data Found!";
		try {
			Query query = entityManager.createNativeQuery("SELECT ms_schema.sendNotificationFunc('" + customerId + "')");
			result = query.getSingleResult();
		} catch (Exception e) {
			log.error("|"+name()+"|Error Occurred: "+e.getMessage());
		}

		StandardResponse stdResponse = createSuccessResponse("Stored Procedure executed: Result = "+result);
		return ResponseEntity.ok(stdResponse);
	}

 }