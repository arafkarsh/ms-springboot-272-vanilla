/**
 * (C) Copyright 2021 Araf Karsh Hamid
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
package io.fusion.air.microservice.adapters.security;

import io.fusion.air.microservice.server.config.ServiceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private ServiceConfiguration serviceConfig;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Forces All Request to be Secured (HTTPS)
        // http.requiresChannel().anyRequest().requiresSecure();
        String apiPath = serviceConfig.getApiDocPath();
        http.authorizeRequests()
                .antMatchers(apiPath + "/**")
                .permitAll()
                .and()
                // This configures exception handling, specifically specifying that when a user tries to access a page
                // they're not authorized to view, they're redirected to "/403" (typically an "Access Denied" page).
                .exceptionHandling().accessDeniedPage("/403");

        // Enable CSRF Protection
        // This line configures the Cross-Site Request Forgery (CSRF) protection, using a Cookie-based CSRF token
        // repository. This means that CSRF tokens will be stored in cookies. The withHttpOnlyFalse() method makes
        // these cookies accessible to client-side scripting, which is typically necessary for applications that use
        // a JavaScript-based frontend.
        // Disabled for Local Testing
        // http.csrf().disable();
        http.csrf()
                .requireCsrfProtectionMatcher(csrfProtectionMatcher())
                .csrfTokenRepository(csrfTokenRepository());

        /**
         http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // Add the above Only for testing in Swagger
            .and()
            .addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class);
         */

        // X-Frame-Options is a security header that is intended to protect your website against "clickjacking" attacks.
        // Clickjacking is a malicious technique of tricking web users into revealing confidential information or taking
        // control of their interaction with the website, by loading your website in an iframe of another website and
        // then overlaying it with additional content.
        http.headers().frameOptions().deny();
        // Only for Local Testing
        // http.headers().frameOptions().disable();

        // The X-XSS-Protection header is designed to enable the cross-site scripting (XSS) filter built into modern web
        // browsers. This header is usually enabled by default, but using it will enforce it. The mode=block option
        // will block any detected XSS attack.
        http.headers().xssProtection().block(true);

        // The X-Content-Type-Options header is designed to protect against MIME type sniffing. MIME type sniffing is a
        // browser behavior where it tries to determine the MIME type of a resource by inspecting the content itself.
        // This behavior is exploited by attackers to perform cross-site scripting (XSS) and content injection attacks.
        // The nosniff option will prevent browsers from performing MIME type sniffing.
        // http.headers().contentTypeOptions().nosniff();

        String hostName = serviceConfig.getServerHost();
        // Content Security Policy
        // The last part sets the Content Security Policy (CSP). This is a security measure that helps prevent a range
        // of attacks, including Cross-Site Scripting (XSS) and data injection attacks. It does this by specifying which
        // domains the browser should consider to be valid sources of executable scripts. In this case, scripts
        // (script-src) and objects (object-src) are only allowed from the same origin ('self') or from a subdomain of
        // the specified host name.
        http.headers()
                .contentSecurityPolicy(
                        "default-src 'self'; "
                        +"script-src 'self' *."+hostName+"; "
                        +"object-src 'self' *."+hostName+"; "
                        +"img-src 'self'; media-src 'self'; frame-src 'self'; font-src 'self'; connect-src 'self'");
    }

    /**
     * The web.ignoring().antMatchers(...) part of the code tells Spring Security to ignore the specified patterns and
     * not apply security to requests matching those. This is useful for static resources like CSS files, JavaScript
     * files, and images, which don't need to be secured.
     *
     * URL Security: It allows you to restrict URL patterns. This is useful when you want to apply security at a more
     * granular level rather than just at the application level.
     *
     * Ignoring Resources: You can tell Spring Security to completely ignore certain patterns, which means Spring
     * Security won't apply any security on those matched paths.
     *
     * HTTP Firewall: This helps in guarding the application against various types of attack vectors.
     * StrictHttpFirewall, as mentioned earlier, is a component that helps tighten the default configuration
     * to prevent specific web-based attack vectors.
     *
     * Custom Filters: It allows the addition of custom filters to the Spring Security filter chain.
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");

        // add the Strict Firewall explicit to ensure that the default firewall is not used.
        web.httpFirewall(httpFirewall());
    }

    /**
     * Handles Malicious URI Path (handles special characters and other things)
     *
     * Blocked HTTP Methods: By default, it allows only GET, POST, HEAD, OPTIONS, and TRACE methods, blocking others
     * like PUT, DELETE, etc. This is to ensure that only typical browser methods are allowed.
     * URL Decoding: StrictHttpFirewall prevents multiple URL decoding attempts, which can be an attack vector.
     * URL ; (semicolon) blocking: By default, it will block URLs that contain a semicolon. This defends against
     * attacks like request parameter pollution.
     * URL // (double slash) blocking: Prevents URLs with double slashes.
     * URL Backslash \ blocking: By default, backslashes are blocked.
     * URL % (percent) blocking: It can be configured to block URLs with URL-encoded values.
     *
     * Disable the Bean for Testing SQL Injection and Http Response Splitting Attacks.
     * @return
     */
    // @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowedHttpMethods(Arrays.asList("GET","POST", "PUT", "DELETE"));
        return firewall;
    }

    /**
     * Default HTTP Firewall should NOT be used. This ONLY for demo purposes.
     * To Test the SQL Injection Vulnerabilities. This is NOT recommended for Production.
     * Enable the httpFireWall() Bean for Production.
     *
     * @return
     * @see #httpFirewall()
     */
    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }

    /**
     * CSRF Token Implementation
     * @return
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HeaderCSRFTokenRepository();
    }

    /**
     * Use CSRF Token for a Specific Method (GET)
     * THIS ONLY for Demo Purpose.
     *
     * @return
     */
    private RequestMatcher csrfProtectionMatcher() {
        return new CustomCsrfMatcher();
    }

    public class CustomCsrfMatcher implements RequestMatcher {
        private final Pattern allowedMethods = Pattern.compile("^(GET|POST|PUT|DELETE|HEAD|TRACE|OPTIONS)$");
        private final List<AntPathRequestMatcher> protectedGetMatchers;
        private final String pathF = "/ms-vanilla/api/v1/security/fixed";
        private final String pathM2 = "/csrf/validate/customer/**";

        public CustomCsrfMatcher() {
            this.protectedGetMatchers = Arrays.asList(
                    // new AntPathRequestMatcher(pathV + pathM1, "GET"),
                    new AntPathRequestMatcher(pathF + pathM2, "GET")
                    // ... add more paths as needed
            );
        }
        @Override
        public boolean matches(HttpServletRequest request) {
            // If the request match one url the CSRF protection will be enabled
            boolean isMatched = protectedGetMatchers.stream()
                    .anyMatch(matcher -> matcher.matches(request));

            // Otherwise, use the default behavior for CSRF protection
            return isMatched || !allowedMethods.matcher(request.getMethod()).matches();
        }
    };

    /**
     * ONLY For Local Testing with Custom CSRF Headers in Swagger APi Docs
     */
    private static class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
            response.setHeader("X-CSRF-HEADER", token.getHeaderName());
            response.setHeader("X-CSRF-PARAM", token.getParameterName());
            response.setHeader("X-CSRF-TOKEN", token.getToken());
            filterChain.doFilter(request, response);
        }
    }
}

