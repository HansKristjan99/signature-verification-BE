package com.vericode.signit.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.vericode.data.File.FileEntity;
import com.vericode.data.File.FileRepository;
import com.vericode.data.UploadSession.UploadSessionEntity;
import com.vericode.data.UploadSession.UploadSessionRepository;
import com.vericode.data.User.UserEntity;
import com.vericode.signit.dto.UploadUrlResponse;
import com.vericode.signit.storage.StorageFileNotFoundException;
import com.vericode.signit.storage.S3.S3Service;
import com.vericode.signit.types.AccessType;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.http.SdkHttpMethod;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/files")
public class FileUploadController {

	private final S3Service storageService;

	private final FileRepository fileRepository;

	private final UploadSessionRepository uploadSessionRepository;

	@Autowired
	public FileUploadController(
			S3Service storageService,
			FileRepository fileRepository,
			UploadSessionRepository uploadSessionRepository
	) {
		this.storageService = storageService;
		this.fileRepository = fileRepository;
		this.uploadSessionRepository = uploadSessionRepository;
	}

	@GetMapping("/")
	public ResponseEntity<ArrayList<String>> listUploadedFiles(Model model, HttpServletRequest request) throws IOException {
		// Get authenticated user from request attributes (set by SessionValidationFilter)
		UserEntity currentUser = (UserEntity) request.getAttribute("currentUser");

		if (currentUser == null) {
			return ResponseEntity.status(401).body(new ArrayList<>());
		}

		UUID userId = currentUser.getUserId();

		// Get only files for current user
		List<FileEntity> userFiles = fileRepository.findByUserId(userId);

		ArrayList<String> filenames = userFiles.stream()
			.map(FileEntity::getFileName)
			.collect(Collectors.toCollection(ArrayList::new));

		return ResponseEntity.ok().body(filenames);
	}

	@GetMapping("/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		try {
			Resource file = storageService.loadAsResource(filename);
			if (file == null) return ResponseEntity.notFound().build();
			System.out.println("Serving file: " + filename);
			System.out.println(ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file));
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}
	@GetMapping("/newUploadURL/")
	@ResponseBody
	@Transactional
	public ResponseEntity<UploadUrlResponse> getUploadUrl(@RequestParam String filename, HttpServletRequest request) {
		try {
			// Get authenticated user from request attributes (set by SessionValidationFilter)
			UserEntity currentUser = (UserEntity) request.getAttribute("currentUser");

			if (currentUser == null) {
				return ResponseEntity.status(401).body(null);
			}

			UUID userId = currentUser.getUserId();

			// STEP 1: Create upload session entity
			UploadSessionEntity uploadSession = new UploadSessionEntity(userId, 10);  // 10 minutes

			// STEP 2: Save to database
			uploadSessionRepository.save(uploadSession);

			// STEP 3: Build full S3 key: uploads/{uploadSessionId}/{filename}
			String s3Key = uploadSession.getS3KeyPrefix() + filename;

			// STEP 4: Generate pre-signed URL for this specific key
			String uploadUrl = storageService.generatePreSignedUrl(
				s3Key,
				SdkHttpMethod.PUT,
				AccessType.PUBLIC
			);

			// STEP 5: Return response with URL and session info
			UploadUrlResponse response = new UploadUrlResponse(
				uploadUrl,
				uploadSession.getId().toString(),
				s3Key,
				600  // 10 minutes in seconds
			);

			return ResponseEntity.ok().body(response);

		} catch (Exception e) {
			e.printStackTrace();
			throw new StorageFileNotFoundException("Could not generate upload URL: " + filename, e);
		}
	}
	// @PostMapping("/confirm-upload")
	// @ResponseBody
	// public ResponseEntity<String> confirmUpload(
	// 		@RequestParam String filename,
	// 		@RequestHeader("X-Session-Token") String sessionToken) {

	// 	Optional<UserSessionEntity> userSessionOpt =
	// 		userSessionRepository.findBySessionToken(sessionToken);

	// 	if (userSessionOpt.isEmpty()) {
	// 		return ResponseEntity.status(401).body("Invalid session token");
	// 	}

	// 	UserEntity currentUser = userSessionOpt.get().getUser();
	// 	System.out.println("Current user ID: " + currentUser.getUserId());

	// 	Optional<UploadSessionEntity> uploadSessionOpt =
	// 		uploadSessionRepository.findByUserIdAndFilePath(currentUser.getUserId(), filename);

	// 	if (uploadSessionOpt.isEmpty()) {
	// 		return ResponseEntity.status(403).body("No valid upload session found for this file");
	// 	}

	// 	UploadSessionEntity uploadSession = uploadSessionOpt.get();

	// 	if (uploadSession.isExpired()) {
	// 		uploadSessionRepository.delete(uploadSession);
	// 		return ResponseEntity.status(403).body("Upload session expired");
	// 	}

	// 	String fileType = storageService.getContentType(filename);
	// 	long fileSize = storageService.getFileSize(filename);

	// 	FileEntity fileEntity = new FileEntity(filename, fileType, (int)fileSize, filename, currentUser.getUserId());
	// 	fileRepository.save(fileEntity);

	// 	uploadSessionRepository.delete(uploadSession);

	// 	return ResponseEntity.ok().body("File registered successfully");
	// }

	// @GetMapping("/signatures/")
	// public List<Signature> getFileSignatures(@RequestParam Integer fileId) {
	// 	FileEntity file = fileRepository.findById(fileId).orElse(null);
	// 	if (file == null) {
	// 		return new ArrayList<>();
	// 	}

		
	// 	return signatureRepository.findByFileId(fileId);
	// }

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}