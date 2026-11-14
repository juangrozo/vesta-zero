"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { AnswerCard } from "@/features/assistant/components/AnswerCard";
import type { AssistantAnswer } from "@/features/assistant/types";
import { useInvestigations } from "@/features/catalog/hooks/useInvestigations";
import type { InvestigationSummary } from "@/features/catalog/types";
import { useApiResource } from "@/shared/lib/http/useApiResource";
import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import { fetchComparison, fetchComparisonNarrative } from "../api";
import { ComparisonTable } from "./ComparisonTable";

const SLOTS = 3;

/** Comparator for 2–3 investigations (spec F4). The selection lives in the URL (?ids=PSI-68,PSI-69). */
export function CompareView() {
  const router = useRouter();
  const pathname = usePathname();
  const params = useSearchParams();
  const selected = (params.get("ids") ?? "")
    .split(",")
    .map((s) => s.trim().toUpperCase())
    .filter(Boolean)
    .slice(0, SLOTS);
  const catalog = useInvestigations({});

  function select(slot: number, psiId: string) {
    const next = [...selected];
    next[slot] = psiId;
    const ids = Array.from(new Set(next.filter(Boolean)));
    router.replace(ids.length > 0 ? `${pathname}?ids=${ids.join(",")}` : pathname, { scroll: false });
  }

  return (
    <div className="flex flex-col gap-8">
      <section aria-label="Select investigations" className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        {Array.from({ length: SLOTS }, (_, slot) => (
          <label key={slot} className="flex flex-col gap-1 text-sm">
            <span className="font-medium text-zinc-700 dark:text-zinc-300">
              Investigation {slot + 1}
              {slot === SLOTS - 1 && <span className="font-normal text-zinc-500"> (optional)</span>}
            </span>
            <select
              value={selected[slot] ?? ""}
              disabled={catalog.status !== "ready"}
              onChange={(e) => select(slot, e.target.value)}
              className="rounded-md border border-zinc-300 bg-white px-3 py-2 text-zinc-900 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/30 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
            >
              <option value="">{catalog.status === "ready" ? "Choose…" : "Loading…"}</option>
              {catalog.status === "ready" &&
                catalog.data.map((inv) => (
                  <option
                    key={inv.psiId}
                    value={inv.psiId}
                    disabled={selected.includes(inv.psiId) && selected[slot] !== inv.psiId}
                  >
                    {label(inv)}
                  </option>
                ))}
            </select>
          </label>
        ))}
      </section>

      {selected.length < 2 ? (
        <p className="rounded-md border border-dashed border-zinc-300 px-4 py-8 text-center text-sm text-zinc-600 dark:border-zinc-700 dark:text-zinc-400">
          Choose at least two investigations to compare them side by side.
        </p>
      ) : (
        <Comparison ids={selected} />
      )}
    </div>
  );
}

function Comparison({ ids }: { ids: string[] }) {
  const key = ids.join(",");
  const comparison = useApiResource(`compare:${key}`, (signal) => fetchComparison(ids, signal));

  if (comparison.status === "loading") {
    return <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading comparison…</p>;
  }
  if (comparison.status === "error") {
    return (
      <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
        Could not compare these investigations ({comparison.message})
      </p>
    );
  }
  const { investigations, facts } = comparison.data;
  return (
    <div className="flex flex-col gap-8">
      <ComparisonTable items={investigations} />

      <section aria-labelledby="facts-heading" className="flex flex-col gap-3">
        <h2 id="facts-heading" className="text-lg font-semibold tracking-tight">
          Similarities and differences
        </h2>
        <p className="text-xs text-zinc-500">Computed from the stored data only, without AI.</p>
        <ul className="grid grid-cols-1 gap-2 sm:grid-cols-2">
          {facts.map((f) => (
            <li key={f.dimension} className="flex gap-3 rounded-md border border-zinc-200 p-3 text-sm dark:border-zinc-800">
              <span
                className={`h-fit shrink-0 rounded px-1.5 py-0.5 text-[11px] font-semibold uppercase ${
                  f.same
                    ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900/40 dark:text-emerald-200"
                    : "bg-amber-100 text-amber-900 dark:bg-amber-900/40 dark:text-amber-200"
                }`}
              >
                {f.same ? "Same" : "Differs"}
              </span>
              <span>
                <span className="font-medium text-zinc-900 dark:text-zinc-100">{f.dimension}</span>
                <span className="block text-zinc-600 dark:text-zinc-400">{f.detail}</span>
              </span>
            </li>
          ))}
        </ul>
      </section>

      <Narrative key={key} ids={ids} />
    </div>
  );
}

type NarrativeState =
  | { status: "idle" }
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "ready"; answer: AssistantAnswer };

function Narrative({ ids }: { ids: string[] }) {
  const [state, setState] = useState<NarrativeState>({ status: "idle" });

  async function generate() {
    setState({ status: "loading" });
    try {
      setState({ status: "ready", answer: await fetchComparisonNarrative(ids) });
    } catch (error) {
      setState({ status: "error", message: error instanceof Error ? error.message : "Request failed" });
    }
  }

  return (
    <section aria-labelledby="narrative-heading" className="flex flex-col gap-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 id="narrative-heading" className="flex items-center gap-2 text-lg font-semibold tracking-tight">
          AI comparison <ProvenanceBadge kind="AI_EXTRACTED" />
        </h2>
        {state.status !== "ready" && (
          <button
            type="button"
            onClick={() => void generate()}
            disabled={state.status === "loading"}
            className="rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white hover:bg-sky-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {state.status === "loading" ? "Claude is comparing…" : "Generate AI comparison"}
          </button>
        )}
      </div>
      <div aria-live="polite">
        {state.status === "idle" && (
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
            Claude compares objectives, fuels and samples, conditions, hardware and measurements, citing each
            investigation. Grounded in the stored records only.
          </p>
        )}
        {state.status === "error" && (
          <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
            {state.message}
          </p>
        )}
        {state.status === "ready" && (
          <AnswerCard question={`Comparison of ${ids.join(", ")}`} answer={state.answer} />
        )}
      </div>
    </section>
  );
}

function label(inv: InvestigationSummary) {
  return `${inv.acronym ?? inv.psiId} (${inv.psiId})`;
}
