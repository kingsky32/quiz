package com.seungju.quiz.aws.s3.service;

import com.seungju.quiz.aws.s3.adapter.S3ObjectMultipartFileAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final S3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public PutObjectResponse uploadFile(MultipartFile multipartFile, Path path) throws IOException {
        return this.uploadFile("files", multipartFile, path);
    }

    public PutObjectResponse uploadFile(String key, MultipartFile multipartFile, Path path) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key + "/" + path.getParent() + "/" + path.getFileName().toString())
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();

        return s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
    }

    public PutObjectResponse uploadFile(byte[] bytes, Path path) {
        return uploadFile("files", bytes, path);
    }

    public PutObjectResponse uploadFile(String key, byte[] bytes, Path path) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key + "/" + path.getParent() + "/" + path.getFileName().toString())
                .build();

        return s3Client.putObject(putObjectRequest, RequestBody.fromByteBuffer(ByteBuffer.wrap(bytes)));
    }

    public void uploadUnzippedFilesToS3(String key, byte[] zipFileBytes, Path path) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipFileBytes);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        byte[] buffer = new byte[1024];
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            int length;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((length = zipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            String entryName = entry.getName();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key + "/" + path.getParent() + "/" + path.getFileName().toString() + "/" + entryName)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(outputStream.toByteArray()));
        }
    }

    public ResponseBytes<GetObjectResponse> getFile(Path path) {
        return getFile("files", path);
    }

    public ResponseBytes<GetObjectResponse> getFile(String key, Path path) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key + "/" + path.getParent() + "/" + path.getFileName().toString())
                .build();

        return s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
    }

    public MultipartFile getMultipartFile(Path path) {
        String objectKey = "files/" + path.getParent() + "/" + path.getFileName().toString();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseBytes<GetObjectResponse> object = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());

        return new S3ObjectMultipartFileAdapter(
                objectKey,
                path.getFileName().toString(),
                object.response().contentType(),
                object.asByteArray()
        );
    }

    public CreateMultipartUploadResponse createMultipartUpload(Path path) {
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key("files/" + path.getParent() + "/" + path.getFileName().toString())
                .build();
        return s3Client.createMultipartUpload(createMultipartUploadRequest);
    }

    public UploadPartResponse uploadPart(Path path, String uploadId, MultipartFile multipartFile, Integer partNumber) throws IOException {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key("files/" + path.getParent() + "/" + path.getFileName().toString())
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        return s3Client.uploadPart(uploadPartRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
    }

    public CompleteMultipartUploadResponse completeMultipartUpload(Path path, String uploadId, List<CompletedPart> completedParts) {
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(completedParts).build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key("files/" + path.getParent() + "/" + path.getFileName().toString())
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();

        return s3Client.completeMultipartUpload(completeMultipartUploadRequest);
    }

}
