"use client";

import { formatDate } from "@/shared/lib/format";
import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import { useExtraction } from "../hooks/useInvestigation";
import type { ExtractedFact } from "../types";

const GROUPS = [
  { key: "fuelsAndSamples", title: "Fuels and samples" },
  { key: "measurements", title: "Sensors and measurements" },
  { key: "testVariables", title: "What changed between tests" },
  { key: "testConstants", title: "What stayed the same" },
  { key: "earthApplications", title: "Applications on Earth" },
] as const;

/** AI-extracted summary (spec F2). Each item shows the verbatim PSI passage it was taken from. */
export function AiExtractionSection({ psiId }: { psiId: string }) {
  const state = useExtraction(psiId);

  return (
    <section className="flex flex-col gap-3">
      <div className="flex items-center gap-2 border-b border-zinc-200 pb-1 dark:border-zinc-800">
        <h2 className="text-lg font-semibold tracking-tight">AI summary</h2>
        <ProvenanceBadge kind="AI_EXTRACTED" />
      </div>
      {state.status === "loading" && <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading AI summary…</p>}
      {state.status === "error" && (
        <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
          Could not load the AI summary ({state.message})
        </p>
      )}
      {state.status === "ready" && !state.data.extracted && (
        <p className="text-sm text-zinc-600 dark:text-zinc-400">The AI extraction has not been run for this investigation.</p>
      )}
      {state.status === "ready" && state.data.extracted && (
        <>
          <p className="text-xs text-zinc-500">
            Extracted by {state.data.model} from the PSI text on {formatDate(state.data.extractedAt?.slice(0, 10) ?? null)}. Only items whose
            source passage was found word for word in the record are kept. Check the source before relying on an item.
          </p>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            {GROUPS.map((g) => (
              <FactGroup key={g.key} title={g.title} facts={state.data[g.key]} />
            ))}
          </div>
        </>
      )}
    </section>
  );
}

function FactGroup({ title, facts }: { title: string; facts: ExtractedFact[] }) {
  return (
    <div className="min-w-0">
      <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-200">{title}</h3>
      {facts.length === 0 ? (
        <p className="mt-1 text-sm text-zinc-400">Not documented</p>
      ) : (
        <ul className="mt-2 flex flex-col gap-2">
          {facts.map((f, i) => (
            <li key={`${f.label}-${i}`} className="rounded-md border border-zinc-200 p-2.5 text-sm dark:border-zinc-800">
              <p className="font-medium text-zinc-900 dark:text-zinc-100">{f.label}</p>
              {f.detail && <p className="text-zinc-700 dark:text-zinc-300">{f.detail}</p>}
              <details className="mt-1">
                <summary className="cursor-pointer text-xs text-sky-700 dark:text-sky-400">Source</summary>
                <blockquote className="mt-1 break-words border-l-2 border-violet-300 pl-2 text-xs italic text-zinc-600 dark:border-violet-700 dark:text-zinc-400">
                  “{f.sourceSpan}”
                </blockquote>
              </details>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
