import { http } from "@/shared/lib/http/client";
import type { InvestigationDetail, InvestigationExtraction, TestConditionRow } from "./types";

export function fetchInvestigation(psiId: string, signal?: AbortSignal) {
  return http.get<InvestigationDetail>(`/api/investigations/${encodeURIComponent(psiId)}`, signal);
}

export function fetchTestConditions(psiId: string, signal?: AbortSignal) {
  return http.get<TestConditionRow[]>(
    `/api/investigations/${encodeURIComponent(psiId)}/test-conditions`,
    signal,
  );
}

export function fetchExtraction(psiId: string, signal?: AbortSignal) {
  return http.get<InvestigationExtraction>(`/api/investigations/${encodeURIComponent(psiId)}/extraction`, signal);
}
