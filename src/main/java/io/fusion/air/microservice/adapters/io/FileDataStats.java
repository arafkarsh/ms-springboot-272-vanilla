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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public class FileDataStats {

    private String fileName;
    private int counters;
    private int bufferSize;
    private long processingTime;
    private long totalFileSize;

    public FileDataStats(String _fileName, int _counters, int _bufferSize, long _processingTime, long _totalFileSize) {
        fileName = _fileName;
        counters = _counters;
        bufferSize = _bufferSize;
        processingTime = _processingTime;
        totalFileSize = _totalFileSize;
    }

    /**
     * Get File Name
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get Counters
     * @return
     */
    public int getCounters() {
        return counters;
    }

    /**
     * Get Buffer Size
     * @return
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Get Processing Time
     * @return
     */
    public long getProcessingTime() {
        return processingTime;
    }

    /**
     * Get Total File Size
     * @return
     */
    public long getTotalFileSize() {
        return totalFileSize;
    }

    @JsonIgnore
    public String toString() {
        return "File="+fileName+", Counters="+counters+" BufferSize="+bufferSize+"Processing Time="+processingTime+" ms";
    }
}
