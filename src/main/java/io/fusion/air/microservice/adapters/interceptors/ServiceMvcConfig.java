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

import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
@Configuration
public class ServiceMvcConfig implements WebMvcConfigurer {

    // Set Logger -> Lookup will automatically determine the class name.
    private static final Logger log = getLogger(lookup().lookupClass());

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${service.api.path}")
    private String serviceApiPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("Paths Included = "+serviceApiPath+"/cart/rate/limit/**");
        log.debug("Paths Excluded = "+serviceApiPath+"/cart/rate/limit/all");
        log.debug("Paths Excluded = "+serviceApiPath+"/**");

        registry.addInterceptor(rateLimitInterceptor)
                // Included paths
                .addPathPatterns(serviceApiPath + "/cart/rate/limit/**")
                // Excluded paths
               .excludePathPatterns(
                   serviceApiPath + "/cart/rate/limit/all",
                   serviceApiPath + "/auth/**",
                   serviceApiPath + "/country/**"
               );
    }
}
