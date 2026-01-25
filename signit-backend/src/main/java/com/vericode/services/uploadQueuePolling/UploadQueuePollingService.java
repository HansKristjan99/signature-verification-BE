package com.vericode.services.uploadQueuePolling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vericode.data.File.FileEntity;
import com.vericode.data.File.FileRepository;
import com.vericode.data.UploadSession.UploadSessionEntity;
import com.vericode.data.UploadSession.UploadSessionRepository;
import com.vericode.signit.storage.S3.S3Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
class UploadQueuePollingService {

    private static final Logger logger = LoggerFactory.getLogger(UploadQueuePollingService.class);

    // Pattern to extract uploadSessionId from S3 key
    // Expected format: uploads/{UUID}/{filename}
    private static final Pattern UPLOAD_KEY_PATTERN =
        Pattern.compile("^uploads/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/(.+)$");

    @Value("${aws.sqs.UploadQueue.url}")
    private String queueUrl;

    private final SqsClient sqsClient;
    private final FileRepository fileRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Autowired
    public UploadQueuePollingService(
            SqsClient sqsClient,
            FileRepository fileRepository,
            UploadSessionRepository uploadSessionRepository,
            S3Service s3Service,
            ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.fileRepository = fileRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 3000)
    public void pollUploadQueue() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(5)
            .build();

        List<Message> messages = this.sqsClient.receiveMessage(receiveMessageRequest).messages();
        if (messages.isEmpty()) {
            logger.info("No messages in upload queue");
            return;
        }
        messages.forEach(this::processMessage);
    }

    private void processMessage(Message message) {
        try {
            String messageBody = message.body();
            logger.info("Processing SQS message: {}", messageBody);

            // Parse the S3 event notification JSON
            JsonNode rootNode = objectMapper.readTree(messageBody);
            JsonNode records = rootNode.get("Records");

            if (records == null || !records.isArray()) {
                logger.warn("No Records found in message");
                return;
            }

            // Process each record in the message
            for (JsonNode record : records) {
                processS3Event(record);
            }

            // Delete the message from the queue after successful processing
            deleteMessage(message);

        } catch (Exception e) {
            logger.error("Error processing SQS message: {}", e.getMessage(), e);
        }
    }

    private void processS3Event(JsonNode record) {
        try {
            String eventName = record.get("eventName").asText();

            // Only process object creation events
            if (!eventName.startsWith("ObjectCreated:")) {
                logger.info("Skipping non-creation event: {}", eventName);
                return;
            }

            JsonNode s3Node = record.get("s3");
            JsonNode objectNode = s3Node.get("object");

            String s3Key = objectNode.get("key").asText();
            long fileSize = objectNode.get("size").asLong();

            logger.info("Processing S3 object: key={}, size={}", s3Key, fileSize);

            // Check if file already exists in database
            if (fileRepository.findByPath(s3Key).isPresent()) {
                logger.info("File already exists in database: {}", s3Key);
                return;
            }

            // NEW: Extract uploadSessionId from S3 key
            Matcher matcher = UPLOAD_KEY_PATTERN.matcher(s3Key);

            if (!matcher.matches()) {
                logger.warn("S3 key does not match expected pattern (uploads/{{sessionId}}/{{filename}}): {}", s3Key);
                logger.warn("SECURITY: Deleting unauthorized file without valid upload session in key");
                deleteUnauthorizedFile(s3Key);
                return;
            }

            String uploadSessionIdStr = matcher.group(1);  // The UUID part
            String originalFilename = matcher.group(2);     // The filename part

            UUID uploadSessionId;
            try {
                uploadSessionId = UUID.fromString(uploadSessionIdStr);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID in S3 key: {}", uploadSessionIdStr);
                deleteUnauthorizedFile(s3Key);
                return;
            }

            // NEW: Validate upload session exists and is valid
            Optional<UploadSessionEntity> sessionOpt = uploadSessionRepository.findById(uploadSessionId);

            if (sessionOpt.isEmpty()) {
                logger.warn("SECURITY: Upload session not found for ID: {}", uploadSessionId);
                logger.warn("Deleting unauthorized file: {}", s3Key);
                deleteUnauthorizedFile(s3Key);
                return;
            }

            UploadSessionEntity uploadSession = sessionOpt.get();

            // NEW: Check if session is expired
            if (uploadSession.isExpired()) {
                logger.warn("SECURITY: Upload session expired for ID: {}", uploadSessionId);
                logger.warn("Deleting file uploaded with expired session: {}", s3Key);
                deleteUnauthorizedFile(s3Key);
                uploadSessionRepository.delete(uploadSession);  // Clean up expired session
                return;
            }

            // NEW: Session is valid - extract userId
            UUID userId = uploadSession.getUserId();
            logger.info("Valid upload session found. User ID: {}, Session ID: {}", userId, uploadSessionId);

            // Extract file information
            String fileType = extractFileType(originalFilename);

            // NEW: Create file entity with userId from upload session
            FileEntity fileEntity = new FileEntity(
                originalFilename,
                fileType,
                (int) fileSize,
                s3Key,
                userId  // NEW: Associate with user from upload session
            );

            fileRepository.save(fileEntity);
            logger.info("Successfully saved file to database with user ID: {}", userId);

            // NEW: Mark upload session as used (optional - for audit trail)
            uploadSession.setUsed(true);
            uploadSessionRepository.save(uploadSession);

        } catch (Exception e) {
            logger.error("Error processing S3 event: {}", e.getMessage(), e);
        }
    }

    private String extractFileName(String s3Key) {
        // Extract filename from S3 key (handle paths like "folder/subfolder/file.txt")
        int lastSlashIndex = s3Key.lastIndexOf('/');
        return lastSlashIndex >= 0 ? s3Key.substring(lastSlashIndex + 1) : s3Key;
    }

    private String extractFileType(String fileName) {
        // Extract file extension
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private void deleteMessage(Message message) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

            sqsClient.deleteMessage(deleteMessageRequest);
            logger.info("Message deleted from queue");
        } catch (Exception e) {
            logger.error("Error deleting message from queue: {}", e.getMessage(), e);
        }
    }

    /**
     * NEW: Deletes an unauthorized file from S3.
     * Called when upload session validation fails.
     */
    private void deleteUnauthorizedFile(String s3Key) {
        try {
            s3Service.deleteFile(s3Key);
            logger.info("Successfully deleted unauthorized file from S3: {}", s3Key);
        } catch (Exception e) {
            logger.error("Failed to delete unauthorized file from S3: {}", s3Key, e);
            // Continue - we've logged the security issue
        }
    }
}
