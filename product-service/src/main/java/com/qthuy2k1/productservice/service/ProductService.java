package com.qthuy2k1.productservice.service;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.qthuy2k1.productservice.dto.request.InventoryRequest;
import com.qthuy2k1.productservice.dto.request.ProductRequest;
import com.qthuy2k1.productservice.dto.response.PaginatedResponse;
import com.qthuy2k1.productservice.dto.response.ProductGraphQLResponse;
import com.qthuy2k1.productservice.dto.response.ProductResponse;
import com.qthuy2k1.productservice.enums.ErrorCode;
import com.qthuy2k1.productservice.exception.AppException;
import com.qthuy2k1.productservice.mapper.ProductMapper;
import com.qthuy2k1.productservice.model.ProductCategoryModel;
import com.qthuy2k1.productservice.model.ProductModel;
import com.qthuy2k1.productservice.repository.ProductCategoryRepository;
import com.qthuy2k1.productservice.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements IProductService {
    ProductRepository productRepository;
    ProductCategoryRepository productCategoryRepository;
    KafkaTemplate<String, InventoryRequest> inventoryRequestKafkaTemplate;
    KafkaTemplate<String, List<InventoryRequest>> inventoryRequestListKafkaTemplate;
    ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest productRequest) {
        ProductModel productModel = productMapper.toProduct(productRequest);

        // Get the product category
        ProductCategoryModel productCategory =
                productCategoryRepository.findById(productRequest.getCategoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
        productModel.setCategory(productCategory);

        productModel = productRepository.save(productModel);

        // Create inventory with quantity and product id
        InventoryRequest inventoryRequest = InventoryRequest.builder()
                .quantity(productRequest.getQuantity())
                .productId(productModel.getId())
                .build();

        // !TODO: change requesting to inventory service from OpenFeign to Kafka
//        inventoryClient.createInventory(inventoryRequest);
        inventoryRequestKafkaTemplate.send("create-inventory", inventoryRequest);

        return productMapper.toProductResponse(productModel);
    }

    public PaginatedResponse<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductModel> productPage = productRepository.findAll(pageable);
        List<ProductResponse> productResponses = productPage
                .stream()
                .map(productMapper::toProductResponse)
                .toList();

        PaginatedResponse.Pagination pagination = new PaginatedResponse.Pagination();
        pagination.setTotalRecords(productPage.getTotalElements());
        pagination.setCurrentPage(productPage.getNumber());
        pagination.setTotalPages(productPage.getTotalPages());
        pagination.setNextPage(productPage.hasNext() ? page + 1 : null);
        pagination.setPrevPage(productPage.hasPrevious() ? page - 1 : null);

        return new PaginatedResponse<>(productResponses, pagination);
    }

    public List<ProductGraphQLResponse> getAllProductsGraphQL() {
        return productRepository.findAll().stream().map(productMapper::toProductGraphQLResponse).toList();
    }

    @CachePut(cacheNames = "products", key = "#p0", condition = "#p0!=null", unless = "#result==null")
    public ProductResponse updateProductById(int id, ProductRequest productRequest) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Get the product category
        ProductCategoryModel productCategory =
                productCategoryRepository.findById(productRequest.getCategoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        // Update the product
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(productCategory);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @CacheEvict(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public void deleteProductById(int id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Cacheable(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public ProductResponse getProductById(int id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toProductResponse(product);
    }

    @Cacheable(cacheNames = "products", key = "#p0", condition = "#p0!=null")
    public Boolean isProductExists(int id) {
        return productRepository.existsById(id);
    }


    public ProductGraphQLResponse getProductGraphQLById(int id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));


        return productMapper.toProductGraphQLResponse(product);
    }


    public List<ProductResponse> getProductByListId(Set<Integer> ids) {
        List<ProductModel> productList = productRepository.findAllById(ids);
        if (ids.size() != productList.size()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return productList.stream().map(productMapper::toProductResponse).toList();
    }

    public List<ProductGraphQLResponse> getProductGraphQLByListId(Set<Integer> ids) {
        List<ProductModel> productList = productRepository.findAllById(ids);
        return productList.stream().map(productMapper::toProductGraphQLResponse).toList();
    }

    public void batchInsertProduct(MultipartFile file) throws IOException {
        CSVReader csvReader = new CSVReader(
                new InputStreamReader(
                        new ByteArrayInputStream(file.getBytes())));

        CsvToBean<ProductRequest> csvToBean = new CsvToBeanBuilder(csvReader).withType(ProductRequest.class).build();
        List<ProductRequest> productList = csvToBean.parse();
        List<ProductModel> productModelList = new ArrayList<>();
        for (ProductRequest product : productList) {
            // create inventory with quantity and product id
            ProductCategoryModel productCategory =
                    productCategoryRepository.findById(product.getCategoryId())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
            ProductModel productModel = productMapper.toProduct(product);
            productModel.setCategory(productCategory);
            productModelList.add(productModel);
        }

        List<ProductModel> productSavedList = productRepository.saveAll(productModelList);
        List<InventoryRequest> inventoryRequestList = new ArrayList<>();
        if (productSavedList.size() == productList.size()) {
            for (int i = 0; i < productSavedList.size(); i++) {
                // Collect inventory request to send later
                inventoryRequestList.add(
                        InventoryRequest.builder()
                                .quantity(productList.get(i).getQuantity())
                                .productId(productSavedList.get(i).getId())
                                .build()
                );
            }
        } else {
            throw new RuntimeException("the size between request and response product list isn't the same");
        }

        // Send list of inventory requests to inventory service
        inventoryRequestListKafkaTemplate.send("create-inventory-list", inventoryRequestList);
    }
}
