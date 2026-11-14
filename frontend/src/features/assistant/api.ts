import { http } from "@/shared/lib/http/client";
import type { AssistantAnswer } from "./types";

export function askAssistant(question: string, signal?: AbortSignal) {
  return http.post<AssistantAnswer>("/api/assistant/ask", { question }, signal);
}
