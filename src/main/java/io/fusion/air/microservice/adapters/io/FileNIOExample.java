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
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
     * ByteBuffer.allocate(): Allocates into JVM Heap
     * ByteBuffer.allocateDirect(): Directly into OS Virtual Memory
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
     * Read File Content from Input Stream
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


    /**
     * Read File Content from File
     * @param fileName
     * @param bufferSize
     * @return
     */
    public StringBuilder readFileContent(String fileName, int bufferSize, boolean showFile) {
        long startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        try (FileChannel fileChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);

            // Disable File Showing for large files
            if(fileChannel.size() > 1024000) {
                showFile = false;
            }

            /**
            try (Scanner scanner = new Scanner(charBuffer)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(showFile) {
                        sb.append(line).append(System.lineSeparator());
                    }
                }
            }
             */
            int start = 0;
            for (int end = 0; end < charBuffer.length(); end++) {
                if (charBuffer.charAt(end) == '\n') {
                    CharSequence line = charBuffer.subSequence(start, end);
                    start = end + 1;
                    if (showFile) {
                        sb.append(line).append(System.lineSeparator());
                    }
                }
            }
            if (showFile) {
                // Append remaining characters if any
                if (start < charBuffer.length()) {
                    sb.append(charBuffer.subSequence(start, charBuffer.length()));
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
     * Read File Content from File
     *
     * @return
     */
    public  ArrayList<String> showFilesInDirectory() {
        ArrayList<String> files = new ArrayList<String>();
        Path startPath = Paths.get("src/main/java");
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    // System.out.println(file);
                    files.add(file.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    /**
     * Read / Write / Delete File
     * @return
     */
    public HashMap<String, String>  fileHandlingNIO() {
        // Creating a Path using Paths.get() method
        HashMap<String, String> map = new HashMap<>();
        Path path = Paths.get("example.txt");
        String data = "ERROR in FILE NIO HANDLING";
        // Writing to a file using Files.write() method
        try {
            data =  LocalDate.now().toString()+" Hello, NIO2!";
            Files.write(path,data.getBytes());
            map.put("Write", "File written successfully!");
            log.info("File written successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            map.put("Write Error", e.getMessage());
        }

        // Reading from a file using Files.readAllBytes() method
        try {
            byte[] bytes = Files.readAllBytes(path);
            data = new String(bytes);
            map.put("Read", "File read successfully!");
            log.info("Content of the file: " +data);
        } catch (IOException e) {
            e.printStackTrace();
            map.put("Read Error", e.getMessage());

        }

        // Deleting a file using Files.delete() method
        try {
            Files.delete(path);
            map.put("Delete", "File deleted successfully!");
            log.info("File deleted successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            map.put("Delete Error", e.getMessage());
        }
        map.put("Data", data);
        return map;
    }

    /**
     * Read Data Asynchronously from File
     *
     * @param fileName
     * @param bufferSize
     * @param showFile
     * @return
     */
    public StringBuilder asyncFileRead(String fileName, int bufferSize, boolean showFile) {
        long startTime = System.nanoTime();
        // This Code is NOT required if Reactive Framework is used
        CompletableFuture<StringBuilder> future = new CompletableFuture<>();

        final StringBuilder sb = new StringBuilder();
        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer byteBuffer) {
                    System.out.println("Read completed");
                    byteBuffer.flip();
                    byte[] data = new byte[byteBuffer.limit()];
                    byteBuffer.get(data);
                    // System.out.println(new String(data));
                    if(showFile) {
                        sb.append(new String(data));
                    }
                    future.complete(sb); // Completing the future with the result
                }

                @Override
                public void failed(Throwable exc, ByteBuffer byteBuffer) {
                    System.err.println("Read failed: " + exc);
                    future.completeExceptionally(exc); // Completing the future with an exception
                }
            });
            // Waiting for the future to complete
            future.get();
        } catch (IOException |  ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            calculateTime(startTime, sb);
        }
        return sb;
    }

}
