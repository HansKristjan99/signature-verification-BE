package com.vericode.signit.controllers;

import java.io.IOException;
import java.nio.file.Path;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vericode.data.File.FileEntity;
import com.vericode.data.File.FileRepository;
import com.vericode.signit.AccessType;
import com.vericode.signit.storage.StorageFileNotFoundException;
import com.vericode.signit.storage.S3.S3Service;

import software.amazon.awssdk.http.SdkHttpMethod;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/files")
public class FileUploadController {

	private final S3Service storageService;

	private final FileRepository fileRepository;

	@Autowired
	public FileUploadController(S3Service storageService, FileRepository fileRepository) {
		this.storageService = storageService;
		this.fileRepository = fileRepository;
	}

	@GetMapping("/")
	public ResponseEntity<ArrayList<String>> listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll()
			.map(path -> org.springframework.web.servlet.mvc.method.annotation
				.MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
					"serveFile", path.getFileName().toString())
				.build()
				.toString())
			.collect(Collectors.toList()));
		return ResponseEntity.ok().body(storageService.loadAll().map(Path::getFileName).map(Path::toString).collect(Collectors.toCollection(ArrayList::new)));
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
	@CrossOrigin(origins= "http://localhost:5173")
	@GetMapping("/newUploadURL/")
	@ResponseBody
	public ResponseEntity<String> getUploadUrl(@RequestParam String filename) {
		try {
			String uploadUrl = storageService.generatePreSignedUrl(filename, SdkHttpMethod.PUT, AccessType.PUBLIC); 
			return ResponseEntity.ok().body(uploadUrl);
		} catch (Exception e) {
			e.printStackTrace();
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}



	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		
		String storeResponse = storageService.store(file);
		fileRepository.save(new FileEntity(file.getOriginalFilename(), file.getContentType(), (int)file.getSize(), file.getOriginalFilename()));
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + storeResponse + "!");

		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}