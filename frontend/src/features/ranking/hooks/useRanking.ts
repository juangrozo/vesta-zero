"use client";

import { useApiResource } from "@/shared/lib/http/useApiResource";
import { fetchCriteria, fetchRanking } from "../api";

export function useCriteria() {
  return useApiResource("ranking-criteria", (signal) => fetchCriteria(signal));
}

/** Ranking for the given weights; `null` weights means "not ready yet" and stays loading. */
export function useRanking(weights: Record<string, number> | null) {
  const key = weights ? JSON.stringify(Object.entries(weights).sort()) : "pending";
  return useApiResource(key, (signal) =>
    weights ? fetchRanking(weights, signal) : new Promise<never>(() => {}),
  );
}
