"use client";

import type { HabitatEnvironment, TargetBound } from "../types";

interface Props {
  environments: HabitatEnvironment[];
  selected: string;
  onSelect: (id: string) => void;
}

const BOUND: Record<TargetBound, string> = {
  AT_LEAST: "≥ ",
  AT_MOST: "≤ ",
  AROUND: "",
};

export function targetText(e: HabitatEnvironment) {
  return `${BOUND[e.o2Bound]}${Math.round(e.o2Fraction * 100)} % O₂ at ${BOUND[e.pressureBound]}${e.pressurePsi} psi (${e.pressureAtm.toFixed(3)} atm)`;
}

/** Crew environment whose atmosphere the ranking targets. Targets the challenge does not give are flagged. */
export function EnvironmentPicker({ environments, selected, onSelect }: Props) {
  const current = environments.find((e) => e.id === selected);
  return (
    <section aria-labelledby="environment-heading" className="flex flex-col gap-3">
      <h2 id="environment-heading" className="text-lg font-semibold tracking-tight">
        Crew environment
      </h2>
      <div role="radiogroup" aria-labelledby="environment-heading" className="flex flex-wrap gap-2">
        {environments.map((e) => {
          const active = e.id === selected;
          return (
            <button
              key={e.id}
              type="button"
              role="radio"
              aria-checked={active}
              onClick={() => onSelect(e.id)}
              className={`rounded-full border px-3 py-1.5 text-sm font-medium transition-colors ${
                active
                  ? "border-sky-600 bg-sky-600 text-white"
                  : "border-zinc-300 text-zinc-700 hover:border-sky-500 dark:border-zinc-700 dark:text-zinc-300"
              }`}
            >
              {e.name}
            </button>
          );
        })}
      </div>
      {current && (
        <div className="rounded-md border border-zinc-200 p-3 text-sm dark:border-zinc-800">
          <p className="flex flex-wrap items-center gap-2 font-medium text-zinc-900 dark:text-zinc-100">
            Target atmosphere: {targetText(current)}
            {current.assumption && (
              <span
                title="Not given by the challenge resources; chosen by Vesta Zero"
                className="rounded border border-amber-300 px-1.5 py-px text-[11px] font-medium uppercase tracking-wide text-amber-800 dark:border-amber-700 dark:text-amber-300"
              >
                Assumption
              </span>
            )}
          </p>
          <p className="mt-1 text-zinc-600 dark:text-zinc-400">{current.basis}</p>
          <p className="mt-1 text-xs text-zinc-500">Earth: 21 % O₂ at 14.7 psi (1 atm).</p>
        </div>
      )}
    </section>
  );
}
