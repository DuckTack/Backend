package com.example.backend1.admin.service;

import com.example.backend1.admin.dto.AdminProductDtos;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.product.domain.Product;
import com.example.backend1.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;

    @Transactional
    public AdminProductDtos.Response create(AdminProductDtos.CreateRequest req) {
        Product p = new Product(req.name(), req.productId(), req.coupangUrl(), req.imageUrl(), req.category());
        return toResponse(productRepository.save(p));
    }

    @Transactional(readOnly = true)
    public Page<AdminProductDtos.ListItem> list(Pageable pageable) {
        return productRepository.findAll(pageable).map(p ->
                new AdminProductDtos.ListItem(p.getId(), p.getName(), p.getCategory(), p.isActive())
        );
    }

    @Transactional
    public AdminProductDtos.Response update(Long id, AdminProductDtos.UpdateRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND)); // 적절한 에러코드 사용

        p.updateFrom(req.name(), req.productId(), req.coupangUrl(), req.imageUrl(), req.category(), req.active());
        return toResponse(productRepository.save(p));
    }

    private AdminProductDtos.Response toResponse(Product p) {
        return new AdminProductDtos.Response(
                p.getId(), p.getName(), p.getProductId(), p.getCoupangUrl(),
                p.getImageUrl(), p.getCategory(), p.isActive(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}