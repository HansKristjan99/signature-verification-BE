package com.vericode.data.File;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class FileEntity {
  
  @Column(name = "file_name")
  private String fileName;

  @Column(name = "file_type")
  private String fileType;
  
  @Column(name = "size")
  private int size;

  @Column(name = "upload_time")
  private LocalDate uploadTime;

  @Column(name = "path")
  private String path;

  @Column(name = "user_id", columnDefinition = "UUID")
  private UUID userId;
  
  @Id
  @Column(name = "id", columnDefinition = "UUID")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;
  // Hibernate expects entities to have a no-arg constructor,
  // though it does not necessarily have to be public.
  private FileEntity() {}
  
    public FileEntity(String fileName, String fileType, int size, String path, UUID userId) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
        this.uploadTime = LocalDate.now();
        this.path = path;
        this.userId = userId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileType() {
        return fileType;    
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LocalDate getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDate uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}