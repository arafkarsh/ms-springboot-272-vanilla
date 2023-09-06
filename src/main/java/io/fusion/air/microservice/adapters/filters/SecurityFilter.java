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
import io.fusion.air.microservice.domain.models.core.StandardResponse;
import io.fusion.air.microservice.server.config.ServiceConfiguration;
import io.fusion.air.microservice.utils.Utils;
// Spring
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
// Servlet
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
// SLF4J
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * Servlet Filter for Security Example
 *
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */

/**
 * In a Spring Boot application, if you annotate your filter class with @Component, Spring's auto-configuration
 * picks it up and applies it globally to every request. This means it will act on all incoming requests, unless
 * you have some conditional logic within the filter's doFilter method to exclude certain paths or requests.
 */
@Component
//@Order(2)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityFilter extends OncePerRequestFilter {
    // Set Logger -> Lookup will automatically determine the class name.
    private static final Logger log = getLogger(lookup().lookupClass());

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param _servletRequest
     * @param _servletResponse
     * @param _filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest _servletRequest, HttpServletResponse _servletResponse,
                                    FilterChain _filterChain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) _servletRequest;
        HttpServletResponse response = (HttpServletResponse) _servletResponse;

        try {
            HttpHeaders headers = Utils.createSecureCookieHeaders("JSESSIONID", UUID.randomUUID().toString(), 3000);
            System.out.println("<[2]>>> Security Filter Called => "+ headers.getFirst("Set-Cookie"));

            _filterChain.doFilter(request, response);

            response.setHeader("Set-Cookie", headers.getFirst("Set-Cookie"));
            // Return the Headers
            HeaderManager.returnHeaders(request, response);
        } catch (RequestRejectedException e) {
            if (!response.isCommitted()) {
                StandardResponse error = Utils.createErrorResponse(
                    null,"", "AA100", HttpStatus.FORBIDDEN,
                    "The request was rejected by Firewall");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                String json = Utils.toJsonString(error);

                PrintWriter out = response.getWriter();
                out.write(json);
                out.flush();
                System.out.println(">1>>>  Request Rejected by Firewall "+e.getMessage());
            }
        }
    }
}

