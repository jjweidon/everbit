"use client";

export interface TableColumn<T> {
  key: string;
  header: string;
  /** 숫자 컬럼 시 우측 정렬 + tabular-nums */
  numeric?: boolean;
  render?: (row: T) => React.ReactNode;
}

export interface TableProps<T extends Record<string, unknown>> {
  columns: TableColumn<T>[];
  rows: T[];
  /** row 키 추출 (접근성/React key) */
  getRowKey: (row: T) => string;
  /** row 클릭 시 (선택) */
  onRowClick?: (row: T) => void;
  className?: string;
}

export function Table<T extends Record<string, unknown>>({
  columns,
  rows,
  getRowKey,
  onRowClick,
  className = "",
}: TableProps<T>) {
  return (
    <div className={`overflow-x-auto ${className}`}>
      <table className="w-full border-collapse text-sm" role="table">
        <thead>
          <tr className="border-b border-divider bg-bg2">
            {columns.map((col) => (
              <th
                key={col.key}
                scope="col"
                className={`px-4 py-3 text-left font-medium text-text-2 ${
                  col.numeric ? "text-right tabular-nums" : ""
                }`}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr
              key={getRowKey(row)}
              onClick={() => onRowClick?.(row)}
              className={`border-b border-divider transition-colors ${
                onRowClick ? "cursor-pointer hover:bg-bg2" : ""
              }`}
            >
              {columns.map((col) => (
                <td
                  key={col.key}
                  className={`px-4 py-3 text-text-1 ${
                    col.numeric ? "text-right tabular-nums" : ""
                  }`}
                >
                  {col.render
                    ? col.render(row)
                    : String((row[col.key] as React.ReactNode) ?? "")}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
