package com.qthuy2k1.productservice.service;

//@ExtendWith(MockitoExtension.class)
//public class ProductServiceTest {
//    @Mock
//    private ProductRepository productRepository;
//    @Mock
//    private ProductCategoryService productCategoryService;
//    @Mock
//    private WebClient.Builder webClientBuilder;
//    private ProductService underTest;
//
//    @BeforeEach
//    void setUp() {
//        underTest = new ProductService(productRepository, productCategoryService, webClientBuilder);
//    }
//
//
//    @Test
//    void createProduct() throws NotFoundException {
//        // Given
//        ProductRequest product = new ProductRequest(
//                "Product 1",
//                "Product description 1",
//                BigDecimal.valueOf(1),
//                1,
//                "abc",
//                1
//        );
//        ProductCategoryResponse productCategoryResponse = new ProductCategoryResponse(1, "abc", "abc");
//
//        given(productCategoryService.getProductCategoryById(product.getCategoryId())).willReturn(productCategoryResponse);
//
//
//        // When
//        underTest.createProduct(product);
//
//        // Then
//        ArgumentCaptor<ProductModel> productArgumentCaptor = ArgumentCaptor.forClass(ProductModel.class);
//        verify(productRepository).save(productArgumentCaptor.capture());
//
//        ProductModel capturedProduct = productArgumentCaptor.getValue();
//
//        assertThat(capturedProduct.getName()).isEqualTo(product.getName());
//        assertThat(capturedProduct.getDescription()).isEqualTo(product.getDescription());
//        assertThat(capturedProduct.getPrice()).isEqualTo(product.getPrice());
////        assertThat(capturedProduct.getCategory().getId()).isEqualTo(product.getCategoryId());
//    }
//
//    @Test
//    void getAllProducts() {
//        // When
//        List<ProductResponse> products = underTest.getAllProducts();
//
//        // Then
//        verify(productRepository).findAll();
//    }
//
//    @Test
//    void deleteProductById() throws NotFoundException {
//        // given
//        ProductCategoryModel productCategory = ProductCategoryModel.builder()
//                .id(1)
//                .name("category 1")
//                .description("description 1")
//                .build();
//        ProductModel product = ProductModel.builder()
//                .id(1)
//                .name("product 1")
//                .description("des 1")
//                .price(BigDecimal.valueOf(1))
//                .category(productCategory)
//                .build();
//        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
//
//        // When
//        underTest.deleteProductById(product.getId());
//
//        // Then
//        verify(productRepository).delete(product);
//    }
//
//    @Test
//    void deleteProductById_ExceptionThrown_ProductNotFound() {
//        // given
//        Integer id = 1;
//        given(productRepository.findById(id)).willReturn(Optional.empty());
//
//        // When
//        assertThatThrownBy(() ->
//                underTest.deleteProductById(id))
//                .hasMessageContaining("Product not found");
//
//        // Then
//        verify(productRepository, never()).delete(any());
//    }
//
//    @Test
//    void getProductById() throws NotFoundException {
//        // given
//        Integer id = 1;
//        ProductCategoryModel productCategory = ProductCategoryModel.builder()
//                .id(1)
//                .name("category 1")
//                .description("description 1")
//                .build();
//        ProductModel productModel = ProductModel.builder()
//                .id(1)
//                .name("product 1")
//                .description("des 1")
//                .price(BigDecimal.valueOf(1))
//                .category(productCategory)
//                .build();
//        given(productRepository.findById(id)).willReturn(Optional.of(productModel));
//
//        // When
//        ProductResponse product = underTest.getProductById(id);
//
//        // Then
//        verify(productRepository).findById(id);
//    }
//
//    @Test
//    void getProductById_ExceptionThrown_ProductNotFound() {
//        // given
//        Integer id = 1;
//        given(productRepository.findById(id)).willReturn(Optional.empty());
//
//        // When
//        assertThatThrownBy(() ->
//                underTest.getProductById(id))
//                .hasMessageContaining("Product not found");
//
//        // Then
//        verify(productRepository).findById(any());
//    }
//
//}
