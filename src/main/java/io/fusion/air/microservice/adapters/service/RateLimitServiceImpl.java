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
package io.fusion.air.microservice.adapters.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Rate Limit Service using Bucket4J
 *
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
@Service
public class RateLimitServiceImpl  {

    // Set Logger -> Lookup will automatically determine the class name.
    private static final Logger log = getLogger(lookup().lookupClass());
    private final Bucket basicBucket;

    public RateLimitServiceImpl() {
        log.info("Initializing RateLimitServiceImpl with Basic Bucket for the Rate Limit");
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        this.basicBucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Try to Consume the Bucket
     * @return
     */
    public boolean tryConsume() {
        return basicBucket.tryConsume(1);
    }

}
