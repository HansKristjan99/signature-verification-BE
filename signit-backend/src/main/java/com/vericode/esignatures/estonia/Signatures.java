package com.vericode.esignatures.estonia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.digidoc4j.Container;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.Signature;
import org.digidoc4j.ValidationResult;
import org.digidoc4j.exceptions.DigiDoc4JException;

import com.vericode.signit.dto.SignatureInfo;
import com.vericode.signit.dto.VerifySignaturesResult;

public class Signatures {
    public boolean validateContainer(String path) {
            // Open an existing container from the file "test-container.asice"
        Container container = ContainerOpener
                .open("test-container.asice");

        // Validate the container
        ContainerValidationResult result = container.validate();

        //Check if the container is valid
        boolean isContainerValid = result.isValid();

        //Get the validation errors and warnings
        List<DigiDoc4JException> validationErrors = result.getErrors();
        List<DigiDoc4JException> validationWarnings = result.getWarnings();
        List<DigiDoc4JException> containerErrors = result.getContainerErrors();//Container format errors; do not affect the result of isValid()
        List<DigiDoc4JException> containerWarnings = result.getContainerWarnings();

        //See the validation report in XML (for debugging only - DO NOT BASE YOUR APPLICATION LOGIC ON IT)
        String validationReport = result.getReport();
        result.getSignatureReports();
        System.out.println("Signature list");
        System.out.println(result.getSignatureIdList().toString());
        return isContainerValid;
    }

    public boolean validateSignature(Signature signature){
        // Get the signature validation result. If the container has already been validated, then an existing validation result is returned, otherwise a full validation is done on the signature.
        ValidationResult result = signature.validateSignature();

        // Check if the signature is valid
        boolean isSignatureValid = result.isValid();

        // See the signature validation errors and warnings
        List<DigiDoc4JException> validationErrors = result.getErrors();
        List<DigiDoc4JException> validationWarnings = result.getWarnings();
        return isSignatureValid;
    }

    public VerifySignaturesResult getContainerSignatures(String path) {
        VerifySignaturesResult result = new VerifySignaturesResult();

        try {
            // Open the container from the provided path
            Container container = ContainerOpener.open(path);

            // Validate the container
            ContainerValidationResult validationResult = container.validate();

            // Basic container info
            result.setValid(validationResult.isValid());
            result.setContainerFormat(container.getType());
            result.setDataFileCount(container.getDataFiles().size());

            // Container-level errors and warnings
            result.setContainerErrors(validationResult.getContainerErrors().stream()
                .map(DigiDoc4JException::getMessage)
                .collect(Collectors.toList()));
            result.setContainerWarnings(validationResult.getContainerWarnings().stream()
                .map(DigiDoc4JException::getMessage)
                .collect(Collectors.toList()));

            // Get all signatures
            List<Signature> signatures = container.getSignatures();
            List<SignatureInfo> signatureDetails = new ArrayList<>();

            for (Signature signature : signatures) {
                // Validate individual signature
                ValidationResult sigValidation = signature.validateSignature();

                // Build SignatureInfo object
                SignatureInfo sigInfo = new SignatureInfo(
                    signature.getId(),
                    signature.getSigningCertificate().getSubjectName(),
                    signature.getSignatureMethod(),
                    signature.getClaimedSigningTime() != null ?
                        signature.getClaimedSigningTime().toString() : null,
                    signature.getCity(),
                    signature.getStateOrProvince(),
                    signature.getPostalCode(),
                    signature.getCountryName(),
                    sigValidation.isValid(),
                    sigValidation.getErrors().stream()
                        .map(DigiDoc4JException::getMessage)
                        .collect(Collectors.toList()),
                    sigValidation.getWarnings().stream()
                        .map(DigiDoc4JException::getMessage)
                        .collect(Collectors.toList())
                );

                signatureDetails.add(sigInfo);
            }

            result.setSignatureCount(signatures.size());
            result.setSignatures(signatureDetails);
            result.setSuccess(true);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(e.getMessage());
            result.setErrorType(e.getClass().getSimpleName());
        }

        return result;
    }

}
