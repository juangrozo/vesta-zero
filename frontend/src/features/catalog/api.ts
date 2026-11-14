import { http } from "@/shared/lib/http/client";
import type { CatalogFilter, InvestigationSummary } from "./types";

export function fetchInvestigations(filter: CatalogFilter, signal?: AbortSignal) {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(filter)) {
    if (value) params.set(key, value);
  }
  const query = params.toString();
  return http.get<InvestigationSummary[]>(`/api/investigations${query ? `?${query}` : ""}`, signal);
}
