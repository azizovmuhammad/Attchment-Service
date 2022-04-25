package com.example.Attachement.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

public interface FileStorageService {

    ResponseEntity<?> save(MultipartFile multipartFile);

    ResponseEntity<?> findByHashId(String hashid) throws MalformedURLException;

    ResponseEntity<?> downloadByHashId(String hashid) throws MalformedURLException;

    ResponseEntity<?> deleteByHashId(String hashid);
}
