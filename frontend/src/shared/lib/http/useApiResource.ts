"use client";

import { useEffect, useRef, useState } from "react";

type Settled<T> = { status: "error"; message: string } | { status: "ready"; data: T };

export type ResourceState<T> = { status: "loading" } | Settled<T>;

/**
 * Loads a backend resource identified by `key`. Refetches when the key changes and cancels stale requests.
 * The settled result is tagged with its key, so a new key reads as "loading" until it resolves.
 */
export function useApiResource<T>(
  key: string,
  load: (signal: AbortSignal) => Promise<T>,
): ResourceState<T> {
  const [result, setResult] = useState<{ key: string; state: Settled<T> } | null>(null);
  const loadRef = useRef(load);

  useEffect(() => {
    loadRef.current = load;
  });

  useEffect(() => {
    const controller = new AbortController();
    loadRef.current(controller.signal)
      .then((data) => setResult({ key, state: { status: "ready", data } }))
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        const message = error instanceof Error ? error.message : "Request failed";
        setResult({ key, state: { status: "error", message } });
      });
    return () => controller.abort();
  }, [key]);

  return result?.key === key ? result.state : { status: "loading" };
}
