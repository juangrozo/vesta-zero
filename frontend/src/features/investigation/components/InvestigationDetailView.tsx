"use client";

import Link from "next/link";
import type { ReactNode } from "react";
import { FUEL_LABELS, HARDWARE_LABELS, PILLAR_LABELS } from "@/features/catalog/labels";
import { formatDate, formatFlow, formatO2, formatPressure } from "@/shared/lib/format";
import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import { useInvestigation } from "../hooks/useInvestigation";
import type { InvestigationDetail } from "../types";
import { CitationBox } from "./CitationBox";
import { TestConditionsTable } from "./TestConditionsTable";

/** Investigation detail page body (spec F2). */
export function InvestigationDetailView({ psiId }: { psiId: string }) {
  const state = useInvestigation(psiId);

  if (state.status === "loading") {
    return <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading {psiId}…</p>;
  }
  if (state.status === "error") {
    return (
      <div role="alert" className="flex flex-col gap-3">
        <p className="rounded-md border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-900 dark:border-rose-800 dark:bg-rose-950/40 dark:text-rose-200">
          Could not load {psiId}. {state.message}
        </p>
        <BackLink />
      </div>
    );
  }
  return <Detail d={state.data} />;
}

function Detail({ d }: { d: InvestigationDetail }) {
  const period = [formatDate(d.startDate), formatDate(d.endDate)].filter(Boolean).join(" – ");
  return (
    <article className="flex flex-col gap-8">
      <header className="flex flex-col gap-3">
        <BackLink />
        <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">{d.acronym ?? d.psiId}</h1>
          <span className="font-mono text-sm text-zinc-500">{d.psiId}</span>
        </div>
        <p className="text-lg text-zinc-700 dark:text-zinc-300">{d.title}</p>
        <div className="flex flex-wrap items-center gap-2 text-xs">
          <Chip>{d.type === "FLIGHT" ? "Flight (microgravity)" : "Ground study"}</Chip>
          <Chip>{PILLAR_LABELS[d.classification.safetyPillar]}</Chip>
          <Chip>{FUEL_LABELS[d.classification.fuelState]}</Chip>
          <Chip>{HARDWARE_LABELS[d.classification.hardwareFamily]}</Chip>
          <ProvenanceBadge kind="CURATED" />
        </div>
      </header>

      <CitationBox citation={d.citation} doiUrl={d.doiUrl} />
      <p className="-mt-4 text-sm">
        <Link
          href={`/compare?ids=${d.psiId}`}
          className="text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
        >
          Compare {d.acronym ?? d.psiId} with other investigations →
        </Link>
      </p>

      <Section title="Tested conditions" badge="DOCUMENTED">
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <Stat label="Oxygen" value={formatO2(d.conditions.o2Fraction)} />
          <Stat label="Pressure" value={formatPressure(d.conditions.pressureAtm)} />
          <Stat label="Flow velocity" value={formatFlow(d.conditions.flowCmPerS)} />
        </dl>
        <p className="mt-3 text-sm text-zinc-600 dark:text-zinc-400">
          {d.conditions.testRows > 0
            ? `${d.conditions.testRows} test rows in ${d.conditions.sourceFiles.length} table(s); ${d.conditions.normalizedRows} with normalized O₂, pressure or flow.`
            : "No experimental table is documented in PSI for this investigation."}
        </p>
        {d.conditions.fuelsOrMaterials.length > 0 && (
          <details className="mt-2 text-sm">
            <summary className="cursor-pointer text-zinc-700 dark:text-zinc-300">
              Fuels and materials ({d.conditions.fuelsOrMaterials.length})
            </summary>
            <ul className="mt-2 flex flex-wrap gap-1.5">
              {d.conditions.fuelsOrMaterials.map((f) => (
                <li key={f} className="rounded bg-zinc-100 px-2 py-0.5 text-xs dark:bg-zinc-800">
                  {f}
                </li>
              ))}
            </ul>
          </details>
        )}
      </Section>

      <Section title="Overview" badge="DOCUMENTED">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <TextBlock title="Objective" text={d.objective} />
          <TextBlock title="Approach" text={d.approach} />
          <TextBlock title="Hypothesis" text={d.hypothesis} />
          <TextBlock title="Research impacts / Earth benefits" text={d.researchImpacts} />
        </div>
      </Section>

      <Section title="Facts" badge="DOCUMENTED">
        <dl className="grid grid-cols-1 gap-x-8 gap-y-3 text-sm sm:grid-cols-2">
          <Fact label="Hardware" value={d.hardware} />
          <Fact label="Flight program" value={d.flightProgram} />
          <Fact label="Research area" value={d.researchArea} />
          <Fact label="Sub-area" value={d.subArea} />
          <Fact label="Project type" value={d.projectType} />
          <Fact label="Investigation period" value={period || null} />
          <Fact label="License" value={d.license} />
          <Fact label="DOI" value={d.doi} />
        </dl>
      </Section>

      <Section title="Test conditions" badge="DOCUMENTED">
        <TestConditionsTable psiId={d.psiId} />
      </Section>

      <Section title={`Publications (${d.publications.length})`} badge="DOCUMENTED">
        {d.publications.length === 0 ? (
          <p className="text-sm text-zinc-600 dark:text-zinc-400">No publications listed in PSI.</p>
        ) : (
          <ol className="flex flex-col gap-3 text-sm">
            {d.publications.map((p, i) => (
              <li key={`${p.doi ?? p.title}-${i}`}>
                <p className="text-zinc-900 dark:text-zinc-100">{p.title || "Untitled"}</p>
                <p className="text-zinc-600 dark:text-zinc-400">
                  {p.authors}
                  {p.doiUrl && (
                    <>
                      {" · "}
                      <a
                        href={p.doiUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="font-mono text-xs text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
                      >
                        {p.doi}
                      </a>
                    </>
                  )}
                </p>
              </li>
            ))}
          </ol>
        )}
      </Section>

      <Section title={`Data quality (${d.dataQualityWarnings.length})`}>
        {d.dataQualityWarnings.length === 0 ? (
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
            Every mapped value in the experimental tables was normalized.
          </p>
        ) : (
          <>
            <p className="mb-2 text-sm text-zinc-600 dark:text-zinc-400">
              These values could not be normalized and are not used in ranges. They are never guessed.
            </p>
            <ul className="flex flex-col gap-1 text-xs">
              {d.dataQualityWarnings.map((w, i) => (
                <li key={i} className="font-mono text-zinc-700 dark:text-zinc-300">
                  {w.rowIndex != null && `row ${w.rowIndex} · `}
                  {w.column && `${w.column} · `}
                  {w.rawValue != null && `“${w.rawValue}” · `}
                  <span className="text-zinc-500">{w.reason}</span>
                </li>
              ))}
            </ul>
          </>
        )}
      </Section>
    </article>
  );
}

function BackLink() {
  return (
    <Link
      href="/"
      className="text-sm text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
    >
      ← All investigations
    </Link>
  );
}

function Section({
  title,
  badge,
  children,
}: {
  title: string;
  badge?: "DOCUMENTED" | "CURATED";
  children: ReactNode;
}) {
  return (
    <section className="flex flex-col gap-3">
      <div className="flex items-center gap-2 border-b border-zinc-200 pb-1 dark:border-zinc-800">
        <h2 className="text-lg font-semibold tracking-tight">{title}</h2>
        {badge && <ProvenanceBadge kind={badge} />}
      </div>
      {children}
    </section>
  );
}

function Chip({ children }: { children: ReactNode }) {
  return (
    <span className="rounded-full bg-zinc-100 px-2.5 py-0.5 font-medium text-zinc-800 dark:bg-zinc-800 dark:text-zinc-200">
      {children}
    </span>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  const missing = value === "Not documented";
  return (
    <div className="rounded-md border border-zinc-200 p-3 dark:border-zinc-800">
      <dt className="text-xs uppercase tracking-wide text-zinc-500">{label}</dt>
      <dd className={`mt-1 font-medium ${missing ? "text-zinc-400" : "text-zinc-900 dark:text-zinc-100"}`}>
        {value}
      </dd>
    </div>
  );
}

function TextBlock({ title, text }: { title: string; text: string | null }) {
  return (
    <div>
      <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-200">{title}</h3>
      <p className={`mt-1 whitespace-pre-line text-sm ${text ? "text-zinc-700 dark:text-zinc-300" : "text-zinc-400"}`}>
        {text ?? "Not documented"}
      </p>
    </div>
  );
}

function Fact({ label, value }: { label: string; value: string | null }) {
  return (
    <div>
      <dt className="text-xs uppercase tracking-wide text-zinc-500">{label}</dt>
      <dd className={value ? "text-zinc-900 dark:text-zinc-100" : "text-zinc-400"}>{value ?? "Not documented"}</dd>
    </div>
  );
}
