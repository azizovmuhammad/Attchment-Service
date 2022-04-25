package com.example.Attachement.controller;

import com.example.Attachement.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileStorageController {

    @Value("${upload.server.folder}")
    private String serverFolderPath;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile multipartFile){
        return fileStorageService.save(multipartFile);
    }

    @GetMapping("/file-preview/{hashid}")
    public ResponseEntity<?> preview(@PathVariable String hashid) throws MalformedURLException {
        return fileStorageService.findByHashId(hashid);
    }

    @GetMapping("/download/{hashid}")
    public ResponseEntity<?> download(@PathVariable String hashid) throws MalformedURLException {
        return fileStorageService.downloadByHashId(hashid);
    }

    @DeleteMapping("/delete/{hashid}")
    public ResponseEntity<?> delete(@PathVariable String hashid){
        return fileStorageService.deleteByHashId(hashid);
    }
}
