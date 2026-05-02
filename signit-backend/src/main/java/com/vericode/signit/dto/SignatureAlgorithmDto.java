package com.vericode.signit.dto;

public class SignatureAlgorithmDto {
    public SignatureHashFunction hashFunction;
    public SignaturePaddingScheme paddingScheme;
    public SignatureCryptoAlgorithm cryptoAlgorithm;

    public enum SignatureHashFunction {
        SHA224, SHA256, SHA384, SHA512, SHA3_224, SHA3_256, SHA3_384, SHA3_512
    }

    public enum SignatureCryptoAlgorithm {
        ECC, RSA
    }

    public enum SignaturePaddingScheme {
        NONE, PKCS1_5, PSS
    }
}
