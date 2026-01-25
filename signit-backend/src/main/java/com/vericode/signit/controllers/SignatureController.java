package com.vericode.signit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vericode.esignatures.estonia.Signatures;
import com.vericode.signit.dto.VerifySignaturesResult;
import com.vericode.signit.storage.S3.S3Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
// https://github.com/open-eid/digidoc4j/wiki/Examples-of-using-it#simple-external-signing-example-eg-signing-in-web
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/signatures")
public class SignatureController {

    private final S3Service storageService;
    private final Signatures signatures;

    // Valid Estonian digital signature container formats
    private static final List<String> VALID_EXTENSIONS = Arrays.asList(".asice", ".bdoc", ".ddoc", ".edoc");

    @Autowired
    public SignatureController(S3Service storageService) {
        this.storageService = storageService;
        this.signatures = new Signatures();
    }

    @PostMapping
    public ResponseEntity<VerifySignaturesResult> verifySignature(@RequestParam String filename) {
        Path tempFile = null;

        try {
            // Validate filename
            String validationError = validateFilename(filename);
            if (validationError != null) {
                VerifySignaturesResult errorResult = new VerifySignaturesResult();
                errorResult.setSuccess(false);
                errorResult.setFilename(filename);
                errorResult.setError("Invalid request");
                errorResult.setErrorType(validationError);
                errorResult.setTimestamp(System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
            }

            // Download file from S3 to temporary location
            tempFile = storageService.load(filename);

            // Verify the signature container
            VerifySignaturesResult result = signatures.getContainerSignatures(tempFile.toString());

            // Add filename and timestamp to response
            result.setFilename(filename);
            result.setTimestamp(System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            VerifySignaturesResult errorResult = new VerifySignaturesResult();
            errorResult.setSuccess(false);
            errorResult.setFilename(filename);
            errorResult.setError("Failed to process signature verification: " + e.getMessage());
            errorResult.setErrorType(e.getClass().getSimpleName());
            errorResult.setTimestamp(System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);

        } finally {
            // Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    System.err.println("Failed to delete temporary file: " + tempFile);
                }
            }
        }
    }

    /**
     * Validates the filename for security and format requirements.
     * Returns null if valid, or an error message if invalid.
     */
    private String validateFilename(String filename) {
        // Check if filename is null or empty
        if (filename == null || filename.trim().isEmpty()) {
            return "Filename cannot be empty";
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return "Filename contains invalid characters (path traversal detected)";
        }

        // Check for valid extension
        String lowerFilename = filename.toLowerCase();
        boolean hasValidExtension = VALID_EXTENSIONS.stream()
            .anyMatch(lowerFilename::endsWith);

        if (!hasValidExtension) {
            return "Invalid file format. Supported formats: " + String.join(", ", VALID_EXTENSIONS);
        }

        // Check filename length
        if (filename.length() > 255) {
            return "Filename is too long (max 255 characters)";
        }

        return null; // Valid
    }
}
