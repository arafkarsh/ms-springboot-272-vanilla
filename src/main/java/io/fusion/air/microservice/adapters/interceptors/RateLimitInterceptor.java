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
package io.fusion.air.microservice.adapters.interceptors;

import io.fusion.air.microservice.adapters.ratelimit.RateLimitService;
import io.fusion.air.microservice.domain.exceptions.LimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    /**
     * Pre Handle to Check Rate Limit
     * @param _request
     * @param _response
     * @param _handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest _request, HttpServletResponse _response, Object _handler) throws Exception {
        String licenseKey = "FREE000-000";
        try {
            licenseKey = _request.getHeader(RateLimitService.LICENSE_KEY_HEADER);
        } catch (Exception ignored) {}
        finally {
            if (licenseKey == null || licenseKey.isEmpty()) {
                licenseKey = "FREE000-000";
            }
        }
        Bucket tokenBucket = rateLimitService.getBucketForLicense(licenseKey);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            _response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            _response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            throw new LimitExceededException("You have exhausted your API Request Quota");
        }
    }
}
