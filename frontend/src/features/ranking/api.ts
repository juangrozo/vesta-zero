import { http } from "@/shared/lib/http/client";
import type { Criterion, HabitatEnvironment, RankingResponse } from "./types";

export function fetchEnvironments(signal?: AbortSignal) {
  return http.get<HabitatEnvironment[]>("/api/ranking/environments", signal);
}

export function fetchCriteria(environment: string, signal?: AbortSignal) {
  return http.get<Criterion[]>(`/api/ranking/criteria?environment=${encodeURIComponent(environment)}`, signal);
}

export function fetchRanking(environment: string, weights: Record<string, number>, signal?: AbortSignal) {
  return http.post<RankingResponse>("/api/ranking", { environment, weights }, signal);
}
