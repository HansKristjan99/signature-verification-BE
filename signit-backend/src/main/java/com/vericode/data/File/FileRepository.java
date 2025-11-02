package com.vericode.data.File;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, Integer> {
    Optional<FileEntity> findByPath(String path);
}