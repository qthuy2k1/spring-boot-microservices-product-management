package com.qthuy2k1.productservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.productservice.controller.ProductCategoryController;
import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import com.qthuy2k1.productservice.service.IProductCategoryService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductCategoryController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@WithMockUser(username = "usertest", password = "password", roles = "admin")
public class ProductCategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IProductCategoryService productCategoryService;
    @InjectMocks
    private ProductCategoryController productCategoryController;

    @BeforeEach
    public void setup() {
        //Init MockMvc Object and build
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createProductCategory() throws Exception {
        // given
        ProductCategoryRequest productCategoryRequest = ProductCategoryRequest.builder()
                .name("category 1")
                .description("category description 1")
                .build();
        ProductCategoryResponse productCategoryResponse = ProductCategoryResponse.builder()
                .id(1)
                .name("category 1")
                .description("category description 1")
                .build();

        given(productCategoryService.createProductCategory(productCategoryRequest)).willReturn(productCategoryResponse);

        // when
        ApiResponse<ProductCategoryResponse> apiResponse = ApiResponse.<ProductCategoryResponse>builder()
                .result(productCategoryResponse)
                .build();
        mockMvc.perform(post("/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productCategoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print());

        // then
        then(productCategoryService).should().createProductCategory(productCategoryRequest);
    }

    @Test
    void getAllCategories() throws Exception {
        List<ProductCategoryResponse> productCategoryResponseList = new ArrayList<>();
        productCategoryResponseList.add(
                ProductCategoryResponse.builder()
                        .id(1)
                        .name("Category 1")
                        .description("Category description 1")
                        .build()
        );
        productCategoryResponseList.add(
                ProductCategoryResponse.builder()
                        .id(2)
                        .name("Category 2")
                        .description("Category description 2")
                        .build()
        );

        given(productCategoryService.getAllProductCategories()).willReturn(productCategoryResponseList);

        ApiResponse<List<ProductCategoryResponse>> apiResponse = ApiResponse.<List<ProductCategoryResponse>>builder()
                .result(productCategoryResponseList)
                .build();
        mockMvc.perform(get("/product-categories"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(apiResponse)))
                .andDo(print())
                .andReturn();

        // then
        then(productCategoryService).should().getAllProductCategories();
    }
}
