package com.example.backend1.storage;

import com.example.backend1.common.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;
    private final int maxFilesPerRequest;

    public FileController(
            FileService fileService,
            @org.springframework.beans.factory.annotation.Value("${upload.max-files:5}") int maxFilesPerRequest
    ) {
        this.fileService = fileService;
        this.maxFilesPerRequest = maxFilesPerRequest;
    }

    // 파일 업로드
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<FileUploadResponse>> upload(
            Authentication authentication,
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new com.example.backend1.common.ApiException(
                    com.example.backend1.common.ErrorCode.INVALID_INPUT
            );
        }

        if (files.size() > maxFilesPerRequest) {
            throw new com.example.backend1.common.ApiException(
                    com.example.backend1.common.ErrorCode.INVALID_INPUT
            );
        }

        var res = files.stream()
                .map(f -> fileService.saveUploads(authentication.getName(), f))
                .map(f -> new FileUploadResponse(
                        f.key(),
                        f.url(),
                        f.contentType(),
                        f.sizeBytes()
                ))
                .toList();

        return ApiResponse.ok(res);
    }

    // 파일 다운로드
    @GetMapping("/{key}")
    public ResponseEntity<byte[]> download(
            Authentication authentication,
            @PathVariable String key
    ) {
        var dl = fileService.downloadOwned(authentication.getName(), key);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, dl.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                dl.filename().replace("\"", "") + "\"")
                .body(dl.bytes());
    }

    public record FileUploadResponse(
            String key,
            String url,
            String contentType,
            long sizeBytes
    ) {}
}