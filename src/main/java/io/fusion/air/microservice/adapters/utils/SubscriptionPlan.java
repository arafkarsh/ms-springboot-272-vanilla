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

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
 public enum SubscriptionPlan {

    FREEMIUM {
        public Bandwidth getLimit() {
            return Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
        }
    },
    STANDARD {
        public Bandwidth getLimit() {
            return Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1)));
        }
    },
    PROFESSIONAL {
        public Bandwidth getLimit() {
            return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        }
    },
    ENTERPRISE {
        public Bandwidth getLimit() {
            return Bandwidth.classic(1000, Refill.intervally(100, Duration.ofMinutes(1)));
        }
    };

    /**
     * Get the Bandwidth Limit
     * @return
     */
    public abstract Bandwidth getLimit();

    /**
     * Get the Subscription Plan API Usage based on the License Key
     * @param licenseKey
     * @return
     */
    public static SubscriptionPlan getPlan(String licenseKey) {
        if (licenseKey == null || licenseKey.isEmpty() || licenseKey.startsWith("FREE000-")) {
            return FREEMIUM;
        } else if (licenseKey.startsWith("STD3000-")) {
            return STANDARD;
        } else if (licenseKey.startsWith("PRO5000-")) {
            return PROFESSIONAL;
        } else if (licenseKey.startsWith("ENT7000-")) {
            return ENTERPRISE;
        }
        return FREEMIUM;
    }

    /**
     * For Testing
     * @param args
     */
    public static void main(String[] args) {
        String licenseKey = "STD3000-XXXXX"; // Replace with a valid license key
        SubscriptionPlan plan = SubscriptionPlan.getPlan(licenseKey);

        Bandwidth limit = plan.getLimit();
        System.out.println("Plan: " + plan.name());
        System.out.println("Rate Limit: " + limit.toString());
    }
}
