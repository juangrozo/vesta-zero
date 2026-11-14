"use client";

import Link from "next/link";
import { useState } from "react";
import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import type { Criterion, RankedInvestigation } from "../types";
import { criterionColor } from "./criterionColors";

interface Props {
  criteria: Criterion[];
  results: RankedInvestigation[];
}

/** Ranked list. Each bar is split by criterion contribution, so the total is visibly explained. */
export function RankingResults({ criteria, results }: Props) {
  const [open, setOpen] = useState<string | null>(null);
  const order = new Map(criteria.map((c, i) => [c.id, i]));

  return (
    <ol className="flex flex-col gap-2">
      {results.map((r) => {
        const expanded = open === r.psiId;
        return (
          <li key={r.psiId} className="rounded-md border border-zinc-200 dark:border-zinc-800">
            <button
              type="button"
              aria-expanded={expanded}
              onClick={() => setOpen(expanded ? null : r.psiId)}
              className="flex w-full flex-col gap-2 px-3 py-2.5 text-left hover:bg-zinc-50 dark:hover:bg-zinc-900/60"
            >
              <div className="flex items-baseline gap-3">
                <span className="w-6 shrink-0 text-right font-mono text-sm text-zinc-500">{r.position}</span>
                <span className="min-w-0 flex-1">
                  <span className="font-medium text-zinc-900 dark:text-zinc-100">{r.acronym ?? r.psiId}</span>
                  <span className="ml-2 font-mono text-xs text-zinc-500">{r.psiId}</span>
                  <span className="block truncate text-sm text-zinc-600 dark:text-zinc-400">{r.title}</span>
                </span>
                <span className="shrink-0 font-mono text-sm font-semibold tabular-nums">
                  {(r.score * 100).toFixed(1)}
                </span>
              </div>
              <div
                className="ml-9 flex h-2.5 overflow-hidden rounded-full bg-zinc-100 dark:bg-zinc-800"
                role="img"
                aria-label={`Score ${(r.score * 100).toFixed(1)} of 100`}
              >
                {r.breakdown.map((c) => (
                  <span
                    key={c.criterionId}
                    className={criterionColor(order.get(c.criterionId) ?? 0).bar}
                    style={{ width: `${c.contribution * 100}%` }}
                  />
                ))}
              </div>
            </button>
            {expanded && (
              <div className="border-t border-zinc-200 px-3 py-3 dark:border-zinc-800">
                <table className="w-full text-left text-sm">
                  <thead className="text-xs uppercase tracking-wide text-zinc-500">
                    <tr>
                      <th scope="col" className="py-1 pr-3 font-medium">Criterion</th>
                      <th scope="col" className="py-1 pr-3 text-right font-medium">Score</th>
                      <th scope="col" className="py-1 pr-3 text-right font-medium">× Weight</th>
                      <th scope="col" className="py-1 text-right font-medium">= Points</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
                    {r.breakdown.map((c) => (
                      <tr key={c.criterionId} className="align-top">
                        <td className="py-1.5 pr-3">
                          <span className="flex items-center gap-2 font-medium">
                            <span
                              aria-hidden
                              className={`h-2.5 w-2.5 shrink-0 rounded-full ${criterionColor(order.get(c.criterionId) ?? 0).dot}`}
                            />
                            {c.criterionName}
                            <ProvenanceBadge kind={c.provenance} />
                          </span>
                          <span className="mt-0.5 block text-xs text-zinc-600 dark:text-zinc-400">{c.reason}</span>
                        </td>
                        <td className="py-1.5 pr-3 text-right font-mono tabular-nums">{c.score.toFixed(2)}</td>
                        <td className="py-1.5 pr-3 text-right font-mono tabular-nums">{(c.weight * 100).toFixed(0)} %</td>
                        <td className="py-1.5 text-right font-mono tabular-nums">{(c.contribution * 100).toFixed(1)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <div className="mt-2 flex flex-wrap items-center justify-between gap-2 text-sm">
                  <Link
                    href={`/investigations/${r.psiId}`}
                    className="text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
                  >
                    Open investigation
                  </Link>
                  <a
                    href={r.doiUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="font-mono text-xs text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
                  >
                    DOI
                  </a>
                </div>
              </div>
            )}
          </li>
        );
      })}
    </ol>
  );
}
