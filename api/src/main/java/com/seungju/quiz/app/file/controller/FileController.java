package com.seungju.quiz.app.file.controller;

import com.seungju.quiz.app.file.dto.FileDto;
import com.seungju.quiz.app.file.service.FileService;
import com.seungju.quiz.aws.s3.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/file")
@RestController
public class FileController {

  private final FileService fileService;
  private final AwsS3Service awsS3Service;

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> getDetail(@PathVariable("id") Long id) {
    FileDto.Response response = fileService.getDetail(id);

    HttpHeaders httpHeaders = new HttpHeaders();

    ResponseBytes<GetObjectResponse> responseBytes = awsS3Service.getFile(Path.of(response.getServerPath()));

    httpHeaders.setContentType(MediaType.valueOf(responseBytes.response().contentType()));
    httpHeaders.setContentLength(responseBytes.response().contentLength());

    return new ResponseEntity<>(responseBytes.asByteArray(), httpHeaders, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<FileDto.Response> upload(@RequestPart("file") MultipartFile multipartFile) {
    return ResponseEntity.ok(fileService.upload(multipartFile));
  }

  @PostMapping("/create-multipart-upload")
  public ResponseEntity<FileDto.CreateMultipartUploadResponse> initiateMultipartUpload(@RequestBody FileDto.CreateMultipartUpload request) {
    return ResponseEntity.ok(fileService.createMultipartUpload(request));
  }

  @PostMapping("/{id}/upload-part")
  public ResponseEntity<FileDto.UploadPartResponse> uploadPart(
          @PathVariable("id") Long id,
          @RequestPart("file") MultipartFile multipartFile,
          Integer partNumber
  ) {
    return ResponseEntity.ok(fileService.uploadPart(id, multipartFile, partNumber));
  }

  @PostMapping("/{id}/complete-multipart-upload")
  public ResponseEntity<FileDto.Response> uploadPart(
          @PathVariable("id") Long id,
          @RequestBody FileDto.CompleteMultipartUpload request
  ) {
    return ResponseEntity.ok(fileService.completeMultipartUpload(id, request));
  }

}
