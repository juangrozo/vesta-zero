"use client";

import { useEffect, useState } from "react";
import { fetchInvestigations } from "../api";
import type { CatalogFilter, InvestigationSummary } from "../types";

type Settled =
  | { status: "error"; message: string }
  | { status: "ready"; items: InvestigationSummary[] };

type State = { status: "loading" } | Settled;

/**
 * Loads the catalog from the backend; refetches when the filter changes and cancels stale requests.
 * The settled result is tagged with its filter key, so a new filter reads as "loading" until it resolves.
 */
export function useInvestigations(filter: CatalogFilter): State {
  const { type, hardware, fuelState, pillar } = filter;
  const key = JSON.stringify([type, hardware, fuelState, pillar]);
  const [result, setResult] = useState<{ key: string; state: Settled } | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    fetchInvestigations({ type, hardware, fuelState, pillar }, controller.signal)
      .then((items) => setResult({ key, state: { status: "ready", items } }))
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        const message = error instanceof Error ? error.message : "Could not load investigations";
        setResult({ key, state: { status: "error", message } });
      });
    return () => controller.abort();
  }, [key, type, hardware, fuelState, pillar]);

  return result?.key === key ? result.state : { status: "loading" };
}
