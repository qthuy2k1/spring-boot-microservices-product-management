package com.qthuy2k1.productservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.productservice.ProductApplication;
import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProductApplication.class,
        properties = "spring.profiles.active=test"
)
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WithMockUser(username = "usertest", roles = "ADMIN")
public class ProductCategoryControllerTest extends BaseControllerTest {
    @Test
    void createProductCategory() throws Exception {
        ProductCategoryRequest productCategoryRequest = ProductCategoryRequest.builder()
                .name("category 1")
                .description("category description 1")
                .build();
        ProductCategoryResponse expectedProductCategoryResponse = ProductCategoryResponse.builder()
                .name("category 1")
                .description("category description 1")
                .build();

        MvcResult createProductCategoryResult = mockMvc.perform(post("/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productCategoryRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<ProductCategoryResponse> createProductCategoryApiResponse = objectMapper.readValue(
                createProductCategoryResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<ProductCategoryResponse>>() {
                }
        );

        assertThat(createProductCategoryApiResponse).isNotNull();
        assertThat(createProductCategoryApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createProductCategoryApiResponse.getResult().getName()).isEqualTo(expectedProductCategoryResponse.getName());
        assertThat(createProductCategoryApiResponse.getResult().getDescription()).isEqualTo(expectedProductCategoryResponse.getDescription());

        MvcResult getAllCategoriesResult = mockMvc.perform(get("/product-categories"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ApiResponse<List<ProductCategoryResponse>> getAllProductCategoriesApiResponse = objectMapper.readValue(
                getAllCategoriesResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<ProductCategoryResponse>>>() {
                }
        );
        assertThat(getAllProductCategoriesApiResponse).isNotNull();
        assertThat(getAllProductCategoriesApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(getAllProductCategoriesApiResponse.getResult().size()).isEqualTo(2);
        ProductCategoryResponse productCategoryResponse = getAllProductCategoriesApiResponse.getResult().getLast();
        assertThat(productCategoryResponse.getName()).isEqualTo(expectedProductCategoryResponse.getName());
        assertThat(productCategoryResponse.getDescription()).isEqualTo(expectedProductCategoryResponse.getDescription());
    }

    @Test
    void getAllCategories() throws Exception {
        MvcResult getAllCategoriesResult = mockMvc.perform(get("/product-categories"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ApiResponse<List<ProductCategoryResponse>> getAllProductCategoriesApiResponse = objectMapper.readValue(
                getAllCategoriesResult.getResponse().getContentAsByteArray(),
                new TypeReference<ApiResponse<List<ProductCategoryResponse>>>() {
                }
        );
        assertThat(getAllProductCategoriesApiResponse).isNotNull();
        assertThat(getAllProductCategoriesApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(getAllProductCategoriesApiResponse.getResult().size()).isEqualTo(1);
        ProductCategoryResponse productCategoryResponse = getAllProductCategoriesApiResponse.getResult().getFirst();
        assertThat(productCategoryResponse.getName()).isEqualTo(productCategorySaved.getName());
        assertThat(productCategoryResponse.getDescription()).isEqualTo(productCategorySaved.getDescription());
    }
}
