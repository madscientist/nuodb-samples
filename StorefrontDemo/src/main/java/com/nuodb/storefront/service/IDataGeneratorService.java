/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service;

import java.io.IOException;
import java.util.List;

import com.nuodb.storefront.model.Product;

public interface IDataGeneratorService {
    /**
     * Generates and saves customers, products, product categories, and products reviews.
     * 
     * @throws IOException
     */
    public void generateAll(int numCustomers, int numProducts, int maxCategoriesPerProduct, int maxReviewsPerProduct) throws IOException;

    /**
     * Saves the specified products along with generated customers and reviews.
     * 
     * @param numCustomers
     *            Number of customers to generate
     * @param products
     *            Products to associate with reviews
     * @param maxReviewsPerProduct
     *            Upper bound on reviews to generate per product in the list. The lower bound is 0, and the exact number is chosen randomly between
     *            this range (inclusive) with equal likelihood. The customer chosen to write each review is randomy selected by the customers
     *            generated by this method. If no customers were generated, no reviews are written.
     */
    public void generateProductReviews(int numCustomers, List<Product> products, int maxReviewsPerProduct) throws IOException;

    /**
     * Removes all database data across all tables.
     */
    public void removeAll() throws IOException;

    /**
     * Closes the underlying database connection. You should call this when you are done using this service.
     */
    public void close();
}