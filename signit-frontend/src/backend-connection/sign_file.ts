import * as webeid from '@web-eid/web-eid-library/dist/es/web-eid.js';
import { apiClient } from '../api/client';
// https://github.com/web-eid/web-eid.js

export async function signFile(filename: string): Promise<void> {
    const { certificate, supportedSignatureAlgorithms } = await webeid.getSigningCertificate({ lang: 'en' });

    const prepareRes = await apiClient.post("/signatures/prepare",
        JSON.stringify({ filename, certificate, supportedSignatureAlgorithms }),
    );
    if (!prepareRes.ok) {
        throw new Error("POST /signatures/prepare error: " + prepareRes.status);
    }

    const { signingSessionId, hash, hashFunction } = await prepareRes.json();

    const { signature, signatureAlgorithm } = await webeid.sign(certificate, hash, hashFunction, { lang: 'en' });

    const finalizeRes = await apiClient.post("/signatures/finalize",
        JSON.stringify({ signingSessionId, signature, signatureAlgorithm }),
    );
    if (!finalizeRes.ok) {
        throw new Error("POST /signatures/finalize error: " + finalizeRes.status);
    }

    const signResult = await finalizeRes.json();
    console.log("Signing successful!", signResult);
}
