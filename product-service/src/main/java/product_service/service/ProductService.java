package product_service.service;

import product_service.dto.ProductRequestDTO;
import product_service.dto.ProductResponseDTO;
import product_service.exception.DuplicateSkuException;
import product_service.exception.ProductNotFoundException;
import product_service.entity.Product;
import product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok: Creates constructor with final fields
@Slf4j // Lombok: For logging
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        log.info("Attempting to create product with SKU: {}", productRequestDTO.getSku());
        if (productRepository.existsBySku(productRequestDTO.getSku())) {
            log.warn("Product creation failed: SKU {} already exists", productRequestDTO.getSku());
            throw new DuplicateSkuException("Product with SKU " + productRequestDTO.getSku() + " already exists.");
        }

        Product product = mapToProductEntity(productRequestDTO);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        return mapToProductResponseDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::mapToProductResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        log.info("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", id);
                    return new ProductNotFoundException("Product not found with id: " + id);
                });
        return mapToProductResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductBySku(String sku) {
        log.info("Fetching product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Product not found with SKU: {}", sku);
                    return new ProductNotFoundException("Product not found with SKU: " + sku);
                });
        return mapToProductResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        log.info("Attempting to update product with ID: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product update failed: Product not found with ID: {}", id);
                    return new ProductNotFoundException("Product not found with id: " + id);
                });

        // Check if SKU is being changed and if the new SKU already exists for another product
        if (!existingProduct.getSku().equals(productRequestDTO.getSku())) {
            if(productRepository.existsBySku(productRequestDTO.getSku())) {
                log.warn("Product update failed: New SKU {} already exists for another product", productRequestDTO.getSku());
                throw new DuplicateSkuException("Product with SKU " + productRequestDTO.getSku() + " already exists for another product.");
            }
        }

        existingProduct.setSku(productRequestDTO.getSku());
        existingProduct.setName(productRequestDTO.getName());
        existingProduct.setDescription(productRequestDTO.getDescription());
        existingProduct.setPrice(productRequestDTO.getPrice());
        existingProduct.setCategory(productRequestDTO.getCategory());
        existingProduct.setImageUrl(productRequestDTO.getImageUrl());
        // createdAt is not updated, updatedAt will be handled by @PreUpdate

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return mapToProductResponseDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Attempting to delete product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product deletion failed: Product not found with ID: {}", id);
            throw new ProductNotFoundException("Product not found with id: " + id + " for deletion.");
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    // --- Mappers ---
    private Product mapToProductEntity(ProductRequestDTO dto) {
        return Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .build();
    }

    private ProductResponseDTO mapToProductResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
