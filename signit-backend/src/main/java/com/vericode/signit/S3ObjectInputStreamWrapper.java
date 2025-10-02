package com.vericode.signit;
import java.io.InputStream;

public record S3ObjectInputStreamWrapper(InputStream inputStream, String eTag) {
}