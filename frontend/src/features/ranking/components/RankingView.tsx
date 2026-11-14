"use client";

import { useState } from "react";
import { useDebouncedValue } from "@/shared/lib/useDebouncedValue";
import { useCriteria, useEnvironments, useRanking } from "../hooks/useRanking";
import type { Criterion, HabitatEnvironment } from "../types";
import { EnvironmentPicker } from "./EnvironmentPicker";
import { RankingResults } from "./RankingResults";
import { WeightControls } from "./WeightControls";

function defaultsOf(criteria: Criterion[]): Record<string, number> {
  return Object.fromEntries(criteria.map((c) => [c.id, Math.round(c.defaultWeight * 100)]));
}

const DEFAULT_ENVIRONMENT = "LUNAR_BASE_ROVER";

/** Explainable ranking by relevance to a crew environment (spec F3). */
export function RankingView() {
  const environments = useEnvironments();
  const criteria = useCriteria(DEFAULT_ENVIRONMENT);
  if (criteria.status === "loading" || environments.status === "loading") {
    return <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading criteria…</p>;
  }
  if (criteria.status === "error" || environments.status === "error") {
    const message =
      criteria.status === "error" ? criteria.message : environments.status === "error" ? environments.message : "";
    return (
      <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
        Could not load ranking criteria. Is the backend running? ({message})
      </p>
    );
  }
  return <Ranking initialCriteria={criteria.data} environments={environments.data} />;
}

function Ranking({
  initialCriteria,
  environments,
}: {
  initialCriteria: Criterion[];
  environments: HabitatEnvironment[];
}) {
  const [environment, setEnvironment] = useState(DEFAULT_ENVIRONMENT);
  const [weights, setWeights] = useState(() => defaultsOf(initialCriteria));
  const envCriteria = useCriteria(environment);
  // Criterion ids are the same for every environment; only descriptions change. Keep showing the previous ones while loading.
  const criteria = envCriteria.status === "ready" ? envCriteria.data : initialCriteria;
  const debounced = useDebouncedValue(weights, 300);
  const hasPositive = Object.values(debounced).some((w) => w > 0);
  const ranking = useRanking(environment, hasPositive ? debounced : null);

  return (
    <div className="flex flex-col gap-8">
      <EnvironmentPicker environments={environments} selected={environment} onSelect={setEnvironment} />
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-[minmax(0,320px)_minmax(0,1fr)]">
        <div className="lg:sticky lg:top-6 lg:self-start">
          <WeightControls
            criteria={criteria}
            weights={weights}
            onChange={setWeights}
            onReset={() => setWeights(defaultsOf(initialCriteria))}
          />
        </div>
        <section aria-labelledby="results-heading" className="flex min-w-0 flex-col gap-3">
          <div className="flex flex-wrap items-baseline justify-between gap-2">
            <h2 id="results-heading" className="text-lg font-semibold tracking-tight">
              Ranked investigations
            </h2>
            <p className="text-xs text-zinc-500">Score out of 100 · click a row for the breakdown</p>
          </div>
          {ranking.status === "loading" && (
            <p className="text-sm text-zinc-600 dark:text-zinc-400">
              {hasPositive ? "Ranking…" : "Waiting for weights…"}
            </p>
          )}
          {ranking.status === "error" && (
            <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
              Could not rank investigations ({ranking.message})
            </p>
          )}
          {ranking.status === "ready" && (
            <>
              {ranking.data.results.every((r) => r.breakdown[0]?.score === 0) && (
                <p className="rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-900 dark:border-amber-800 dark:bg-amber-950/40 dark:text-amber-200">
                  No documented PSI test reaches this atmosphere, so atmosphere proximity adds nothing here and the
                  order comes from the other criteria. That absence is itself a data gap.
                </p>
              )}
              <RankingResults criteria={criteria} results={ranking.data.results} />
            </>
          )}
        </section>
      </div>
    </div>
  );
}
