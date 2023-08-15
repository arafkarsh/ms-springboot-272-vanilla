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
package io.fusion.air.microservice.adapters.io;
//  Custom Exceptions
import io.fusion.air.microservice.domain.exceptions.InvalidInputException;
// Java
import org.slf4j.Logger;
import java.io.InputStream;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public abstract class AbstractFileProcessing {

    private static final Logger log = getLogger(lookup().lookupClass());

    public static int MIN_BUFFER_SIZE = 128;
    public static int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Read File
     * @param filePath
     * @return
     */
    public abstract long readFile(String filePath);

    /**
     * Read File
     * @param filePath
     * @param bufferSize
     * @return
     */
    public abstract long readFile(String filePath, int bufferSize);

    /**
     * Read File
     * @param inputStream
     * @param bufferSize
     * @return
     */
    public abstract long readFile(InputStream inputStream, int bufferSize );

    /**
     * Read File Content
     * @param inputStream
     * @param bufferSize
     * @return
     */
    public abstract StringBuilder readFileContent(InputStream inputStream, int bufferSize );

    /**
     * Log the Processing Time
     * @param startTime
     * @param fileName
     * @param counters
     * @param bufferSize
     */
    private void logProcessingTime(long startTime, String fileName, int counters, int bufferSize) {
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        log.info("File="+fileName+", Counters="+counters+" BufferSize="+bufferSize+"Processing Time="+processingTime+" ms");
    }

    /**
     * Calculate processing Time
     * @param startTime
     * @param sb
     * @return
     */
    public void calculateTime(long startTime, StringBuilder sb) {
        long endTime = System.nanoTime();
        long processingTime = endTime - startTime;
        double milliSeconds = (double) processingTime / 1000000.0;  // 1 millisecond = 1,000,000 nanoseconds
        sb.append(System.lineSeparator());
        sb.append("--------------------------------------------------------------------------------------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("Processing Time: ").append(processingTime).append(" nano seconds OR ").append(milliSeconds).append(" ms");
        sb.append(System.lineSeparator());
    }

    /**
     * Read File Multiple Times
     * @param filePath
     */
    public FileDataStats readFileMultipleTimes(String filePath) {
        return readFileMultipleTimes(filePath, 8192);
    }

    /**
     * Read File Multiple Times
     * @param filePath
     * @param bufferSize
     */
    public FileDataStats readFileMultipleTimes(String filePath,  int bufferSize) {
        return readFileMultipleTimes(filePath, 1000, bufferSize);
    }

    /**
     * Read File Multiple Times
     * @param filePath
     * @param counter
     * @param bufferSize
     */
    public FileDataStats readFileMultipleTimes(String filePath, int counter, int bufferSize) {
        validateInputs( filePath,  null,  counter,  bufferSize);
        long startTime = System.currentTimeMillis();
        long fileSize = 0;
        for(int i=0; i<counter; i++) {
            fileSize += readFile(filePath, bufferSize);
        }
        FileDataStats fileStats = new FileDataStats(filePath, counter, bufferSize,
                (System.currentTimeMillis() - startTime), fileSize);
        logProcessingTime(startTime, filePath, counter, bufferSize);
        return fileStats;
    }

    /**
     *
     * @param filePath
     * @param inputStream
     * @param counter
     * @param bufferSize
     * @return
     */
    public FileDataStats readFileMultipleTimes(String filePath, InputStream inputStream, int counter, int bufferSize) {
        validateInputs( filePath,  inputStream,  counter,  bufferSize);
        long startTime = System.currentTimeMillis();
        long fileSize = 0;
        String fn = inputStream.toString();
        for(int i=0; i<counter; i++) {
            fileSize += readFile(inputStream, bufferSize);
        }
        FileDataStats fileStats = new FileDataStats(filePath, counter, bufferSize,
                (System.currentTimeMillis() - startTime), fileSize);
        logProcessingTime(startTime, filePath, counter, bufferSize);
        return fileStats;
    }

    /**
     * Validate the Inputs
     * @param filePath
     * @param inputStream
     * @param counter
     * @param bufferSize
     */
    private void validateInputs(String filePath, InputStream inputStream, int counter, int bufferSize) {
        if(filePath == null || inputStream == null) {
            throw new InvalidInputException("File Path or InputStream is required");
        }
        if(counter < 1 || counter > 1000000) {
            throw new InvalidInputException("Counter should be greater than 0 and less than 1000000");
        }
        if(bufferSize < 11) {
            throw new InvalidInputException("Buffer Size should be greater than 10");
        }
    }
}
