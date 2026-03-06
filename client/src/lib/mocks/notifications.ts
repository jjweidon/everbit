import type { PushSubscriptionItem } from "@/types/api-contracts";

export const mockPushSubscriptions: PushSubscriptionItem[] = [
  {
    id: "sub-01",
    endpointMasked: "https://fcm.googleapis.com/...abc123",
    userAgent: "Chrome/122.0",
    enabled: true,
  },
  {
    id: "sub-02",
    endpointMasked: "https://fcm.googleapis.com/...def456",
    userAgent: "Safari/17.2",
    enabled: false,
  },
];
