package com.vericode.data.File;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "file_info")
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
  
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  // Hibernate expects entities to have a no-arg constructor,
  // though it does not necessarily have to be public.
  private FileEntity() {}
  
    public FileEntity(String fileName, String fileType, int size, String path) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
        this.uploadTime = LocalDate.now();
        this.path = path;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}