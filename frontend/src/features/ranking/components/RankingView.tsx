"use client";

import { useState } from "react";
import { useDebouncedValue } from "@/shared/lib/useDebouncedValue";
import { useCriteria, useRanking } from "../hooks/useRanking";
import type { Criterion } from "../types";
import { RankingResults } from "./RankingResults";
import { WeightControls } from "./WeightControls";

function defaultsOf(criteria: Criterion[]): Record<string, number> {
  return Object.fromEntries(criteria.map((c) => [c.id, Math.round(c.defaultWeight * 100)]));
}

/** Explainable ranking by relevance to a lunar habitat (spec F3). */
export function RankingView() {
  const criteria = useCriteria();
  if (criteria.status === "loading") {
    return <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading criteria…</p>;
  }
  if (criteria.status === "error") {
    return (
      <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
        Could not load ranking criteria. Is the backend running? ({criteria.message})
      </p>
    );
  }
  return <Ranking criteria={criteria.data} />;
}

function Ranking({ criteria }: { criteria: Criterion[] }) {
  const [weights, setWeights] = useState(() => defaultsOf(criteria));
  const debounced = useDebouncedValue(weights, 300);
  const hasPositive = Object.values(debounced).some((w) => w > 0);
  const ranking = useRanking(hasPositive ? debounced : null);

  return (
    <div className="grid grid-cols-1 gap-8 lg:grid-cols-[minmax(0,320px)_minmax(0,1fr)]">
      <div className="lg:sticky lg:top-6 lg:self-start">
        <WeightControls
          criteria={criteria}
          weights={weights}
          onChange={setWeights}
          onReset={() => setWeights(defaultsOf(criteria))}
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
          <p className="text-sm text-zinc-600 dark:text-zinc-400">{hasPositive ? "Ranking…" : "Waiting for weights…"}</p>
        )}
        {ranking.status === "error" && (
          <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
            Could not rank investigations ({ranking.message})
          </p>
        )}
        {ranking.status === "ready" && (
          <>
            <p className="text-sm text-zinc-600 dark:text-zinc-400">
              Lunar habitat target: ≥ {ranking.data.lunarTarget.o2Fraction * 100} % O₂ at ≤{" "}
              {ranking.data.lunarTarget.pressurePsi} psi ({ranking.data.lunarTarget.pressureAtm.toFixed(3)} atm).
              Earth: 21 % O₂ at 14.7 psi.
            </p>
            <RankingResults criteria={criteria} results={ranking.data.results} />
          </>
        )}
      </section>
    </div>
  );
}
