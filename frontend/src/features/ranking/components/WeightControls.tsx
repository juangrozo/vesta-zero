"use client";

import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import type { Criterion } from "../types";
import { criterionColor } from "./criterionColors";

interface Props {
  criteria: Criterion[];
  weights: Record<string, number>;
  onChange: (weights: Record<string, number>) => void;
  onReset: () => void;
}

/** One slider per criterion (0–100). The backend normalizes weights so they always sum to 100 %. */
export function WeightControls({ criteria, weights, onChange, onReset }: Props) {
  const total = criteria.reduce((sum, c) => sum + (weights[c.id] ?? 0), 0);
  return (
    <section aria-labelledby="weights-heading" className="flex flex-col gap-4">
      <div className="flex items-baseline justify-between gap-2">
        <h2 id="weights-heading" className="text-lg font-semibold tracking-tight">
          Weights
        </h2>
        <button
          type="button"
          onClick={onReset}
          className="text-sm text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
        >
          Reset to defaults
        </button>
      </div>
      {criteria.map((c, i) => {
        const value = weights[c.id] ?? 0;
        const share = total > 0 ? Math.round((value / total) * 100) : 0;
        return (
          <div key={c.id} className="flex flex-col gap-1">
            <label htmlFor={`w-${c.id}`} className="flex items-center justify-between gap-2 text-sm">
              <span className="flex items-center gap-2 font-medium">
                <span aria-hidden className={`h-2.5 w-2.5 rounded-full ${criterionColor(i).dot}`} />
                {c.name}
              </span>
              <span className="tabular-nums text-zinc-600 dark:text-zinc-400">{share} %</span>
            </label>
            <input
              id={`w-${c.id}`}
              type="range"
              min={0}
              max={100}
              step={5}
              value={value}
              onChange={(e) => onChange({ ...weights, [c.id]: Number(e.target.value) })}
              className="w-full accent-sky-600"
              aria-describedby={`d-${c.id}`}
            />
            <p id={`d-${c.id}`} className="flex flex-wrap items-center gap-1.5 text-xs text-zinc-500">
              <ProvenanceBadge kind={c.provenance} />
              {c.description}
            </p>
          </div>
        );
      })}
      {total === 0 && (
        <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
          Set at least one weight above zero.
        </p>
      )}
    </section>
  );
}
