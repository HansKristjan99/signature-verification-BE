package com.vericode.signit.storage.S3;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vericode.signit.AccessType;
import com.vericode.signit.S3ObjectInputStreamWrapper;
import com.vericode.signit.storage.StorageService;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service implements StorageService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Autowired
    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

  /**
     * Generates a presigned URL for GET or PUT operations with specified access type.
     */
    public String generatePreSignedUrl(String filePath, SdkHttpMethod method, AccessType accessType) {
        if (method == SdkHttpMethod.GET) {
            return generateGetPresignedUrl(filePath);
        } else if (method == SdkHttpMethod.PUT) {
            return generatePutPresignedUrl(filePath, accessType);
        } else {
            throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }
    }

    /**
     * Generates a presigned GET URL.
     */
    private String generateGetPresignedUrl(String filePath) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        // you can change expiration time here
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Generates a presigned PUT URL with optional ACL based on AccessType.
     */
    private String generatePutPresignedUrl(String filePath, AccessType accessType) {
        
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                .putObjectRequest(objectRequest)
                .build();


        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String myURL = presignedRequest.url().toString();
        System.out.println(myURL);


        return presignedRequest.url().toExternalForm();
    }
    
    /**
     * Uploads a MultipartFile to S3 with specified access type.
     */
    public PutObjectResponse uploadMultipartFile(MultipartFile file, AccessType accessType) throws IOException {
        String fileName = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName);

            if (accessType == AccessType.PUBLIC) {
                putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();

            return s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        }
    }

    /**
     * Downloads a file from S3 and returns an InputStream and ETag.
     */
    public S3ObjectInputStreamWrapper downloadFile(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        ResponseInputStream<GetObjectResponse> s3ObjectResponse = s3Client.getObject(getObjectRequest);
        String eTag = s3ObjectResponse.response().eTag();
        return new S3ObjectInputStreamWrapper(s3ObjectResponse, eTag);
    }

    @Override
    public void init() {
      System.out.println("Initializing S3 storage service..."); // Placeholder for any initialization logic
    }

    @Override
    public String store(MultipartFile file) {
        try {
            PutObjectResponse putObjectResponse = this.uploadMultipartFile(file, AccessType.PRIVATE);
            return putObjectResponse.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        }
    }

    @Override
    public Stream<Path> loadAll() {
        return s3Client.listObjectsV2Paginator(b -> b.bucket(bucketName))
                .contents()
                .stream()
                .map(s3Object -> Path.of(s3Object.key()));
    }

    @Override
    public Path load(String filename) {
        // Download the file from S3 and save it to a temporary file, then return its Path
        S3ObjectInputStreamWrapper inputStreamWrapper = downloadFile(filename);
        try (InputStream inputStream = inputStreamWrapper.inputStream()) {
            // Create a temp file with the same name as the S3 object
            Path tempFile = java.nio.file.Files.createTempFile("s3-", "-" + filename);
            java.nio.file.Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file from S3: " + filename, e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        S3ObjectInputStreamWrapper inputStreamWrapper = downloadFile(filename);
        return new org.springframework.core.io.InputStreamResource(inputStreamWrapper.inputStream());
    }

    @Override
    public void deleteAll() {
        s3Client.listObjectsV2Paginator(b -> b.bucket(bucketName))
            .contents()
            .forEach(s3Object -> s3Client.deleteObject(builder -> builder.bucket(bucketName).key(s3Object.key())));
    }
}