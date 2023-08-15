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
// Spring
import org.springframework.stereotype.Service;
// Java
import java.io.*;
import java.nio.charset.StandardCharsets;
// SLF4J
import org.slf4j.Logger;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Java IO Examples
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */

@Service
public class FileIOExample extends AbstractFileProcessing {

    private static final Logger log = getLogger(lookup().lookupClass());

    /**
     * Read File
     * @param filePath
     */
    public long readFile(String filePath) {
        return readFile(filePath, AbstractFileProcessing.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Read File
     * @param filePath
     * @param bufferSize
     * @return
     */
    public long readFile(String filePath, int bufferSize ) {
        bufferSize = bufferSize < 11 ? AbstractFileProcessing.MIN_BUFFER_SIZE : bufferSize;
        // Try Statement automatically closes the reader after the try block
        // This is a Java 7 Feature
        long fileSize = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath), bufferSize)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line
                fileSize += line.length();
                // System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileSize;
    }

    /**
     * Read Input Stream
     * @param inputStream
     * @param bufferSize
     * @return
     */
    public long readFile(InputStream inputStream, int bufferSize ) {
        bufferSize = bufferSize < 11 ? AbstractFileProcessing.MIN_BUFFER_SIZE  : bufferSize;
        // Try Statement automatically closes the reader after the try block
        // This is a Java 7 Feature
        long fileSize = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), bufferSize)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line
                fileSize += line.length();
                // System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileSize;
    }

    /**
     * Read File Content using Input Stream
     *
     * @param inputStream
     * @param bufferSize
     * @return
     */
    @Override
    public StringBuilder readFileContent(InputStream inputStream, int bufferSize) {
        long startTime = System.nanoTime();
        bufferSize = bufferSize < 11 ? AbstractFileProcessing.MIN_BUFFER_SIZE  : bufferSize;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), bufferSize)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            calculateTime(startTime, sb);
        }
        return sb;
    }

    /**
     * Read File Content
     * @param fileName
     * @param bufferSize
     * @return
     */
    public StringBuilder readFileContentBuffer(String fileName, int bufferSize, boolean showFile) {
        long startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        try (FileReader fileReader = new FileReader(fileName);
             BufferedReader bufferedReader = new BufferedReader(fileReader, bufferSize)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(showFile) {
                    sb.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            calculateTime(startTime, sb);
        }
        return sb;
    }

    /**
     * Read File Content using Random Access File
     * @param fileName
     * @param bufferSize
     * @param showFile
     * @return
     */
    public StringBuilder readFileContent(String fileName, int bufferSize, boolean showFile) {
        long startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
            // If you want to seek to the beginning, use:
            raf.seek(0);

            String line;
            while ((line = raf.readLine()) != null) {
                if (showFile) {
                    sb.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            calculateTime(startTime, sb);
        }
        return sb;
    }

}
