/**
 * (C) Copyright 2022 Araf Karsh Hamid
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
package io.fusion.air.microservice.adapters.filters;
// Custom
import io.fusion.air.microservice.utils.Utils;
// Spring
import org.springframework.core.annotation.Order;
// Servlet
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
// SLF4J
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

/**
 * Servlet Filter with Filter Registration Example
 *
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */

/**
 *  Register the Filter with WebSecurityConfigurerAdapter
 *  @see io.fusion.air.microservice.server.config.WebSecurityConfiguration
 *
 *     @Bean
 *     public FilterRegistrationBean<ProductFilter> productFilterRegistration() {
 *         String apiPath = serviceConfig.getServiceApiPath() + "/product/*";
 *         FilterRegistrationBean<ProductFilter> registrationBean = new FilterRegistrationBean<>();
 *         registrationBean.setFilter(new ProductFilter());
 *         // Add URL patterns here
 *         registrationBean.addUrlPatterns( apiPath);
 *         return registrationBean;
 *     }
 *
 *
 */
public class ProductFilter implements Filter {
    // Set Logger -> Lookup will automatically determine the class name.
    private static final Logger log = getLogger(lookup().lookupClass());

    @Override
    public void doFilter(ServletRequest _servletRequest, ServletResponse _servletResponse, FilterChain _filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) _servletRequest;
        HttpServletResponse response = (HttpServletResponse) _servletResponse;

        HttpHeaders headers = Utils.createSecureCookieHeaders("PROD-RID", MDC.get("ReqId"), 300);
        response.addHeader("Set-Cookie", headers.getFirst("Set-Cookie"));
        System.out.println("<[5]>>> Product Filter Called => "+ headers.getFirst("Set-Cookie"));

        _filterChain.doFilter(request, response);
    }
}

