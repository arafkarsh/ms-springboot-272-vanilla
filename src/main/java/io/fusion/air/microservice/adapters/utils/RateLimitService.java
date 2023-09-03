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
package io.fusion.air.microservice.adapters.utils;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class RateLimitService {

    // Set Logger -> Lookup will automatically determine the class name.
    private static final Logger log = getLogger(lookup().lookupClass());
    private final Bucket freemiumBucket;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public RateLimitService() {
        log.info("Initializing RateLimitService with Freemium Bucket for the Rate Limit");
        this.freemiumBucket = getBucketForLicense("FREE000-000");
    }
    /**
     * Try to Consume the Bucket
     * @return
     */
    public boolean tryConsume() {
        return freemiumBucket.tryConsume(1);
    }

    /**
     * Try to Consume the Freemium Bucket
     * @return
     */
    public boolean tryConsumeFreemium() {
        return tryConsume("FREE000-000");
    }

    /**
     * Try to Consume the Bucket based on License Key
     *
     * @param licenseKey
     * @return
     */
    public boolean tryConsume(String licenseKey) {
        return getBucketForLicense(licenseKey).tryConsume(1);
    }

    /**
     * Get Bucket for the License Key
     * @param licenseKey
     * @return
     */
    public Bucket getBucketForLicense(String licenseKey) {
        return cache.computeIfAbsent(licenseKey, this::createBucket);
    }

    /**
     * Create the Bucket based on License Key
     * @param licenseKey
     * @return
     */
    private Bucket createBucket(String licenseKey) {
        SubscriptionPlan plan = SubscriptionPlan.getPlan(licenseKey);
        return Bucket.builder()
                .addLimit(plan.getLimit())
                .build();
    }
}
