import * as webeid from '@web-eid/web-eid-library';
import { apiClient } from '../api/client';
// https://github.com/web-eid/web-eid.js

async function signFile(file: string) {
    try {
            const {
                certificate,
                supportedSignatureAlgorithms
            } = await webeid.getSigningCertificate({'lang': 'en'});

            const prepareSigningResponse = await apiClient.post("/sign/prepare", 
               JSON.stringify({certificate, supportedSignatureAlgorithms}),
            );
            if (!prepareSigningResponse.ok) {
                throw new Error("POST /sign/prepare server error: " +
                                prepareSigningResponse.status);
            }
            const {
                hash,
                hashFunction
            } = await prepareSigningResponse.json();

            const {
                signature,
                signatureAlgorithm
            } = await webeid.sign(certificate, hash, hashFunction, {lang});

            const finalizeSigningResponse = await apiClient.post("/sign/finalize", 
               JSON.stringify({signature, signatureAlgorithm}),
            );
            if (!finalizeSigningResponse.ok) {
                throw new Error("POST /sign/finalize server error: " +
                                finalizeSigningResponse.status);
            }
            const signResult = await finalizeSigningResponse.json();

            console.log("Signing successful! Response:", response);
            // display successful signing message to user

        } catch (error) {
            console.log("Signing failed! Error:", error);
            throw error;
        }
}