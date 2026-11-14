"use client";

import { useApiResource } from "@/shared/lib/http/useApiResource";
import { fetchCriteria, fetchEnvironments, fetchRanking } from "../api";

export function useEnvironments() {
  return useApiResource("ranking-environments", (signal) => fetchEnvironments(signal));
}

export function useCriteria(environment: string) {
  return useApiResource(`ranking-criteria:${environment}`, (signal) => fetchCriteria(environment, signal));
}

/** Ranking for the given environment and weights; `null` weights means "not ready yet" and stays loading. */
export function useRanking(environment: string, weights: Record<string, number> | null) {
  const key = weights ? `${environment}:${JSON.stringify(Object.entries(weights).sort())}` : "pending";
  return useApiResource(key, (signal) =>
    weights ? fetchRanking(environment, weights, signal) : new Promise<never>(() => {}),
  );
}
