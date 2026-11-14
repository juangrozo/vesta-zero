import { http } from "@/shared/lib/http/client";
import type { Criterion, RankingResponse } from "./types";

export function fetchCriteria(signal?: AbortSignal) {
  return http.get<Criterion[]>("/api/ranking/criteria", signal);
}

export function fetchRanking(weights: Record<string, number>, signal?: AbortSignal) {
  return http.post<RankingResponse>("/api/ranking", { weights }, signal);
}
