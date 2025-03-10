package com.webshop.controllers;

import com.webshop.entities.CustomerOrder;
import com.webshop.exceptions.CartIsEmptyException;
import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String getCart(Model model) {
        CustomerOrder orderInCart = cartService.getCurrentCart();

        Double totalPrice = cartService.calculateTotalPrice(orderInCart);

        model.addAttribute("cart", orderInCart);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    @PostMapping("/add")
    public String addCartItem(@RequestParam("productId") Integer productId,
                              @RequestParam("quantity") Integer quantity,
                              HttpServletRequest request) {
        cartService.addItemToCart(productId, quantity);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam("productId") Integer productId,
                                 @RequestParam("quantity") Integer quantity,
                                 HttpServletRequest request) {
        cartService.updateItemQuantity(productId, quantity);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/remove")
    public String removeCartItem(@RequestParam("productId") Integer productId,
                                 HttpServletRequest request) {
        cartService.removeCartItem(productId);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/checkout")
    public String completeOrder() {
        CustomerOrder completedOrder = cartService.completeOrder();
        return "redirect:/orders/" + completedOrder.getId();
    }

    @ExceptionHandler(CartIsEmptyException.class)
    public String handleCartIsEmptyException(CartIsEmptyException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/cart";
    }
}
