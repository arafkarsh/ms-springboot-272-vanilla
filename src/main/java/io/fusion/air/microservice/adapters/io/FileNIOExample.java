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
// Java IO
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
public class FileNIOExample  extends AbstractFileProcessing {

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
     *
     * FileChannel.open():
     * What It Does: Opens or creates a file at the given path, returning a file channel to access the file. The
     * StandardOpenOption.READ argument specifies that the file should be opened for reading.
     * Why It's Critical: This method gives you a FileChannel, which provides more fine-grained control over file
     * operations compared to the older FileReader class. It's a critical step to access the file at a low level,
     * allowing efficient reading, especially for large files, and enabling other NIO features such as non-blocking
     * I/O.
     *
     * ByteBuffer.allocate():
     * What It Does: Allocates a new byte buffer with the given capacity.
     * Why It's Critical: By allocating a buffer of a specific size, you can control how much data is read from the
     * file at once. It helps in optimizing memory usage and read efficiency, especially with large files. The buffer
     * acts as an intermediate storage area for the bytes read from the file.
     *
     * buffer.flip():
     * What It Does: Switches the buffer from writing mode to reading mode. The limit is set to the current position,
     * and the position is set to 0.
     * Why It's Critical: After reading data into the buffer, it needs to be prepared for reading (i.e., extracting)
     * the data from the buffer. Calling flip() makes this switch, allowing the buffer content to be read sequentially
     * from the beginning.
     *
     * StandardCharsets.UTF_8.decode(buffer):
     * What It Does: Decodes the buffer's content from UTF-8 encoding to a CharBuffer. The content between the
     * buffer's position and limit is treated as encoded characters.
     * Why It's Critical: Files contain bytes, but in order to work with text, those bytes need to be interpreted
     * as characters. This method performs that conversion using the UTF-8 character encoding, making sure that the
     * content is correctly interpreted.
     *
     * @param filePath
     * @param bufferSize
     * @return
     */
    public long readFile(String filePath, int bufferSize) {
        bufferSize = bufferSize < 11 ? AbstractFileProcessing.MIN_BUFFER_SIZE  : bufferSize;
        long fileSize = 0;

        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            while (fileChannel.read(buffer) != -1) {
                buffer.flip();
                fileSize += StandardCharsets.UTF_8.decode(buffer).length();
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileSize;
    }

    /**
     * Read Input Stream
     *
     * ReadableByteChannel channel = Channels.newChannel(inputStream):
     * What It Does: This line of code creates a new channel that reads bytes from the given input stream.
     * Channels.newChannel(inputStream) takes an InputStream object and returns a ReadableByteChannel.
     * Why It's Critical: Creating a ReadableByteChannel from an InputStream allows you to take advantage of
     * Java NIO's capabilities even when you are starting with a traditional input stream. Channels provide
     * more control and efficiency over I/O operations compared to streams. In this specific context, converting
     * the InputStream to a ReadableByteChannel enables you to use it with other NIO features, such as ByteBuffer,
     * to read data from the input stream more effectively. It creates a bridge between old I/O and NIO, letting
     * you use the more advanced features of NIO without changing the original source of data.
     *
     * ByteBuffer.allocate():
     * What It Does: Allocates a new byte buffer with the given capacity.
     * Why It's Critical: By allocating a buffer of a specific size, you can control how much data is read from the
     * file at once. It helps in optimizing memory usage and read efficiency, especially with large files. The buffer
     * acts as an intermediate storage area for the bytes read from the file.
     *
     * buffer.flip():
     * What It Does: Switches the buffer from writing mode to reading mode. The limit is set to the current position,
     * and the position is set to 0.
     * Why It's Critical: After reading data into the buffer, it needs to be prepared for reading (i.e., extracting)
     * the data from the buffer. Calling flip() makes this switch, allowing the buffer content to be read sequentially
     * from the beginning.
     *
     * StandardCharsets.UTF_8.decode(buffer):
     * What It Does: Decodes the buffer's content from UTF-8 encoding to a CharBuffer. The content between the
     * buffer's position and limit is treated as encoded characters.
     * Why It's Critical: Files contain bytes, but in order to work with text, those bytes need to be interpreted
     * as characters. This method performs that conversion using the UTF-8 character encoding, making sure that the
     * content is correctly interpreted.
     *
     * @param inputStream
     * @param bufferSize
     * @return
     */
    public long readFile(InputStream inputStream, int bufferSize) {
        bufferSize = bufferSize < 11 ? AbstractFileProcessing.MIN_BUFFER_SIZE  : bufferSize;
        long fileSize = 0;

        try (ReadableByteChannel channel = Channels.newChannel(inputStream)) {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            while (channel.read(buffer) != -1) {
                buffer.flip();
                fileSize += StandardCharsets.UTF_8.decode(buffer).length();
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileSize;
    }


    /**
     * Read File Content
     * @param inputStream
     * @param bufferSize
     * @return
     */
    public StringBuilder readFileContent(InputStream inputStream, int bufferSize) {
        long startTime = System.nanoTime();
        bufferSize = bufferSize < 11 ? AbstractFileProcessing.MIN_BUFFER_SIZE  : bufferSize;
        StringBuilder sb = new StringBuilder();
        try (ReadableByteChannel channel = Channels.newChannel(inputStream);
             Reader reader = Channels.newReader(channel, StandardCharsets.UTF_8.newDecoder(), bufferSize);
             BufferedReader br = new BufferedReader(reader, bufferSize)) {

            br.lines().forEach(line -> sb.append(line).append(System.lineSeparator()));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            calculateTime(startTime, sb);
        }
        return sb;
    }

}
