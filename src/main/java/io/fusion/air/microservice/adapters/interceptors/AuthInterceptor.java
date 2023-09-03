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
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * Pre Handle to Authenticate the Request
     * @param _request
     * @param _response
     * @param _handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest _request, HttpServletResponse _response, Object _handler)
            throws Exception {
        // Add the Auth interceptor code here and return true of Auth is successful.
        // Currently JWT Token is handled in Aspect Layer and not in Interceptor Layer
        return true;
    }
}
