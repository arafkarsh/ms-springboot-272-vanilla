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
import io.fusion.air.microservice.domain.entities.order.ProductEntity;
import io.fusion.air.microservice.domain.exceptions.DataNotFoundException;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.domain.ports.services.CountryService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
// Java
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
@Tag(name = "Security Vulnerable API", description = "To Manage (Add/Update/Delete/Search) Country.")
public class VulnerableControllerImpl extends AbstractController {

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
	 * Cross-Site Scripting (XSS) Vulnerability
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

		log.debug("|"+name()+"|Security Vulnerable: Request to Search the Product By Name ... "+_productName);
		List<ProductEntity> products = productServiceImpl.findProductsAndPriceGreaterThan(
				_productName, _location, _productPrice);

		StandardResponse stdResponse = createSuccessResponse("Products Found ("+products.size()
				+") For Search Keys: Phone = " +_productName + " Price = "+_productPrice + " Location = "+_location);
		stdResponse.setPayload(products);
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
	@Operation(summary = "Read File")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "File Read",
					content = {@Content(mediaType = "application/text")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Read File",
					content = @Content)
	})
	@GetMapping("/readFile")
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
	@Operation(summary = "Read File by Executing Command")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "File Read",
					content = {@Content(mediaType = "application/text")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Read File",
					content = @Content)
	})
	@GetMapping("/cmd/readFile")
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
	 * Set the Cookie with Patient's Name
	 *
	 * @param response
	 * @param patientName
	 */
	public void setPatientCookie(HttpServletResponse response, String patientName) {
		response.addHeader("Set-Cookie", "patient=" + patientName);
	}

 }