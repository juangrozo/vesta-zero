import Link from "next/link";
import type { ReactNode } from "react";
import { FUEL_LABELS, HARDWARE_LABELS, PILLAR_LABELS } from "@/features/catalog/labels";
import { formatDate, formatFlow, formatO2, formatPressure } from "@/shared/lib/format";
import { ProvenanceBadge, type Provenance } from "@/shared/ui/ProvenanceBadge";
import type { CompareItem } from "../types";

const MAX_FUELS = 8;

interface Row {
  label: string;
  provenance: Provenance;
  render: (item: CompareItem) => ReactNode;
}

const ROWS: Row[] = [
  { label: "Type", provenance: "DOCUMENTED", render: (i) => (i.type === "FLIGHT" ? "Flight (microgravity)" : "Ground study") },
  { label: "Sub-area", provenance: "DOCUMENTED", render: (i) => i.subArea },
  { label: "Hardware", provenance: "DOCUMENTED", render: (i) => i.hardware },
  { label: "Hardware family", provenance: "CURATED", render: (i) => HARDWARE_LABELS[i.classification.hardwareFamily] },
  { label: "Fuel state", provenance: "CURATED", render: (i) => FUEL_LABELS[i.classification.fuelState] },
  { label: "Safety pillar", provenance: "CURATED", render: (i) => PILLAR_LABELS[i.classification.safetyPillar] },
  { label: "Oxygen", provenance: "DOCUMENTED", render: (i) => formatO2(i.conditions.o2Fraction) },
  { label: "Pressure", provenance: "DOCUMENTED", render: (i) => formatPressure(i.conditions.pressureAtm) },
  { label: "Flow velocity", provenance: "DOCUMENTED", render: (i) => formatFlow(i.conditions.flowCmPerS) },
  {
    label: "Test rows",
    provenance: "DOCUMENTED",
    render: (i) =>
      i.conditions.testRows === 0
        ? "No experimental table"
        : `${i.conditions.testRows} (${i.conditions.normalizedRows} with normalized conditions)`,
  },
  {
    label: "Fuels / materials",
    provenance: "DOCUMENTED",
    render: (i) => {
      const fuels = i.conditions.fuelsOrMaterials;
      if (fuels.length === 0) return null;
      const shown = fuels.slice(0, MAX_FUELS).join("; ");
      return fuels.length > MAX_FUELS ? `${shown}; +${fuels.length - MAX_FUELS} more` : shown;
    },
  },
  {
    label: "Period",
    provenance: "DOCUMENTED",
    render: (i) => [formatDate(i.startDate), formatDate(i.endDate)].filter(Boolean).join(" – ") || null,
  },
  { label: "Objective", provenance: "DOCUMENTED", render: (i) => <Clamp text={i.objective} /> },
];

/** Side-by-side table; on narrow screens each investigation becomes its own card. */
export function ComparisonTable({ items }: { items: CompareItem[] }) {
  return (
    <>
      <div className="relative hidden overflow-x-auto rounded-md border border-zinc-200 md:block dark:border-zinc-800">
        <table className="w-full table-fixed border-collapse text-left text-sm">
          <thead className="bg-zinc-50 dark:bg-zinc-900">
            <tr>
              <th scope="col" className="w-44 px-3 py-2 text-xs uppercase tracking-wide text-zinc-500">
                Aspect
              </th>
              {items.map((item) => (
                <th key={item.psiId} scope="col" className="px-3 py-2 align-top">
                  <Header item={item} />
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
            {ROWS.map((row) => (
              <tr key={row.label} className="align-top">
                <th scope="row" className="px-3 py-2 font-medium text-zinc-700 dark:text-zinc-300">
                  <span className="flex flex-col items-start gap-1">
                    {row.label}
                    <ProvenanceBadge kind={row.provenance} />
                  </span>
                </th>
                {items.map((item) => (
                  <td key={item.psiId} className="break-words px-3 py-2 text-zinc-800 dark:text-zinc-200">
                    <Value>{row.render(item)}</Value>
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ul className="flex flex-col gap-4 md:hidden">
        {items.map((item) => (
          <li key={item.psiId} className="rounded-md border border-zinc-200 p-3 dark:border-zinc-800">
            <Header item={item} />
            <dl className="mt-3 flex flex-col gap-2 text-sm">
              {ROWS.map((row) => (
                <div key={row.label}>
                  <dt className="flex items-center gap-2 text-xs uppercase tracking-wide text-zinc-500">
                    {row.label} <ProvenanceBadge kind={row.provenance} />
                  </dt>
                  <dd className="break-words text-zinc-800 dark:text-zinc-200">
                    <Value>{row.render(item)}</Value>
                  </dd>
                </div>
              ))}
            </dl>
          </li>
        ))}
      </ul>
    </>
  );
}

function Header({ item }: { item: CompareItem }) {
  return (
    <div className="flex flex-col gap-0.5 font-normal">
      <Link
        href={`/investigations/${item.psiId}`}
        className="font-semibold text-zinc-900 underline-offset-2 hover:underline dark:text-zinc-100"
      >
        {item.acronym ?? item.psiId} <span className="font-mono text-xs font-normal text-zinc-500">{item.psiId}</span>
      </Link>
      <span className="text-xs text-zinc-600 dark:text-zinc-400">{item.title}</span>
      <a
        href={item.doiUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="font-mono text-xs text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
      >
        {item.doi}
      </a>
    </div>
  );
}

function Value({ children }: { children: ReactNode }) {
  const missing = children === null || children === undefined || children === "" || children === "Not documented";
  return missing ? <span className="text-zinc-400">Not documented</span> : <>{children}</>;
}

function Clamp({ text }: { text: string | null }) {
  if (!text) return null;
  if (text.length <= 280) return <span className="whitespace-pre-line">{text}</span>;
  return (
    <details>
      <summary className="cursor-pointer whitespace-pre-line">{text.slice(0, 280).trimEnd()}… <span className="text-sky-700 dark:text-sky-400">more</span></summary>
      <span className="whitespace-pre-line">{text.slice(280)}</span>
    </details>
  );
}
