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
package io.fusion.air.microservice.adapters.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Auto Generates Csrf Token and Saves it in the Header
 *
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public class HeaderCSRFTokenRepository implements CsrfTokenRepository {

    private static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-TOKEN";
    private final HttpSessionCsrfTokenRepository delegate = new HttpSessionCsrfTokenRepository();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        delegate.saveToken(token, request, response);

        if (token != null) {
            response.setHeader(CSRF_TOKEN_HEADER_NAME, token.getToken());
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }
}
