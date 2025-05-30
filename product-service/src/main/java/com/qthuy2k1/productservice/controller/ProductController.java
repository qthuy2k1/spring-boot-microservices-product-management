package com.qthuy2k1.productservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.MessageResponse;
import com.qthuy2k1.productservice.dto.response.PaginatedResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Slf4j
public class ProductController {
    private final IProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody @Valid ProductRequest productRequest) throws JsonProcessingException {
        logUserRoles();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductResponse>builder()
                        .result(productService.createProduct(productRequest)).build()
        );
    }

    public void logUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            System.out.println("User: " + username);

            for (GrantedAuthority authority : authentication.getAuthorities()) {
                System.out.println("Role: " + authority.getAuthority());
            }
        } else {
            System.out.println("No authenticated user.");
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size
    ) {
        logUserRoles();
        log.info("PAGE: {}", page);
        log.info("SIZE: {}", size);
        return ResponseEntity.ok().body(
                ApiResponse.<PaginatedResponse<ProductResponse>>builder()
                        .result(productService.getAllProducts(page, size))
                        .build()
        );
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable("id") int id, @RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.ok().body(
                ApiResponse.<ProductResponse>builder()
                        .result(productService.updateProductById(id, productRequest))
                        .build()
        );
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable("id") int id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok().body(
                ApiResponse.<String>builder()
                        .result(MessageResponse.SUCCESS)
                        .build()
        );
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable("id") int id) {
        return ResponseEntity.ok().body(
                ApiResponse.<ProductResponse>builder()
                        .result(productService.getProductById(id))
                        .build()
        );
    }

    @GetMapping("{id}/is-exists")
    public Boolean isProductExists(@PathVariable("id") int id) {
        return productService.isProductExists(id);
    }


    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByListId(@RequestParam("ids") String ids) {
        List<ProductResponse> products = productService.getProductByListId(ids);
        return ResponseEntity.ok().body(
                ApiResponse.<List<ProductResponse>>builder()
                        .result(products)
                        .build()
        );
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<String>> uploadProductList(@RequestParam("file") MultipartFile file) throws IOException {
        productService.batchInsertProduct(file);
        return ResponseEntity.ok().body(
                ApiResponse.<String>builder()
                        .result(MessageResponse.SUCCESS)
                        .build()
        );
    }
}
