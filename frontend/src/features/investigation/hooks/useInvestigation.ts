"use client";

import { useApiResource } from "@/shared/lib/http/useApiResource";
import { fetchInvestigation, fetchTestConditions } from "../api";

export function useInvestigation(psiId: string) {
  return useApiResource(`detail:${psiId}`, (signal) => fetchInvestigation(psiId, signal));
}

export function useTestConditions(psiId: string) {
  return useApiResource(`conditions:${psiId}`, (signal) => fetchTestConditions(psiId, signal));
}
