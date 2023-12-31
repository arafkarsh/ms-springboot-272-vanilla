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
package io.fusion.air.microservice.domain.entities.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusion.air.microservice.domain.entities.core.mdc.AbstractBaseEntityWithUUID;
import io.fusion.air.microservice.domain.models.order.Product;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */

@Entity
@Table(name = "products_m")
public class ProductEntity extends AbstractBaseEntityWithUUID {

    @Column(name = "productName")
    @NotBlank(message = "The Product Name is required.")
    @Size(min = 3, max = 32, message = "The length of Product Name must be 3-32 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9 \\-]{3,32}$", message = "Must contain only alphanumeric characters, spaces, and dashes, up to 32 characters")
    private String productName;

    @Column(name = "productDetails")
    @NotNull(message = "The Product Details is  required.")
    @Size(min = 5, max = 64, message = "The length of Product Description must be 5-64 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9 ,\\-]{5,64}$", message = "Must contain only alphanumeric characters, spaces, commas, and dashes, up to 64 characters")
    private String productDetails;

    @NotNull(message = "The Price is required.")
    @Column(name = "price")
    private BigDecimal productPrice;

    @NotBlank(message = "The Product ZipCode is required.")
    @Column(name = "productLocationZipCode")
    @Pattern(regexp = "^[0-9]{5}\\b", message = "Zip Code Must be 5 Digits")
    private String productLocationZipCode;

    /**
     * Empty Product Entity
     */
    public ProductEntity() {
    }

    /**
     * Create Product from the Product DTO
     * @param _product
     */
    public ProductEntity(Product _product) {
        this(_product.getProductName(), _product.getProductDetails(),
                _product.getProductPrice(), _product.getProductLocationZipCode());
    }

    /**
     * Create Product Entity
     * @param _pName
     * @param _pDetails
     * @param _pPrice
     * @param _pZipCode
     */
    public ProductEntity(String _pName, String _pDetails, BigDecimal _pPrice, String _pZipCode) {
        this.productName            = _pName;
        this.productDetails         = _pDetails;
        this.productPrice           = _pPrice;
        this.productLocationZipCode = _pZipCode;
    }

    /**
     * Get Product ID
     * @return
     */
    @JsonIgnore
    public UUID getProductId() {
        return getUuid();
    }

    /**
     * Returns Product ID as String
     * @return
     */
    @JsonIgnore
    public String getProductIdAsString() {
        return getUuid().toString();
    }

    /**
     * Returns Product Name
     * @return
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Returns Product Details
     * @return
     */
    public String getProductDetails() {
        return productDetails;
    }

    /**
     * Returns Product Price
     * @return
     */
    public BigDecimal getProductPrice() {
        return productPrice;
    }

    /**
     * Returns Product Zip Code
     * @return
     */
    public String getProductLocationZipCode() {
        return productLocationZipCode;
    }

    // Set Methods Provided to demonstrate the Update Part of the CRUD Operations
    // Immutable Objects are always better.
    /**
     * Set the Product Name
     * @param productName
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Set the Product Details
     * @param productDetails
     */
    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    /**
     * Set the Product Price
     * @param productPrice
     */
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    /**
     * De-Activate Product
     */
    @JsonIgnore
    public void deActivateProduct() {
        deActivate();
    }

    /**
     * Activate Product
     */
    @JsonIgnore
    public void activateProduct() {
        activate();
    }

    /**
     * Return This Product ID / Name
     * @return
     */
    public String toString() {
        return super.toString() + "|" + productName;
    }
}
