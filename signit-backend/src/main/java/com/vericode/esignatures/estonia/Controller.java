package com.vericode.esignatures.estonia;

import java.util.List;

import org.digidoc4j.Container;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.Signature;
import org.digidoc4j.SignatureValidationResult;
import org.digidoc4j.exceptions.DigiDoc4JException;

public class Controller {
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
        return isContainerValid;
    }

    public boolean validateSignature(Signature signature){ 
        // Get the signature validation result. If the container has already been validated, then an existing validation result is returned, otherwise a full validation is done on the signature.
        SignatureValidationResult result = (SignatureValidationResult) signature.validateSignature();

        // Check if the signature is valid
        boolean isSignatureValid = result.isValid();

        // See the signature validation errors and warnings
        List<DigiDoc4JException> validationErrors = result.getErrors();
        List<DigiDoc4JException> validationWarnings = result.getWarnings();
        return isSignatureValid;
    }

    
}
