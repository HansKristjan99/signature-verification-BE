import { Table } from '@mantine/core';

export interface FileTableProps {
  elements: string[];
};

function FileTable({ elements }: FileTableProps) {
  const rows = elements.map((file, idx) => (
    <Table.Tr key={idx}>
      <Table.Td>{file}</Table.Td>
    </Table.Tr>
  ));

  return (
    <Table>
      <Table.Thead>
        <Table.Tr>
          <Table.Th>File name</Table.Th>
        </Table.Tr>
      </Table.Thead>
      <Table.Tbody>{rows}</Table.Tbody>
    </Table>
  );
}
export default FileTable;
