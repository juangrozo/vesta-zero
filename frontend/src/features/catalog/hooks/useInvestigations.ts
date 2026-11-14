"use client";

import { useApiResource } from "@/shared/lib/http/useApiResource";
import { fetchInvestigations } from "../api";
import type { CatalogFilter } from "../types";

/** Catalog rows for the current filter; refetches when any filter value changes. */
export function useInvestigations(filter: CatalogFilter) {
  const { type, hardware, fuelState, pillar } = filter;
  return useApiResource(JSON.stringify([type, hardware, fuelState, pillar]), (signal) =>
    fetchInvestigations({ type, hardware, fuelState, pillar }, signal),
  );
}
