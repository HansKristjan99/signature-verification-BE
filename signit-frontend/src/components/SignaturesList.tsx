import {
  Paper,
  Title,
  Text,
  Badge,
  Accordion,
  Group,
  Stack,
  Alert,
  Skeleton,
  List,
} from "@mantine/core";
import {
  IconShieldCheck,
  IconShieldX,
  IconAlertCircle,
  IconInfoCircle,
  IconCertificate,
} from "@tabler/icons-react";
import type { VerifySignaturesResult } from "../types/signature";

interface SignaturesListProps {
  signatureData: VerifySignaturesResult | null;
  loading: boolean;
}

function SignaturesList({ signatureData, loading }: SignaturesListProps) {
  // Loading state
  if (loading) {
    return (
      <Paper p="lg" radius="md" withBorder>
        <Stack gap="md">
          <Skeleton height={30} width="60%" />
          <Skeleton height={20} width="40%" />
          <Skeleton height={100} />
          <Skeleton height={100} />
        </Stack>
      </Paper>
    );
  }

  // Empty state
  if (!signatureData) {
    return (
      <Paper p="xl" radius="md" withBorder>
        <Stack align="center" gap="md">
          <IconCertificate size={48} color="var(--mantine-color-gray-5)" />
          <Text ta="center" c="dimmed" size="sm">
            Select a file and click "Verify Signatures" to see verification results
          </Text>
        </Stack>
      </Paper>
    );
  }

  // Error state
  if (!signatureData.success) {
    return (
      <Paper p="lg" radius="md" withBorder>
        <Alert
          icon={<IconAlertCircle size={16} />}
          title="Verification Failed"
          color="red"
          variant="light"
        >
          <Stack gap="xs">
            <Text size="sm">{signatureData.error || "Unknown error occurred"}</Text>
            {signatureData.errorType && (
              <Text size="xs" c="dimmed">
                Error Type: {signatureData.errorType}
              </Text>
            )}
            <Text size="xs" c="dimmed">
              File: {signatureData.filename}
            </Text>
          </Stack>
        </Alert>
      </Paper>
    );
  }

  // Success state - display results
  return (
    <Paper p="lg" radius="md" withBorder>
      <Stack gap="lg">
        {/* Header */}
        <div>
          <Group justify="space-between" mb="xs">
            <Title order={3}>Signature Verification Results</Title>
            <Badge
              size="lg"
              variant="filled"
              color={signatureData.valid ? "green" : "red"}
              leftSection={
                signatureData.valid ? (
                  <IconShieldCheck size={16} />
                ) : (
                  <IconShieldX size={16} />
                )
              }
            >
              {signatureData.valid ? "Valid" : "Invalid"}
            </Badge>
          </Group>
          <Text size="sm" c="dimmed">
            {signatureData.filename}
          </Text>
        </div>

        {/* Container Info */}
        <Paper p="md" withBorder bg="gray.0">
          <Group gap="md">
            <div>
              <Text size="xs" c="dimmed">
                Format
              </Text>
              <Text size="sm" fw={500}>
                {signatureData.containerFormat}
              </Text>
            </div>
            <div>
              <Text size="xs" c="dimmed">
                Signatures
              </Text>
              <Text size="sm" fw={500}>
                {signatureData.signatureCount}
              </Text>
            </div>
            <div>
              <Text size="xs" c="dimmed">
                Data Files
              </Text>
              <Text size="sm" fw={500}>
                {signatureData.dataFileCount}
              </Text>
            </div>
          </Group>
        </Paper>

        {/* Container Errors */}
        {signatureData.containerErrors.length > 0 && (
          <Alert
            icon={<IconAlertCircle size={16} />}
            title="Container Errors"
            color="red"
            variant="light"
          >
            <List size="sm">
              {signatureData.containerErrors.map((error, idx) => (
                <List.Item key={idx}>{error}</List.Item>
              ))}
            </List>
          </Alert>
        )}

        {/* Container Warnings */}
        {signatureData.containerWarnings.length > 0 && (
          <Alert
            icon={<IconInfoCircle size={16} />}
            title="Container Warnings"
            color="yellow"
            variant="light"
          >
            <List size="sm">
              {signatureData.containerWarnings.map((warning, idx) => (
                <List.Item key={idx}>{warning}</List.Item>
              ))}
            </List>
          </Alert>
        )}

        {/* Signatures */}
        {signatureData.signatures.length > 0 && (
          <div>
            <Title order={4} mb="md">
              Signatures ({signatureData.signatures.length})
            </Title>
            <Accordion variant="separated">
              {signatureData.signatures.map((signature, idx) => (
                <Accordion.Item key={signature.id || idx} value={`sig-${idx}`}>
                  <Accordion.Control>
                    <Group justify="space-between">
                      <div>
                        <Text fw={500}>{signature.signerName}</Text>
                        <Text size="xs" c="dimmed">
                          {signature.signingTime
                            ? new Date(signature.signingTime).toLocaleString()
                            : "Unknown time"}
                        </Text>
                      </div>
                      <Badge
                        color={signature.valid ? "green" : "red"}
                        variant="light"
                      >
                        {signature.valid ? "Valid" : "Invalid"}
                      </Badge>
                    </Group>
                  </Accordion.Control>
                  <Accordion.Panel>
                    <Stack gap="md">
                      {/* Signature Details */}
                      <Paper p="sm" withBorder bg="gray.0">
                        <Stack gap="xs">
                          <Group gap="md">
                            <div>
                              <Text size="xs" c="dimmed">
                                Method
                              </Text>
                              <Text size="sm">{signature.signatureMethod}</Text>
                            </div>
                            {signature.city && (
                              <div>
                                <Text size="xs" c="dimmed">
                                  City
                                </Text>
                                <Text size="sm">{signature.city}</Text>
                              </div>
                            )}
                            {signature.countryName && (
                              <div>
                                <Text size="xs" c="dimmed">
                                  Country
                                </Text>
                                <Text size="sm">{signature.countryName}</Text>
                              </div>
                            )}
                          </Group>
                          {signature.stateOrProvince && (
                            <div>
                              <Text size="xs" c="dimmed">
                                State/Province
                              </Text>
                              <Text size="sm">{signature.stateOrProvince}</Text>
                            </div>
                          )}
                          {signature.postalCode && (
                            <div>
                              <Text size="xs" c="dimmed">
                                Postal Code
                              </Text>
                              <Text size="sm">{signature.postalCode}</Text>
                            </div>
                          )}
                        </Stack>
                      </Paper>

                      {/* Signature Errors */}
                      {signature.errors.length > 0 && (
                        <Alert
                          icon={<IconAlertCircle size={16} />}
                          title="Errors"
                          color="red"
                          variant="light"
                        >
                          <List size="sm">
                            {signature.errors.map((error, errIdx) => (
                              <List.Item key={errIdx}>{error}</List.Item>
                            ))}
                          </List>
                        </Alert>
                      )}

                      {/* Signature Warnings */}
                      {signature.warnings.length > 0 && (
                        <Alert
                          icon={<IconInfoCircle size={16} />}
                          title="Warnings"
                          color="yellow"
                          variant="light"
                        >
                          <List size="sm">
                            {signature.warnings.map((warning, warnIdx) => (
                              <List.Item key={warnIdx}>{warning}</List.Item>
                            ))}
                          </List>
                        </Alert>
                      )}
                    </Stack>
                  </Accordion.Panel>
                </Accordion.Item>
              ))}
            </Accordion>
          </div>
        )}

        {signatureData.signatures.length === 0 && (
          <Alert
            icon={<IconInfoCircle size={16} />}
            title="No Signatures"
            color="blue"
            variant="light"
          >
            This container does not contain any signatures.
          </Alert>
        )}
      </Stack>
    </Paper>
  );
}

export default SignaturesList;
