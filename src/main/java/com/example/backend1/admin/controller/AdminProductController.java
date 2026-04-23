package com.example.backend1.admin.controller;

import com.example.backend1.admin.dto.AdminProductDtos;
import com.example.backend1.admin.service.AdminProductService;
import com.example.backend1.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Product")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "물품 수동 등록")
    @PostMapping
    public ApiResponse<AdminProductDtos.Response> create(
            @RequestBody @jakarta.validation.Valid AdminProductDtos.CreateRequest req
    ) {
        return ApiResponse.ok(adminProductService.create(req));
    }

    @Operation(summary = "물품 전체 목록 (페이징)")
    @GetMapping
    public ApiResponse<Page<AdminProductDtos.ListItem>> list(Pageable pageable) {
        return ApiResponse.ok(adminProductService.list(pageable));
    }

    @Operation(summary = "물품 수정")
    @PutMapping("/{id}")
    public ApiResponse<AdminProductDtos.Response> update(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid AdminProductDtos.UpdateRequest req
    ) {
        return ApiResponse.ok(adminProductService.update(id, req));
    }
}