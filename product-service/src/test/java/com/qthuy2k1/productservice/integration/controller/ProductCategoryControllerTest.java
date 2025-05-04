package com.qthuy2k1.productservice.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.productservice.ProductApplication;
import com.qthuy2k1.productservice.dto.request.ProductCategoryRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.ProductCategoryResponse;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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

        String createProductCategoryResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(productCategoryRequest))
                .when().post("/product-categories")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        ApiResponse<ProductCategoryResponse> createProductCategoryApiResponse = objectMapper.readValue(
                createProductCategoryResponseBody,
                new TypeReference<ApiResponse<ProductCategoryResponse>>() {
                }
        );

        assertThat(createProductCategoryApiResponse).isNotNull();
        assertThat(createProductCategoryApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createProductCategoryApiResponse.getResult().getName()).isEqualTo(expectedProductCategoryResponse.getName());
        assertThat(createProductCategoryApiResponse.getResult().getDescription()).isEqualTo(expectedProductCategoryResponse.getDescription());

        String getAllProductCategoriesResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(ContentType.JSON)
                .when().get("/product-categories")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<List<ProductCategoryResponse>> getAllProductCategoriesApiResponse = objectMapper.readValue(
                getAllProductCategoriesResponseBody,
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
        String getAllProductCategoriesResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(ContentType.JSON)
                .when().get("/product-categories")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();
        ApiResponse<List<ProductCategoryResponse>> getAllProductCategoriesApiResponse = objectMapper.readValue(
                getAllProductCategoriesResponseBody,
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
