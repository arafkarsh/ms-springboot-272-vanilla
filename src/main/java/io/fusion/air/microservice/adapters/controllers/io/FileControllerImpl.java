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
package io.fusion.air.microservice.adapters.controllers.io;
// Custom
import io.fusion.air.microservice.adapters.io.FileDataStats;
import io.fusion.air.microservice.adapters.io.FileIOExample;
import io.fusion.air.microservice.adapters.io.FileNIOExample;
import io.fusion.air.microservice.domain.exceptions.DataNotFoundException;
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.server.controllers.AbstractController;
// Swagger Open API 3.0
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
// Java
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Java File IO / NIO / Streams / Readers / Writers / Security Manager
 *
 * @author arafkarsh
 * @version 1.0
 * @See FixedControllerImpl
 */
@CrossOrigin
@Configuration
@RestController
// "/ms-cache/api/v1"
@RequestMapping("${service.api.path}/security")
@RequestScope
@Tag(name = "Security IO", description = "File IO, Streams, Readers, NIO, Security Manager etc.")
public class FileControllerImpl extends AbstractController {

	// Set Logger -> Lookup will automatically determine the class name.
	private static final Logger log = getLogger(lookup().lookupClass());
	
	@Autowired
	private ServiceConfiguration serviceConfig;

	@Autowired
	private FileIOExample fileIOExample;

	@Autowired
	private FileNIOExample fileNIOExample;

	@Autowired
	private ResourceLoader resourceLoader;


	/**
	 * File IO
	 * @param counter
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java IO BufferedReader", description = "File Processing Java IO BufferedReader")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/io/file/read/counter/{counter}/buffer/{buffer}")
	public ResponseEntity<StandardResponse> fileIO(@PathVariable("counter")int counter,
													   @PathVariable("buffer")int buffer) throws Exception {
		// Read the File from the Resource Folder
		Resource resource = resourceLoader.getResource("classpath:/static/data/a.txt");
		log.debug("|"+name()+"|Security IO: Request to Read File ("+resource.getFilename()+") Counter="+counter+" Buffer="+buffer);
		try {
			FileDataStats filedata = fileIOExample.readFileMultipleTimes(resource.getFilename(),resource.getInputStream(), counter, buffer);
			StandardResponse stdResponse = createSuccessResponse("File IO Processing Result!");
			stdResponse.setPayload(filedata);
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File IO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE IO Error: "+e.getMessage());
		}
	}

	/**
	 * File IO Read Content
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java IO Read Content", description = "File Processing Java IO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/io/file/read/buffer/{buffer}")
	public ResponseEntity<StandardResponse> fileIOContent(@PathVariable("buffer")int buffer) throws Exception {
		// Read the File from the Resource Folder
		Resource resource = resourceLoader.getResource("classpath:/static/data/India-Mars-Orbiter-Mission.txt");
		log.debug("|"+name()+"|Security IO: Request to Read File ("+resource.getFilename()+") Buffer="+buffer);
		try {
			StringBuilder sb = fileIOExample.readFileContent(resource.getInputStream(),buffer);
			StandardResponse stdResponse = createSuccessResponse("File IO Read Content!");
			stdResponse.setPayload(getContent(sb));
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File IO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE IO Error: "+e.getMessage());
		}
	}

	/**
	 * File IO Read Local Content
	 * @param fileName
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java IO Read Local Content", description = "File Processing Java IO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/io/file/read/{fileName}/buffer/{buffer}/show/{showFile}")
	public ResponseEntity<StandardResponse> fileIOLocalContent(
					@PathVariable("fileName")String fileName,
					@PathVariable("buffer")int buffer,
					@PathVariable("showFile")boolean showFile) throws Exception {
		// Read the File from the Resource Folder
		log.debug("|"+name()+"|Security IO: Request to Read Local File ("+fileName+") Buffer="+buffer);
		try {
			StringBuilder sb = fileIOExample.readFileContent(fileName,buffer, showFile);
			StandardResponse stdResponse = createSuccessResponse("File IO Read Local Content!");
			stdResponse.setPayload(getContent(sb));
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File IO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE IO Error: "+e.getMessage());
		}
	}

	/**
	 * File NIO
	 * @param counter
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java NIO", description = "File Processing Java NIO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/nio/file/read/counter/{counter}/buffer/{buffer}")
	public ResponseEntity<StandardResponse> fileNIO(@PathVariable("counter")int counter,
													   @PathVariable("buffer")int buffer) throws Exception {
		// Read the File from the Resource Folder
		Resource resource = resourceLoader.getResource("classpath:/static/data/a.txt");
		log.debug("|"+name()+"|Security IO: Request to Read File ("+resource.getFilename()+") Counter="+counter+" Buffer="+buffer);
		try {
			FileDataStats filedata = fileNIOExample.readFileMultipleTimes(resource.getFilename(),resource.getInputStream(), counter, buffer);
			StandardResponse stdResponse = createSuccessResponse("File NIO Processing Result!");
			stdResponse.setPayload(filedata);
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File NIO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE NIO Error: "+e.getMessage());
		}
	}

	/**
	 * File NIO Read Content
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java NIO Read Content", description = "File Processing Java NIO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/nio/file/read/buffer/{buffer}")
	public ResponseEntity<StandardResponse> fileNIOContent(@PathVariable("buffer")int buffer) throws Exception {
		// Read the File from the Resource Folder
		Resource resource = resourceLoader.getResource("classpath:/static/data/India-Mars-Orbiter-Mission.txt");
		log.debug("|"+name()+"|Security IO: Request to Read File ("+resource.getFilename()+") Buffer="+buffer);
		try {
			StringBuilder sb = fileNIOExample.readFileContent(resource.getInputStream(),buffer);
			StandardResponse stdResponse = createSuccessResponse("File NIO Read Content!");
			stdResponse.setPayload(getContent(sb));
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File NIO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE NIO Error: "+e.getMessage());
		}
	}

	/**
	 * File NIO Read Local Content
	 * @param fileName
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java NIO Read Local Content", description = "File Processing Java NIO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/nio/file/read/{fileName}/buffer/{buffer}/show/{showFile}")
	public ResponseEntity<StandardResponse> fileNIOLocalContent(
									@PathVariable("fileName")String fileName,
									@PathVariable("buffer")int buffer,
									@PathVariable("showFile")boolean showFile) throws Exception {
		// Read the File from the Resource Folder
		log.debug("|"+name()+"|Security IO: Request to Read Local File ("+fileName+") Buffer="+buffer);
		try {
			StringBuilder sb = fileNIOExample.readFileContent(fileName,buffer, showFile);
			StandardResponse stdResponse = createSuccessResponse("File NIO Read Local Content!");
			stdResponse.setPayload(getContent(sb));
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File NIO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE NIO Error: "+e.getMessage());
		}
	}

	/**
	 * File NIO List Directories
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java NIO List Directories", description = "File Processing Java NIO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/nio/directories/list/all")
	public ResponseEntity<StandardResponse> listDirectories() throws Exception {
		// Read the File from the Resource Folder
		log.debug("|"+name()+"|Security IO: Request to List Directories");
		try {
			ArrayList<String> files = fileNIOExample.showFilesInDirectory();
			StandardResponse stdResponse = createSuccessResponse("File NIO List Directories!");
			stdResponse.setPayload(files);
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File NIO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE NIO Error: "+e.getMessage());
		}
	}

	/**
	 * NIO File Handling
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "File Processing Java NIO File Handling", description = "File Processing Java NIO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/nio/file/handling")
	public ResponseEntity<StandardResponse> fileHandling() throws Exception {
		// Read the File from the Resource Folder
		log.debug("|"+name()+"|Security IO: Request to List Directories");
		try {
			HashMap<String, String> data = fileNIOExample.fileHandlingNIO();
			StandardResponse stdResponse = createSuccessResponse("File NIO File Handling!");
			stdResponse.setPayload(data);
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File NIO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE NIO Error: "+e.getMessage());
		}
	}

	@Operation(summary = "File Processing Java NIO Async Read Local Content", description = "File Processing Java NIO")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200",
					description = "Data Found!",
					content = {@Content(mediaType = "application/json")}),
			@ApiResponse(responseCode = "400",
					description = "Unable to Find Data",
					content = @Content)
	})
	@GetMapping("/nio/file/async/read/{fileName}/buffer/{buffer}/show/{showFile}")
	public ResponseEntity<StandardResponse> fileAsyncRead(
			@PathVariable("fileName")String fileName,
			@PathVariable("buffer")int buffer,
			@PathVariable("showFile")boolean showFile) throws Exception {
		// Read the File from the Resource Folder
		log.debug("|"+name()+"|Security IO: Request to Read Async Local File ("+fileName+") Buffer="+buffer);
		try {
			StringBuilder sb = fileNIOExample.asyncFileRead(fileName,buffer, showFile);
			StandardResponse stdResponse = createSuccessResponse("File NIO Async Read Local Content!");
			stdResponse.setPayload(getContent(sb));
			return ResponseEntity.ok(stdResponse);
		} catch (Exception e) {
			log.error("|"+name()+"|File NIO Error Occurred: "+e.getMessage());
			throw new DataNotFoundException("FILE NIO Error: "+e.getMessage());
		}
	}

	/**
	 * Read the Content From StringBuilder and Transform into ArrayList
	 * @param sb
	 * @return
	 */
	private ArrayList<String> getContent(StringBuilder sb) {
		ArrayList<String> content = new ArrayList<>();
		String[] lines = sb.toString().split("\n");
		for(String line: lines) {
			content.add(line);
		}
		return content;
	}
 }