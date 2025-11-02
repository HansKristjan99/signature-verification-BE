package com.vericode.signit.controllers;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.vericode.data.File.FileEntity;
import com.vericode.data.File.FileRepository;
import com.vericode.data.UserSession.UserSessionRepository;
import com.vericode.signit.storage.StorageFileNotFoundException;
import com.vericode.signit.storage.S3.S3Service;
import com.vericode.signit.types.AccessType;

import software.amazon.awssdk.http.SdkHttpMethod;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/files")
public class FileUploadController {

	private final S3Service storageService;

	private final FileRepository fileRepository;

	private final UserSessionRepository userSessionRepository;

	@Autowired
	public FileUploadController(S3Service storageService, FileRepository fileRepository, UserSessionRepository userSessionRepository) {
		this.storageService = storageService;
		this.fileRepository = fileRepository;
		this.userSessionRepository = userSessionRepository;
	}

	@GetMapping("/")
	public ResponseEntity<ArrayList<String>> listUploadedFiles(Model model) throws IOException {

		ArrayList<String> filenames = java.util.stream.StreamSupport.stream(fileRepository.findAll().spliterator(), false)
			.map(FileEntity::getFileName)
			.collect(Collectors.toCollection(ArrayList::new));
		return ResponseEntity.ok().body(filenames);


		// model.addAttribute("files", storageService.loadAll()
		// 	.map(path -> org.springframework.web.servlet.mvc.method.annotation
		// 		.MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
		// 			"serveFile", path.getFileName().toString())
		// 		.build()
		// 		.toString())
		// 	.collect(Collectors.toList()));
		// return ResponseEntity.ok().body(storageService.loadAll().map(Path::getFileName).map(Path::toString).collect(Collectors.toCollection(ArrayList::new)));
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
	public ResponseEntity<String> getUploadUrl(@RequestParam String filename, @RequestHeader("X-Session-Token") String sessionToken) {
		try {
			String uploadUrl = storageService.generatePreSignedUrl(filename, SdkHttpMethod.PUT, AccessType.PUBLIC);
			Integer userId = userSessionRepository.findBySessionToken(sessionToken).get().getUser().getUserId();
			return ResponseEntity.ok().body(uploadUrl);
		} catch (Exception e) {
			e.printStackTrace();
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
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