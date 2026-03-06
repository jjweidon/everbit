"use client";

import { useState } from "react";
import {
  Button,
  Input,
  Select,
  Card,
  StatusChip,
  Table,
  ConfirmModal,
  Drawer,
  ToastProvider,
  useToast,
} from "@/components/ui";

const selectOptions = [
  { value: "a", label: "옵션 A" },
  { value: "b", label: "옵션 B" },
  { value: "c", label: "옵션 C" },
];

type DemoRow = { id: string; market: string; side: string; amount: string; status: string };
const demoRows: DemoRow[] = [
  { id: "1", market: "KRW-BTC", side: "BUY", amount: "1,234,567", status: "ACKED" },
  { id: "2", market: "KRW-ETH", side: "SELL", amount: "987,654", status: "THROTTLED" },
  { id: "3", market: "KRW-XRP", side: "BUY", amount: "500,000", status: "UNKNOWN" },
];

export default function UiKitPage() {
  const [inputVal, setInputVal] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const toast = useToast();

  return (
    <ToastProvider>
      <div className="space-y-8 p-6">
        <h1 className="text-lg font-medium text-text-1">UI Kit (프리미티브 데모)</h1>

        <Card title="Button">
          <div className="flex flex-wrap gap-2">
            <Button variant="primary">Primary</Button>
            <Button variant="secondary">Secondary</Button>
            <Button variant="destructive">Destructive</Button>
            <Button variant="ghost">Ghost</Button>
            <Button disabled>Disabled</Button>
          </div>
        </Card>

        <Card title="Input & Select">
          <div className="grid max-w-md gap-3">
            <Input
              placeholder="입력 예시"
              value={inputVal}
              onChange={(e) => setInputVal(e.target.value)}
              aria-label="데모 입력"
            />
            <Input placeholder="에러 상태" error aria-label="에러 입력" />
            <Select
              options={selectOptions}
              placeholder="선택하세요"
              aria-label="데모 선택"
            />
          </div>
        </Card>

        <Card title="StatusChip (색 + 텍스트)">
          <div className="flex flex-wrap gap-2">
            <StatusChip tone="green" label="RUNNING" />
            <StatusChip tone="red" label="STOPPED" />
            <StatusChip tone="yellow" label="THROTTLED" />
            <StatusChip tone="cyan" label="SENT" />
            <StatusChip tone="neutral" label="PREPARED" />
          </div>
        </Card>

        <Card title="Table (숫자 tabular-nums)">
          <Table<DemoRow>
            columns={[
              { key: "market", header: "마켓" },
              { key: "side", header: "방향" },
              { key: "amount", header: "금액", numeric: true },
              {
                key: "status",
                header: "상태",
                render: (row) => (
                  <StatusChip
                    tone={
                      row.status === "ACKED"
                        ? "green"
                        : row.status === "THROTTLED"
                          ? "yellow"
                          : "neutral"
                    }
                    label={row.status}
                  />
                ),
              },
            ]}
            rows={demoRows}
            getRowKey={(r) => r.id}
            onRowClick={(r) => toast.add(`클릭: ${r.market}`, "info")}
          />
        </Card>

        <Card title="Modal (Confirm)">
          <Button variant="secondary" onClick={() => setModalOpen(true)}>
            확인 모달 열기
          </Button>
          <ConfirmModal
            open={modalOpen}
            onClose={() => setModalOpen(false)}
            title="확인"
            confirmLabel="확인"
            cancelLabel="취소"
            onConfirm={() => toast.add("확인했습니다.", "success")}
          >
            실행하시겠습니까?
          </ConfirmModal>
        </Card>

        <Card title="Drawer">
          <Button variant="secondary" onClick={() => setDrawerOpen(true)}>
            드로어 열기
          </Button>
          <Drawer
            open={drawerOpen}
            onClose={() => setDrawerOpen(false)}
            title="상세"
          >
            <p className="text-sm text-text-2">드로어 본문입니다.</p>
          </Drawer>
        </Card>

        <Card title="Toast">
          <div className="flex flex-wrap gap-2">
            <Button
              variant="secondary"
              onClick={() => toast.add("저장되었습니다.", "success")}
            >
              Success Toast
            </Button>
            <Button
              variant="secondary"
              onClick={() => toast.add("오류가 발생했습니다.", "error")}
            >
              Error Toast
            </Button>
            <Button
              variant="secondary"
              onClick={() => toast.add("정보 메시지", "info")}
            >
              Info Toast
            </Button>
          </div>
        </Card>
      </div>
    </ToastProvider>
  );
}
