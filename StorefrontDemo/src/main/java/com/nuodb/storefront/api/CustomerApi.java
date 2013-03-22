package com.nuodb.storefront.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.Cart;
import com.nuodb.storefront.model.CartSelection;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Transaction;

@Path("/customer")
public class CustomerApi extends BaseApi {
    public CustomerApi() {
    }

    @GET
    @Path("/cart")
    @Produces(MediaType.APPLICATION_JSON)
    public Cart getCartSelections(@Context HttpServletRequest req) {
        Customer customer = getOrCreateCustomer(req);
        return getService().getCustomerCart(customer.getId());
    }

    @PUT
    @Path("/cart")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult<CartSelection> addToCart(@Context HttpServletRequest req, @FormParam("productId") int productId, @FormParam("quantity") int quantity) {
        Customer customer = getOrCreateCustomer(req);
        int itemCount = getService().addToCart(customer.getId(), productId, quantity);

        SearchResult<CartSelection> result = new SearchResult<CartSelection>();
        result.setTotalCount(itemCount);
        return result;
    }

    @POST
    @Path("/checkout")
    @Produces(MediaType.APPLICATION_JSON)
    public Transaction purchase(@Context HttpServletRequest req) {
        Customer customer = getOrCreateCustomer(req);
        return getService().checkout(customer.getId());
    }
}
