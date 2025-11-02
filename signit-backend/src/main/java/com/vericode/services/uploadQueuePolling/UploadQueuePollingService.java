package com.vericode.services.uploadQueuePolling;

import java.util.List;

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

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
class UploadQueuePollingService {

    private static final Logger logger = LoggerFactory.getLogger(UploadQueuePollingService.class);

    @Value("${aws.sqs.UploadQueue.url}")
    private String queueUrl;

    private final SqsClient sqsClient;
    private final FileRepository fileRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public UploadQueuePollingService(SqsClient sqsClient, FileRepository fileRepository, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.fileRepository = fileRepository;
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

            // Extract file information
            String fileName = extractFileName(s3Key);
            String fileType = extractFileType(fileName);

            // Create new file entity (userId is null for automatic uploads)
            FileEntity fileEntity = new FileEntity(
                fileName,
                fileType,
                (int) fileSize,
                s3Key,
                null  // userId - will be associated later when user claims the file
            );

            fileRepository.save(fileEntity);
            logger.info("Successfully saved new file to database: {}", s3Key);

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
}
