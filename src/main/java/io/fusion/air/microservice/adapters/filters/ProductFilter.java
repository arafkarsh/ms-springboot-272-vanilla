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
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.utils.Utils;
// Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
// Servlet
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
// SLF4J
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Servlet Filter with WebFilter Example
 *
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */

/**
 * WebSecurityConfigurerAdapter, then its scope of application will depend on the specific configurations you make
 * in the configure(HttpSecurity http) method. You can use methods like antMatcher or antMatchers to specify which
 * routes the filter should apply to.
 * @Override
 * protected void configure(HttpSecurity http) throws Exception {
 *     http
 *          // Apply only to paths that start with /ms-vanilla/api/v1/product/
 *         .antMatcher("/ms-vanilla/api/v1/product/**")
 *         .addFilterBefore(productFilter, io.fusion.air.microservice.adapters.filters.SecurityFilter.class);
 * }
 *
 */
// @Component
@WebFilter(urlPatterns = "/ms-vanilla/api/v1/product/*")
@Order(50)
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

