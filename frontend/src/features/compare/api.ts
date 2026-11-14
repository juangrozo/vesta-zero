import type { AssistantAnswer } from "@/features/assistant/types";
import { http } from "@/shared/lib/http/client";
import type { Comparison } from "./types";

export function fetchComparison(psiIds: string[], signal?: AbortSignal) {
  return http.post<Comparison>("/api/compare", { psiIds }, signal);
}

export function fetchComparisonNarrative(psiIds: string[], signal?: AbortSignal) {
  return http.post<AssistantAnswer>("/api/compare/narrative", { psiIds }, signal);
}
