export interface SignatureInfo {
  id: string;
  signerName: string;
  signatureMethod: string;
  signingTime: string | null;
  city: string | null;
  stateOrProvince: string | null;
  postalCode: string | null;
  countryName: string | null;
  valid: boolean;
  errors: string[];
  warnings: string[];
}

export interface VerifySignaturesResult {
  success: boolean;
  filename: string;
  valid: boolean;
  containerFormat: string;
  dataFileCount: number;
  signatureCount: number;
  signatures: SignatureInfo[];
  containerErrors: string[];
  containerWarnings: string[];
  error?: string;
  errorType?: string;
  timestamp: number;
}
