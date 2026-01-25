package com.vericode.data.File;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, UUID> {
    Optional<FileEntity> findByPath(String path);

    List<FileEntity> findByUserId(UUID userId);
}