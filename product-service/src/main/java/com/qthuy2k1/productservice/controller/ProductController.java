package com.qthuy2k1.productservice.controller;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.MessageResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Slf4j
public class ProductController {
    private final IProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductResponse>builder()
                        .result(productService.createProduct(productRequest)).build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok().body(
                ApiResponse.<List<ProductResponse>>builder()
                        .result(products)
                        .build()
        );
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable("id") int id, @RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.ok().body(
                ApiResponse.<ProductResponse>builder()
                        .result(productService.updateProductById(id, productRequest))
                        .build()
        );
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
        List<ProductResponse> products = productService.getProductByListId(
                Arrays.stream(ids.split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toSet())
        );
        return ResponseEntity.ok().body(
                ApiResponse.<List<ProductResponse>>builder()
                        .result(products)
                        .build()
        );
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadProductList(@RequestParam("file") MultipartFile file) throws IOException {
        CSVReader csvReader = new CSVReader(
                new InputStreamReader(
                        new ByteArrayInputStream(file.getBytes())));

        CsvToBean<ProductRequest> csvToBean = new CsvToBeanBuilder(csvReader).withType(ProductRequest.class).build();
        List<ProductRequest> productRequests = csvToBean.parse();

        productService.batchInsertProduct(productRequests);
        return ResponseEntity.ok().body(
                ApiResponse.<String>builder()
                        .result(MessageResponse.SUCCESS)
                        .build()
        );
    }
}
