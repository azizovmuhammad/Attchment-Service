package com.example.Attachement.service.impl;

import com.example.Attachement.entity.FileStorage;
import com.example.Attachement.enums.FileStorageStatus;
import com.example.Attachement.repository.FileStorageRepository;
import com.example.Attachement.service.FileStorageService;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Date;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${upload.server.folder}")
    private String serverFolderPath;

    private final Hashids hashids;

    private final FileStorageRepository fileStorageRepository;

    public FileStorageServiceImpl(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
        this.hashids = new Hashids(getClass().getName(), 6);
    }

    @Override
    public ResponseEntity<?> save(MultipartFile multipartFile){

        FileStorage fileStorage = new FileStorage();
        fileStorage.setName(multipartFile.getOriginalFilename());
        fileStorage.setFileSize(multipartFile.getSize());
        fileStorage.setContentType(multipartFile.getContentType());
        fileStorage.setExtension(getExt(multipartFile.getOriginalFilename()));
        fileStorage.setFileStorageStatus(FileStorageStatus.DRAFT);

        fileStorage = fileStorageRepository.save(fileStorage);

        // /serverFolderPath/upload_folder/2002/02/20/hash.pdf

        Date now = new Date();

        // 1 -----> the first way - bunday holatda ham path ni yozishimiz mumkin
       /* File uploadFolder = new File(this.serverFolderPath + "/upload_files" +
                1900 + now.getYear() + "/" +
                1 + now.getMonth() + "/" +
                now.getDate());*/

        // 2 -----> the second way - string formatter orqali ham yozishimiz mumkin
        String path = String.format("%s/upload_files/%d/%d/%d",
                this.serverFolderPath,
                1900 + now.getYear(),
                1 + now.getMonth(),
                now.getDate()
                );

        File  uploadFolder = new File(path);
        if (!uploadFolder.exists() && uploadFolder.mkdirs()){
            System.out.println("file created");
        }

        fileStorage.setHashId(hashids.encode(fileStorage.getId()));
        String pathLocal = String.format("/upload_files/%d/%d/%d/%s.%s",
                1900 + now.getYear(),
                1 + now.getMonth(),
                now.getDate(),
                fileStorage.getHashId(),
                fileStorage.getExtension()
        );

        fileStorage.setUploadFolder(pathLocal);

        fileStorageRepository.save(fileStorage);

        uploadFolder = uploadFolder.getAbsoluteFile();
        File uploadFile = new File(uploadFolder, String.format("%s.%s", fileStorage.getHashId(), fileStorage.getExtension()));

        try {
            multipartFile.transferTo(uploadFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(fileStorage);
    }

    @Override
    public ResponseEntity<?> findByHashId(String hashid) throws MalformedURLException {
        FileStorage fileStorage = fileStorageRepository.findByHashId(hashid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename+\""+ UriEncoder.encode(fileStorage.getName()))
                .contentType(MediaType.parseMediaType(fileStorage.getContentType()))
                .contentLength(fileStorage.getFileSize())
                .body(new FileUrlResource(String.format("%s/%s", this.serverFolderPath, fileStorage.getUploadFolder())));
    }

    @Override
    public ResponseEntity<?> downloadByHashId(String hashid) throws MalformedURLException {
        FileStorage fileStorage = fileStorageRepository.findByHashId(hashid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename+\""+ UriEncoder.encode(fileStorage.getName()))
                .contentType(MediaType.parseMediaType(fileStorage.getContentType()))
                .contentLength(fileStorage.getFileSize())
                .body(new FileUrlResource(String.format("%s/%s", this.serverFolderPath, fileStorage.getUploadFolder())));
    }

    @Override
    public ResponseEntity<?> deleteByHashId(String hashid) {
        FileStorage fileStorage = fileStorageRepository.findByHashId(hashid);
        File file = new File(String.format("%s/%s", this.serverFolderPath, fileStorage.getUploadFolder()));
        if (file.delete()){
            fileStorageRepository.delete(fileStorage);
        }
        return ResponseEntity.ok("Successfully deleted");
    }

    private String getExt(String fileName){
        // photo.jpg
        String ext = null;

        if (fileName != null && !fileName.isEmpty()){
            int dot = fileName.lastIndexOf(".");
            if (dot>0 && dot <= fileName.length()-2){
                ext = fileName.substring(dot + 1);
            }
        }
        return ext;
    }
}
