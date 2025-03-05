package com.webshop.controllers;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.OrderStatus;
import com.webshop.entities.Product;
import com.webshop.services.CartService;
import com.webshop.services.CustomerOrderService;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/web-shop")
public class ShopController {

    private final ProductService productService;
    private final CartService cartService;
    private final CustomerOrderService customerOrderService;

    @GetMapping("/products/{id}")
    public String getProduct(@PathVariable("id") Integer productId, Model model) {
        Product product = productService.getProductById(productId);
        model.addAttribute("product", product);

        CustomerOrder cart = cartService.getCurrentCart();
        OrderItem cartItem = cartService.findCartItemByProductId(cart, productId);
        model.addAttribute("cartQuantity", cartItem != null ? cartItem.getQuantity() : 0);

        return "product";
    }

    @GetMapping("/products")
    public String getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "4") int size,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "asc") String sort,
        Model model) {

        Page<Product> products = productService.getProducts(title, minPrice, maxPrice, sort, page, size);

        Map<Integer, Integer> cartProductsQuantities = cartService.getCartProductsQuantity();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("title", title);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("cartProductsQuantities", cartProductsQuantities);

        return "showcase";
    }

    @GetMapping
    public String getCart(Model model) {
        CustomerOrder cart = cartService.getCurrentCart();

        Double totalPrice = cartService.calculateTotalPrice(cart);

        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    @PostMapping("/cart/add")
    public String addCartItem(@RequestParam("productId") Integer productId,
                              @RequestParam("quantity") Integer quantity) {
        cartService.addItemToCart(productId, quantity);
        return "cart";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("productId") Integer productId,
                                 @RequestParam("quantity") Integer quantity) {
        cartService.updateItemQuantity(productId, quantity);
        return "cart";
    }

    @PostMapping("/cart/remove")
    public String removeCartItem(@RequestParam("productId") Integer productId) {
        cartService.removeCartItem(productId);
        return "cart";
    }

    @PostMapping("/cart/checkout")
    public String completed() {
        CustomerOrder completedOrder = cartService.completeOrder();
        return "redirect:/web-shop/orders/" + completedOrder.getId();
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable("id") Integer orderId, Model model) {
        CustomerOrder order = customerOrderService.getOrderById(orderId);

        //ToDo
        if (order == null || !order.getStatus().equals(OrderStatus.COMPLETED)) {
            model.addAttribute("error", "Оформленный заказ не найден.");
            return "error";
        }

        model.addAttribute("order", order);
        return "order";
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model) {

        List<CustomerOrder> completedOrders = customerOrderService.getCompletedOrders();

        Double totalPrice = customerOrderService.getTotalPriceOfCompletedOrders();

        model.addAttribute("orders", completedOrders);
        model.addAttribute("totalPrice", totalPrice);

        return "orders";
    }
}
