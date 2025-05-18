package com.qthuy2k1.productservice.integration.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.qthuy2k1.productservice.ProductApplication;
import com.qthuy2k1.productservice.dto.request.InventoryRequest;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.ApiResponse;
import com.qthuy2k1.productservice.dto.response.MessageResponse;
import com.qthuy2k1.productservice.dto.response.PaginatedResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.mapper.ProductCategoryMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProductApplication.class,
        properties = "spring.profiles.active=test"
)
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class ProductControllerTest extends BaseControllerTest {
    private final ProductCategoryMapper productCategoryMapper = Mappers.getMapper(ProductCategoryMapper.class);
    private final DecimalFormat decimalFormatPrice = new DecimalFormat("#.00");
    @MockBean
    KafkaTemplate<String, InventoryRequest> inventoryRequestKafkaTemplate;
    @MockBean
    KafkaTemplate<String, List<InventoryRequest>> inventoryRequestListKafkaTemplate;

    @Test
    void createProduct() throws Exception {
        String token = getAdminToken();
        ProductRequest productRequest = ProductRequest.builder()
                .name("iphone 13")
                .description("description of iphone 13")
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(productCategorySaved.getId())
                .build();
        ProductResponse expectedProductResponse = ProductResponse.builder()
                .name("iphone 13")
                .description("description of iphone 13")
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .category(productCategoryMapper.toProductCategoryResponse(productCategorySaved))
                .build();

        String createProductResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(productRequest))
                .when().post("/products")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().asString();

        // Deserialize the JSON response into ApiResponse<UserResponse>
        ApiResponse<ProductResponse> createProductApiResponse = objectMapper.readValue(
                createProductResponseBody,
                new TypeReference<ApiResponse<ProductResponse>>() {
                }
        );

        assertThat(createProductApiResponse).isNotNull();
        assertThat(createProductApiResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(createProductApiResponse.getResult().getName()).isEqualTo(expectedProductResponse.getName());
        assertThat(createProductApiResponse.getResult().getDescription()).isEqualTo(expectedProductResponse.getDescription());
        assertThat(createProductApiResponse.getResult().getPrice()).isEqualTo(expectedProductResponse.getPrice());
        assertThat(createProductApiResponse.getResult().getSkuCode()).isEqualTo(expectedProductResponse.getSkuCode());
        assertThat(createProductApiResponse.getResult().getUrl()).isEqualTo(expectedProductResponse.getUrl());
        assertThat(createProductApiResponse.getResult().getThumbnail()).isEqualTo(expectedProductResponse.getThumbnail());
        assertThat(createProductApiResponse.getResult().getCategory()).isEqualTo(expectedProductResponse.getCategory());

        String getAllProductsResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().get("/products")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<PaginatedResponse<ProductResponse>> getAllProductsApiResponse = objectMapper.readValue(
                getAllProductsResponseBody,
                new TypeReference<ApiResponse<PaginatedResponse<ProductResponse>>>() {
                }
        );

        assertThat(getAllProductsApiResponse).isNotNull();
        assertThat(getAllProductsApiResponse.getResult().getPagination().getTotalRecords()).isEqualTo(3);
        ProductResponse productCreated = getAllProductsApiResponse.getResult().getData().getLast();
        assertThat(productCreated.getName()).isEqualTo(expectedProductResponse.getName());
        assertThat(productCreated.getDescription()).isEqualTo(expectedProductResponse.getDescription());
        assertThat(productCreated.getPrice()).isEqualTo(decimalFormatPrice.format(expectedProductResponse.getPrice()));
        assertThat(productCreated.getSkuCode()).isEqualTo(expectedProductResponse.getSkuCode());
        assertThat(productCreated.getUrl()).isEqualTo(expectedProductResponse.getUrl());
        assertThat(productCreated.getThumbnail()).isEqualTo(expectedProductResponse.getThumbnail());
        assertThat(productCreated.getCategory()).isEqualTo(expectedProductResponse.getCategory());
    }

    @Test
    void createProduct_ExceptionThrown_InvalidRequest() throws Exception {
        String token = getAdminToken();
        ProductRequest productRequest = ProductRequest.builder()
                .name("abc")
                .description("  ") // only whitespaces
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(1)
                .build();

        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .code(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getCode())
                .message(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getMessage())
                .build();

        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(productRequest))
                .when().post("/products")
                .then().statusCode(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getAllProducts() throws Exception {
        String token = getAdminToken();
        String getAllProductsResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().get("/products")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<PaginatedResponse<ProductResponse>> getAllProductsApiResponse = objectMapper.readValue(
                getAllProductsResponseBody,
                new TypeReference<ApiResponse<PaginatedResponse<ProductResponse>>>() {
                }
        );

        assertThat(getAllProductsApiResponse).isNotNull();
        assertThat(getAllProductsApiResponse.getResult().getPagination().getTotalRecords()).isEqualTo(2);

        ProductResponse productCreated1 = getAllProductsApiResponse.getResult().getData().getFirst();
        assertThat(productCreated1.getId()).isEqualTo(productSaved1.getId());
        assertThat(productCreated1.getName()).isEqualTo(productSaved1.getName());
        assertThat(productCreated1.getDescription()).isEqualTo(productSaved1.getDescription());
        assertThat(productCreated1.getPrice()).isEqualTo(decimalFormatPrice.format(productSaved1.getPrice()));
        assertThat(productCreated1.getSkuCode()).isEqualTo(productSaved1.getSkuCode());
        assertThat(productCreated1.getUrl()).isEqualTo(productSaved1.getUrl());
        assertThat(productCreated1.getThumbnail()).isEqualTo(productSaved1.getThumbnail());
        assertThat(productCreated1.getCategory()).isEqualTo(productCategoryMapper.toProductCategoryResponse(productSaved1.getCategory()));

        ProductResponse productCreated2 = getAllProductsApiResponse.getResult().getData().getLast();
        assertThat(productCreated2.getId()).isEqualTo(productSaved2.getId());
        assertThat(productCreated2.getName()).isEqualTo(productSaved2.getName());
        assertThat(productCreated2.getDescription()).isEqualTo(productSaved2.getDescription());
        assertThat(productCreated2.getPrice()).isEqualTo(decimalFormatPrice.format(productSaved2.getPrice()));
        assertThat(productCreated2.getSkuCode()).isEqualTo(productSaved2.getSkuCode());
        assertThat(productCreated2.getUrl()).isEqualTo(productSaved2.getUrl());
        assertThat(productCreated2.getThumbnail()).isEqualTo(productSaved2.getThumbnail());
        assertThat(productCreated2.getCategory()).isEqualTo(productCategoryMapper.toProductCategoryResponse(productSaved2.getCategory()));
    }

    @Test
    void deleteProductById() throws Exception {
        String token = getAdminToken();
        int id = productSaved1.getId();

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result(MessageResponse.SUCCESS)
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().delete("/products/" + id)
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));

        String getAllProductsResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().get("/products")
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();
        ApiResponse<PaginatedResponse<ProductResponse>> getAllProductsApiResponse = objectMapper.readValue(
                getAllProductsResponseBody,
                new TypeReference<ApiResponse<PaginatedResponse<ProductResponse>>>() {
                }
        );

        assertThat(getAllProductsApiResponse).isNotNull();
        assertThat(getAllProductsApiResponse.getResult().getPagination().getTotalRecords()).isEqualTo(1);

        ProductResponse productCreated = getAllProductsApiResponse.getResult().getData().getFirst();
        assertThat(productCreated.getId()).isEqualTo(productSaved2.getId());
        assertThat(productCreated.getName()).isEqualTo(productSaved2.getName());
        assertThat(productCreated.getDescription()).isEqualTo(productSaved2.getDescription());
        assertThat(productCreated.getPrice()).isEqualTo(decimalFormatPrice.format(productSaved2.getPrice()));
        assertThat(productCreated.getSkuCode()).isEqualTo(productSaved2.getSkuCode());
        assertThat(productCreated.getUrl()).isEqualTo(productSaved2.getUrl());
        assertThat(productCreated.getThumbnail()).isEqualTo(productSaved2.getThumbnail());
        assertThat(productCreated.getCategory()).isEqualTo(productCategoryMapper.toProductCategoryResponse(productSaved2.getCategory()));
    }

    @Test
    void deleteProductById_ExceptionThrown_ProductNotFound() throws Exception {
        String token = getAdminToken();
        int id = productSaved2.getId() + 1;
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.PRODUCT_NOT_FOUND.getCode())
                .message(ErrorCode.PRODUCT_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().delete("/products/" + id)
                .then().statusCode(ErrorCode.PRODUCT_NOT_FOUND.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void updateProductById() throws Exception {
        String token = getAdminToken();
        int id = productSaved1.getId();
        ProductRequest productRequest = ProductRequest.builder()
                .name("Product 1000")
                .description(productSaved1.getDescription())
                .price(productSaved1.getPrice())
                .skuCode(productSaved1.getSkuCode())
                .quantity(1)
                .categoryId(productCategorySaved.getId())
                .build();
        ProductResponse expectProductResponse = ProductResponse.builder()
                .id(id)
                .name("Product 1000")
                .description(productSaved1.getDescription())
                .price(productSaved1.getPrice())
                .skuCode(productSaved1.getSkuCode())
                .category(productCategoryMapper.toProductCategoryResponse(productCategorySaved))
                .build();
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .result(expectProductResponse)
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(productRequest))
                .when().put("/products/" + id)
                .then().statusCode(HttpStatus.OK.value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void updateProductById_ExceptionThrown_InvalidRequest() throws Exception {
        String token = getAdminToken();
        int id = 1;
        ProductRequest productRequest = ProductRequest.builder()
                .name("abc")
                .description("  ") // only whitespaces
                .price(BigDecimal.valueOf(1000))
                .skuCode("abc")
                .quantity(2)
                .categoryId(1)
                .build();

        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder()
                .code(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getCode())
                .message(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(productRequest))
                .when().put("/products/" + id)
                .then().statusCode(ErrorCode.PRODUCT_DESCRIPTION_BLANK.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }

    @Test
    void getProductById() throws Exception {
        String token = getAdminToken();
        int id = productSaved1.getId();
        String getProductResponseBody = given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().get("/products/" + id)
                .then().statusCode(HttpStatus.OK.value())
                .extract().asString();

        ApiResponse<ProductResponse> getProductResponse = objectMapper.readValue(
                getProductResponseBody,
                new TypeReference<ApiResponse<ProductResponse>>() {
                }
        );

        assertThat(getProductResponse).isNotNull();
        assertThat(getProductResponse.getCode()).isEqualTo(DEFAULT_CODE_RESPONSE);
        assertThat(getProductResponse.getResult().getName()).isEqualTo(productSaved1.getName());
        assertThat(getProductResponse.getResult().getDescription()).isEqualTo(productSaved1.getDescription());
        assertThat(getProductResponse.getResult().getPrice())
                .isEqualTo(decimalFormatPrice.format(productSaved1.getPrice()));
        assertThat(getProductResponse.getResult().getSkuCode()).isEqualTo(productSaved1.getSkuCode());
        assertThat(getProductResponse.getResult().getUrl()).isEqualTo(productSaved1.getUrl());
        assertThat(getProductResponse.getResult().getThumbnail()).isEqualTo(productSaved1.getThumbnail());
        assertThat(getProductResponse.getResult().getCategory())
                .isEqualTo(productCategoryMapper.toProductCategoryResponse(productSaved1.getCategory()));
    }

    @Test
    void getUserById_ExceptionThrown_UserNotFound() throws Exception {
        String token = getAdminToken();
        int id = 1;

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.PRODUCT_NOT_FOUND.getCode())
                .message(ErrorCode.PRODUCT_NOT_FOUND.getMessage())
                .build();
        given()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .when().get("/products/" + id)
                .then().statusCode(ErrorCode.PRODUCT_NOT_FOUND.getStatusCode().value())
                .body(equalTo(objectMapper.writeValueAsString(apiResponse)));
    }
}