package com.kata.bookstore.service;


import com.kata.bookstore.dto.AddToCartRequest;
import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.OrderItem;
import com.kata.bookstore.entity.OrderStatus;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.InSufficientStockException;
import com.kata.bookstore.exception.InvalidQtyCountException;
import com.kata.bookstore.exception.QtyNotAvailableException;
import com.kata.bookstore.exception.ResourceNotFoundException;
import com.kata.bookstore.repository.BookOrderRepository;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.repository.CartRepository;
import com.kata.bookstore.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookOrderRepository bookOrderRepository;
    private final BookRepository bookRepository;
    public CartService(CartRepository cartRepository,UserRepository userRepository,
                       BookOrderRepository bookOrderRepository,
                       BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookOrderRepository = bookOrderRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Cart addBookToCart(AddToCartRequest addToCartRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book book = bookRepository.findById(addToCartRequest.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        if (book.getStock() <= 0) {
            throw new IllegalStateException("Book is out of stock: " + book.getTitle());
        }
        Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() ->cartRepository.save(Cart.builder().user(user).build()));
        Optional<CartItem> existingItem = cart.getItems().stream().filter(item -> item.getBook().getId().equals(book.getId())).findFirst();
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + addToCartRequest.getQuantity();
            if (newQuantity > book.getStock()) {
                throw new QtyNotAvailableException("Requested quantity exceeds available stock");
            }
            item.setQuantity(item.getQuantity() + addToCartRequest.getQuantity());
        } else {
            cart.getItems().add(CartItem.builder().cart(cart).book(book).quantity(addToCartRequest.getQuantity()).build());
        }
        return cartRepository.save(cart);
    }

    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() ->
                Cart.builder().user(user).build());
    }

    public Cart updateCartItemQuantity(User user, Long bookId, int qty) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));
        CartItem cartItem = cart.getItems().stream().filter(item ->
                        item.getBook().getId().equals(bookId)).findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found in cart: " + bookId));
        if (qty <= 0) {
            throw new InvalidQtyCountException("Quantity must be greater than zero");
        }
        if (qty > cartItem.getBook().getStock()) {
            throw new QtyNotAvailableException("Requested quantity exceeds available stock");
        }
        cartItem.setQuantity(qty);
        return cartRepository.save(cart);
    }

    public Cart removeBookFromCart(User user, Long bookId) {
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));
        CartItem cartItem = cart.getItems()
                .stream().filter(item ->item.getBook().getId().equals(bookId))
                .findFirst().orElseThrow(() ->
                        new ResourceNotFoundException("Book not found in cart: " + bookId));
        cart.getItems().remove(cartItem);
        return cartRepository.save(cart);
    }

    public BookOrder checkout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        BookOrder bookOrder = BookOrder.builder().user(user).status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now()).build();
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getQuantity() > cartItem.getBook().getStock()) {
                throw new InSufficientStockException("Insufficient stock");
            }
            OrderItem orderItem = OrderItem.builder().order(bookOrder).
                    book(cartItem.getBook())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getBook().getPrice())
                    .build();
            bookOrder.getItems().add(orderItem);
            int remainingStock = cartItem.getBook().getStock() - cartItem.getQuantity();
            cartItem.getBook().setStock(remainingStock);
            bookRepository.save(cartItem.getBook());
        }
        BigDecimal totalAmount = cart.getItems().stream().map(item -> item.getBook().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        bookOrder.setTotalAmount(totalAmount);
        bookOrderRepository.save(bookOrder);
        if(!cart.getItems().isEmpty()) {
            cart.getItems().clear();
        }
        return bookOrder;
    }
}
