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
package io.fusion.air.microservice.domain.models.order;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Cart item Request
 *
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public class CartWithRestrictions {

    @NotBlank(message = "The Customer ID is required.")
    @Size(min = 3, max = 32, message = "The length of Customer ID must be 3-32 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9.-]{3,32}$", message = "Must contain only alphanumeric characters, dots, and dashes, between 3 to 32 characters.")
    private String customerId;

    @NotBlank(message = "The Product ID is required.")
    @Size(min = 3, max = 32, message = "The length of Product ID must be 3-32 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9.-]{3,32}$", message = "Must contain only alphanumeric characters, dots, and dashes, between 3 to 32 characters.")
    private String productId;

    @Size(min = 3, max = 64, message = "The length of Product Name must be 3-64 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9.-]{3,64}$", message = "Must contain only alphanumeric characters, dots, and dashes, between 3 to 64 characters.")
    private String productName;

    @NotNull(message = "The Price is required.")
    private BigDecimal price;

    @NotNull(message = "The Quantity is required.")
    private BigDecimal quantity;

    public CartWithRestrictions() {}

    /**
     * Get Customer ID
     * @return
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Get Product ID
     * @return
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Get Product Name
     * @return
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Get Price
     * @return
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Get Quantity
     * @return
     */
    public BigDecimal getQuantity() {
        return quantity;
    }
}
