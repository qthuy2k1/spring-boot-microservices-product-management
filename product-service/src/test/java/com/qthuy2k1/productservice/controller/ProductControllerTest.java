package com.qthuy2k1.productservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.MessageResponse;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.exception.AppException;
import com.qthuy2k1.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@WithMockUser(username = "usertest", password = "password", roles = "ADMIN")
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ProductService productService;
    @InjectMocks
    private ProductController productController;

    @BeforeEach
    public void setup() {
        //Init MockMvc Object and build
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createProduct() throws Exception {
        // given
        ProductRequest productRequest = ProductRequest.builder()
                .name("iphone 13")
                .description("description of iphone 13")
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(1)
                .build();
        ProductCategoryResponse productCategoryResponse = ProductCategoryResponse.builder()
                .id(1)
                .name("category 1")
                .description("category description 1")
                .build();
        ProductResponse productResponse = ProductResponse.builder()
                .id(1)
                .name("iphone 13")
                .description("description of iphone 13")
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .category(productCategoryResponse)
                .build();

        given(productService.createProduct(productRequest)).willReturn(productResponse);

        // when
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(productResponse)
                .build();
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should().createProduct(productRequest);
    }

    @Test
    void createProduct_ExceptionThrown_InvalidRequest() throws Exception {
        // given
        ProductRequest productRequest = ProductRequest.builder()
                .name("  ") // only whitespaces
                .description("  ") // only whitespaces
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(1)
                .build();


        // when
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .code(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getCode())
                .message(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getMessage())
                .build();
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should(never()).createProduct(any());
    }


    @Test
    void getAllProducts() throws Exception {
        // given
        List<ProductResponse> products = new ArrayList<>();
        ProductCategoryResponse productCategoryResponse = ProductCategoryResponse.builder()
                .id(1)
                .name("category 1")
                .description("description of category 1")
                .build();

        products.add(
                ProductResponse.builder()
                        .name("iphone 13")
                        .description("description of iphone 13")
                        .price(BigDecimal.valueOf(1000))
                        .category(productCategoryResponse)
                        .build()
        );
        products.add(
                ProductResponse.builder()
                        .name("iphone 12")
                        .description("description of iphone 12")
                        .price(BigDecimal.valueOf(900))
                        .category(productCategoryResponse)
                        .build()
        );
        given(productService.getAllProducts()).willReturn(products);


        // when
        ApiResponse<List<ProductResponse>> apiResponse = ApiResponse.<List<ProductResponse>>builder()
                .result(products)
                .build();
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print())
                .andReturn();

        // then
        then(productService).should().getAllProducts();
    }

    @Test
    void deleteProductById() throws Exception {
        // given
        int id = 1;

        // when
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        mockMvc.perform(delete("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should().deleteProductById(id);
    }

    @Test
    void deleteProductById_ExceptionThrown_ProductNotFound() throws Exception {
        // given
        int id = 1;
        doThrow(new AppException(ErrorCode.PRODUCT_NOT_FOUND))
                .when(productService).deleteProductById(id);

        // when
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.PRODUCT_NOT_FOUND.getCode())
                .message(ErrorCode.PRODUCT_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(delete("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        verify(productService).deleteProductById(id);
    }

    @Test
    void updateProductById() throws Exception {
        // given
        int id = 1;
        ProductRequest productRequest = ProductRequest.builder()
                .name("iphone 14")
                .description("description of iphone 14")
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(1)
                .build();
        ProductCategoryResponse productCategoryResponse = ProductCategoryResponse.builder()
                .id(1)
                .name("category 1")
                .description("category description 1")
                .build();
        ProductResponse productResponse = ProductResponse.builder()
                .id(1)
                .name("iphone 14")
                .description("description of iphone 14")
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .category(productCategoryResponse)
                .build();

        given(productService.updateProductById(id, productRequest)).willReturn(productResponse);

        // when
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(productResponse)
                .build();
        mockMvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should().updateProductById(id, productRequest);
    }

    @Test
    void updateProductById_ExceptionThrown_InvalidRequest() throws Exception {
        // given
        int id = 1;
        ProductRequest productRequest = ProductRequest.builder()
                .name("  ") // only whitespaces
                .description("  ") // only whitespaces
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(1)
                .build();

        // when
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .code(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getCode())
                .message(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getMessage())
                .build();
        mockMvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should(never()).updateProductById(id, productRequest);
    }

    @Test
    void getProductById() throws Exception {
        // given
        int id = 1;
        ProductCategoryResponse productCategoryResponse = ProductCategoryResponse.builder()
                .id(1)
                .name("category 1")
                .description("description of category 1")
                .build();
        ProductResponse productResponse = ProductResponse.builder()
                .name("iphone 12")
                .description("description of iphone 12")
                .price(BigDecimal.valueOf(900))
                .category(productCategoryResponse)
                .build();

        given(productService.getProductById(id)).willReturn(productResponse);

        // when
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(productResponse)
                .build();
        mockMvc.perform(get("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should().getProductById(id);
    }

    @Test
    void getUserById_ExceptionThrown_UserNotFound() throws Exception {
        // given
        int id = 1;

        given(productService.getProductById(id)).willThrow(new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // when
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.PRODUCT_NOT_FOUND.getCode())
                .message(ErrorCode.PRODUCT_NOT_FOUND.getMessage())
                .build();
        mockMvc.perform(get("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productService).should().getProductById(id);
    }
}