package com.vericode.signit.controllers;

import com.vericode.data.File.FileEntity;
import com.vericode.data.File.FileRepository;
import com.vericode.data.User.UserEntity;
import com.vericode.esignatures.estonia.Signatures;
import com.vericode.signit.dto.FinalizeSigningRequest;
import com.vericode.signit.dto.FinalizeSigningResponse;
import com.vericode.signit.dto.PrepareSigningRequest;
import com.vericode.signit.dto.PrepareSigningResponse;
import com.vericode.signit.dto.VerifySignaturesResult;
import com.vericode.signit.signing.SigningSession;
import com.vericode.signit.signing.SigningSessionStore;
import com.vericode.signit.storage.S3.S3Service;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.DataToSign;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.Signature;
import org.digidoc4j.SignatureBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

// https://github.com/open-eid/digidoc4j/wiki/Examples-of-using-it#simple-external-signing-example-eg-signing-in-web
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/signatures")
public class SignatureController {

    private final S3Service storageService;
    private final Signatures signatures;
    private final SigningSessionStore signingSessionStore;
    private final FileRepository fileRepository;

    // Valid Estonian digital signature container formats
    private static final List<String> VALID_EXTENSIONS = Arrays.asList(".asice", ".bdoc", ".ddoc", ".edoc");

    @Autowired
    public SignatureController(S3Service storageService, SigningSessionStore signingSessionStore, FileRepository fileRepository) {
        this.storageService = storageService;
        this.signatures = new Signatures();
        this.signingSessionStore = signingSessionStore;
        this.fileRepository = fileRepository;
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
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    System.err.println("Failed to delete temporary file: " + tempFile);
                }
            }
        }
    }

    @PostMapping("/prepare")
    public ResponseEntity<?> prepareSignature(@RequestBody PrepareSigningRequest requestDto, HttpServletRequest request) {
        Path tempFile = null;

        try {
            String filename = requestDto.getFilename();

            // Validate filename (allow S3 key paths, just block traversal)
            if (filename == null || filename.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Filename cannot be empty");
            }
            if (filename.contains("..") || filename.contains("\\")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid filename");
            }
            if (filename.length() > 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Filename is too long");
            }

            UserEntity currentUser = (UserEntity) request.getAttribute("currentUser");

            // Download file from S3
            tempFile = storageService.load(filename);

            // Decode certificate: Base64 → DER bytes → X509Certificate
            byte[] certBytes = Base64.getDecoder().decode(requestDto.getCertificate());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));

            // Create or open container depending on file type
            String lowerFilename = filename.toLowerCase();
            boolean isContainer = VALID_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);

            Container container;
            if (isContainer) {
                container = ContainerOpener.open(tempFile.toString());
            } else {
                String mimeType = detectMimeType(filename);
                container = ContainerBuilder.aContainer()
                        .withConfiguration(Configuration.of(Configuration.Mode.PROD))
                        .build();
                container.addDataFile(tempFile.toFile(), mimeType);
            }

            DataToSign dataToSign = SignatureBuilder.aSignature(container)
                    .withSigningCertificate(cert)
                    .withSignatureDigestAlgorithm(DigestAlgorithm.SHA256)
                    .buildDataToSign();

            String sessionId = UUID.randomUUID().toString();
            signingSessionStore.store(sessionId, new SigningSession(
                    filename, tempFile, container, dataToSign, currentUser.getUserId()
            ));

            String hash = Base64.getEncoder().encodeToString(dataToSign.getDataToSign());
            return ResponseEntity.ok(new PrepareSigningResponse(sessionId, hash, "SHA-256"));

        } catch (Exception e) {
            // Clean up temp file on error (if session was not stored)
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to prepare signing: " + e.getMessage());
        }
    }

    @PostMapping("/finalize")
    public ResponseEntity<?> finalizeSignature(@RequestBody FinalizeSigningRequest requestDto, HttpServletRequest request) {
        try {
            UserEntity currentUser = (UserEntity) request.getAttribute("currentUser");

            SigningSession session = signingSessionStore.get(requestDto.getSigningSessionId())
                    .orElse(null);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Signing session not found");
            }

            if (!session.getUserId().equals(currentUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Session belongs to a different user");
            }

            byte[] sigBytes = Base64.getDecoder().decode(requestDto.getSignature());
            Signature sig = session.getDataToSign().finalize(sigBytes);
            session.getContainer().addSignature(sig);

            Path containerTempPath = Files.createTempFile("signed-", ".asice");
            session.getContainer().saveAsFile(containerTempPath.toString());

            // Compute output S3 key
            String originalKey = session.getOriginalFilename();
            String basename = originalKey.contains("/")
                    ? originalKey.substring(originalKey.lastIndexOf('/') + 1)
                    : originalKey;
            String outputName = basename.endsWith(".asice") ? basename : basename + ".asice";
            String s3Key = "uploads/" + UUID.randomUUID() + "/" + outputName;

            storageService.uploadFile(containerTempPath, s3Key);

            long fileSize = Files.size(containerTempPath);
            fileRepository.save(new FileEntity(
                    outputName,
                    "application/vnd.etsi.asic-e+zip",
                    (int) fileSize,
                    s3Key,
                    currentUser.getUserId()
            ));

            // Cleanup
            signingSessionStore.remove(requestDto.getSigningSessionId());
            try { Files.deleteIfExists(session.getTempFilePath()); } catch (Exception ignored) {}
            try { Files.deleteIfExists(containerTempPath); } catch (Exception ignored) {}

            return ResponseEntity.ok(new FinalizeSigningResponse(true, s3Key));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to finalize signing: " + e.getMessage());
        }
    }

    private String detectMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    /**
     * Validates the filename for security and format requirements (container files only).
     * Returns null if valid, or an error message if invalid.
     */
    private String validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "Filename cannot be empty";
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return "Filename contains invalid characters (path traversal detected)";
        }

        String lowerFilename = filename.toLowerCase();
        boolean hasValidExtension = VALID_EXTENSIONS.stream()
            .anyMatch(lowerFilename::endsWith);

        if (!hasValidExtension) {
            return "Invalid file format. Supported formats: " + String.join(", ", VALID_EXTENSIONS);
        }

        if (filename.length() > 255) {
            return "Filename is too long (max 255 characters)";
        }

        return null;
    }
}
