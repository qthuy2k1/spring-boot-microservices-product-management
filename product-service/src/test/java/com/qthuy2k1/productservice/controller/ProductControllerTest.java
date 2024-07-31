package com.qthuy2k1.productservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
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


        String productRequestString = objectMapper.writeValueAsString(productRequest);

        // when
        // then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success"))
                .andDo(print());
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


        String productRequestString = objectMapper.writeValueAsString(productRequest);

        // when
        // then
        String error = "{\"name\":\"the product name shouldn't be blank\"," +
                "\"description\":\"the product description shouldn't be blank\"}";
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(error))
                .andDo(print());
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
        MvcResult result = mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(productService).getAllProducts();

        assertThat(result.getResponse().getContentAsString()).
                isEqualTo(objectMapper.writeValueAsString(products));
    }

    @Test
    void deleteProductById() throws Exception {
        // given
        int id = 1;

        // when
        mockMvc.perform(delete("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"))
                .andDo(print());

        // then
        verify(productService).deleteProductById(any());
    }

    @Test
    void deleteProductById_ExceptionThrown_ProductNotFound() throws Exception {
        // given
        int id = 1;
        doThrow(new AppException(ErrorCode.PRODUCT_NOT_FOUND))
                .when(productService).deleteProductById(id);

        // when
        String error = "{\"error\":\"Product not found\"}";
        mockMvc.perform(delete("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(error))
                .andDo(print());

        // then
        verify(productService).deleteProductById(any());
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
        String userRequestString = objectMapper.writeValueAsString(productRequest);

        // when
        mockMvc.perform(put("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestString))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"))
                .andDo(print());

        // then
        verify(productService).updateProductById(id, productRequest);
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
        String productRequestString = objectMapper.writeValueAsString(productRequest);

        // when
        String error = "{\"name\":\"the product name shouldn't be blank\",\"description\":\"the product description shouldn't be blank\"}";
        mockMvc.perform(put("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(error))
                .andDo(print());

        // then
        verify(productService, never()).updateProductById(any(), any());
    }

    @Test
    void getProductById() throws Exception {
        // given
        Integer id = 1;
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
        String productResponseString = objectMapper.writeValueAsString(productResponse);

        given(productService.getProductById(id)).willReturn(productResponse);

        // when
        mockMvc.perform(get("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(productResponseString))
                .andDo(print());

        // then
        verify(productService).getProductById(id);
    }

    @Test
    void getUserById_ExceptionThrown_UserNotFound() throws Exception {
        // given
        Integer id = 1;

        given(productService.getProductById(id)).willThrow(new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // when
        String error = "{\"error\":\"Product not found\"}";
        mockMvc.perform(get("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(error))
                .andDo(print());

        // then
        verify(productService).getProductById(id);
    }
}