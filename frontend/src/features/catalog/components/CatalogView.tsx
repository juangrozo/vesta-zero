"use client";

import { useState } from "react";
import { useInvestigations } from "../hooks/useInvestigations";
import type { CatalogFilter } from "../types";
import { CatalogFilters } from "./CatalogFilters";
import { CatalogTable } from "./CatalogTable";

/** Catalog of in-scope PSI fire investigations with filters (spec F1). */
export function CatalogView() {
  const [filter, setFilter] = useState<CatalogFilter>({});
  const state = useInvestigations(filter);

  return (
    <section className="flex flex-col gap-4" aria-labelledby="catalog-heading">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 id="catalog-heading" className="text-xl font-semibold tracking-tight">
          Investigations
        </h2>
        {state.status === "ready" && (
          <p className="text-sm text-zinc-600 dark:text-zinc-400" aria-live="polite">
            {state.data.length} shown
          </p>
        )}
      </div>

      <CatalogFilters filter={filter} onChange={setFilter} />

      {state.status === "loading" && (
        <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading investigations…</p>
      )}
      {state.status === "error" && (
        <p role="alert" className="rounded-md border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-900 dark:border-rose-800 dark:bg-rose-950/40 dark:text-rose-200">
          Could not load investigations. Is the backend running? ({state.message})
        </p>
      )}
      {state.status === "ready" && <CatalogTable items={state.data} />}

      <p className="text-xs text-zinc-500">
        Hardware, fuel state and safety pillar are curated by Vesta Zero from PSI metadata and the challenge
        resources. All other fields are documented by NASA PSI (CC0-1.0). Cite each investigation by its DOI.
      </p>
    </section>
  );
}
