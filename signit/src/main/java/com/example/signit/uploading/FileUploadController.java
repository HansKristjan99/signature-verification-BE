package com.example.signit.uploading;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.signit.storage.StorageFileNotFoundException;
import com.example.signit.storage.StorageService;
import com.example.data.FileEntity;
import com.example.data.FileRepository;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	private final FileRepository fileRepository;

	@Autowired
	public FileUploadController(StorageService storageService, FileRepository fileRepository) {
		this.storageService = storageService;
		this.fileRepository = fileRepository;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		// model.addAttribute("files", storageService.loadAll().map(
		// 		path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
		// 				"serveFile", path.getFileName().toString()).build().toUri().toString())
		// 		.collect(Collectors.toList()));
		model.addAttribute("files", java.util.stream.StreamSupport.stream(fileRepository.findAll().spliterator(), false)
			.map(fileEntity -> new java.util.AbstractMap.SimpleEntry<>(
				fileEntity.getFileName(),
				org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodName(
					FileUploadController.class, "serveFile", fileEntity.getFileName()
				).build().toUri().toString()
			))
			.collect(Collectors.toList()));

		// // return "uploadForm";
		// ArrayList<String> fileList = new ArrayList<>();
		// fileRepository.findAll().forEach(file -> fileList.add(file.getFileName()));
		// model.addAttribute("files", fileList);
		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);

		if (file == null)
			return ResponseEntity.notFound().build();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		
		storageService.store(file);
		fileRepository.save(new FileEntity(file.getOriginalFilename(), file.getContentType(), (int)file.getSize(), file.getName()));
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}