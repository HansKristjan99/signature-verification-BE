import { Table, Paper, TextInput, Text, Badge, Group, ActionIcon, Tooltip, ScrollArea } from '@mantine/core';
import { useState, useMemo } from 'react';
import { IconSearch, IconFile, IconDownload, IconTrash } from '@tabler/icons-react';

export interface FileTableProps {
  elements: string[];
}

function FileTable({ elements }: FileTableProps) {
  const [search, setSearch] = useState('');

  const filteredElements = useMemo(() => {
    if (!search) return elements;
    return elements.filter((file) =>
      file.toLowerCase().includes(search.toLowerCase())
    );
  }, [elements, search]);

  const getFileExtension = (filename: string) => {
    const ext = filename.split('.').pop()?.toUpperCase();
    return ext || 'FILE';
  };

  const getFileColor = (filename: string) => {
    const ext = filename.split('.').pop()?.toLowerCase();
    const colorMap: { [key: string]: string } = {
      pdf: 'red',
      doc: 'blue',
      docx: 'blue',
      xls: 'green',
      xlsx: 'green',
      txt: 'gray',
      zip: 'orange',
    };
    return colorMap[ext || ''] || 'blue';
  };

  if (elements.length === 0) {
    return (
      <Paper p="xl" radius="md" withBorder>
        <Text ta="center" c="dimmed" size="sm">
          No files uploaded yet. Upload a file to get started.
        </Text>
      </Paper>
    );
  }

  return (
    <Paper radius="md" withBorder>
      <div style={{ padding: '16px' }}>
        <TextInput
          placeholder="Search files..."
          leftSection={<IconSearch size={16} />}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          mb="md"
        />
      </div>

      <ScrollArea>
        <Table highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>File Name</Table.Th>
              <Table.Th>Type</Table.Th>
              <Table.Th style={{ width: 100 }}>Actions</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {filteredElements.length > 0 ? (
              filteredElements.map((file, idx) => (
                <Table.Tr key={idx}>
                  <Table.Td>
                    <Group gap="sm">
                      <IconFile size={20} color="var(--mantine-color-blue-6)" />
                      <Text size="sm" fw={500}>
                        {file}
                      </Text>
                    </Group>
                  </Table.Td>
                  <Table.Td>
                    <Badge
                      variant="light"
                      color={getFileColor(file)}
                      size="sm"
                    >
                      {getFileExtension(file)}
                    </Badge>
                  </Table.Td>
                  <Table.Td>
                    <Group gap="xs">
                      <Tooltip label="Download">
                        <ActionIcon variant="subtle" color="blue">
                          <IconDownload size={16} />
                        </ActionIcon>
                      </Tooltip>
                      <Tooltip label="Delete">
                        <ActionIcon variant="subtle" color="red">
                          <IconTrash size={16} />
                        </ActionIcon>
                      </Tooltip>
                    </Group>
                  </Table.Td>
                </Table.Tr>
              ))
            ) : (
              <Table.Tr>
                <Table.Td colSpan={3}>
                  <Text ta="center" c="dimmed" size="sm">
                    No files found matching "{search}"
                  </Text>
                </Table.Td>
              </Table.Tr>
            )}
          </Table.Tbody>
        </Table>
      </ScrollArea>
    </Paper>
  );
}

export default FileTable;
