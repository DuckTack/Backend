package com.example.backend1.storage;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storage")
public class StorageController {

    private final FileStorage storage;

    public StorageController(FileStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/**")
    public ResponseEntity<byte[]> download(HttpServletRequest request) {

        String path = request.getRequestURI()
                .replaceFirst("/storage/", "");

        byte[] file = storage.load(path);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=\"report.pdf\"")
                .body(file);
    }
}