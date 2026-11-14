import Link from "next/link";
import { FUEL_LABELS, HARDWARE_LABELS, PILLAR_LABELS } from "../labels";
import type { InvestigationSummary } from "../types";

export function CatalogTable({ items }: { items: InvestigationSummary[] }) {
  if (items.length === 0) {
    return (
      <p className="rounded-md border border-dashed border-zinc-300 px-4 py-8 text-center text-sm text-zinc-600 dark:border-zinc-700 dark:text-zinc-400">
        No investigations match these filters.
      </p>
    );
  }
  return (
    <>
      <ul className="flex flex-col gap-3 md:hidden">
        {items.map((item) => (
          <li key={item.psiId} className="rounded-md border border-zinc-200 p-3 text-sm dark:border-zinc-800">
            <div className="flex items-baseline justify-between gap-2">
              <span className="font-medium text-zinc-900 dark:text-zinc-100"><Link href={`/investigations/${item.psiId}`} className="underline-offset-2 hover:underline">{item.acronym ?? item.psiId}</Link></span>
              <span className="font-mono text-xs text-zinc-500">{item.psiId}</span>
            </div>
            <p className="mt-0.5 text-zinc-600 dark:text-zinc-400">{item.title}</p>
            <div className="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-zinc-700 dark:text-zinc-300">
              <PillarBadge pillar={item.classification.safetyPillar} />
              <span>{item.type === "FLIGHT" ? "Flight" : "Ground"}</span>
              <span>{FUEL_LABELS[item.classification.fuelState]}</span>
              <span>{HARDWARE_LABELS[item.classification.hardwareFamily]}</span>
            </div>
            <div className="mt-2 flex flex-wrap items-center justify-between gap-2 text-xs">
              <TestData data={item.experimentalData} />
              <DoiLink item={item} />
            </div>
          </li>
        ))}
      </ul>
      <CatalogDesktopTable items={items} />
    </>
  );
}

function DoiLink({ item }: { item: InvestigationSummary }) {
  return (
    <a
      href={item.doiUrl}
      target="_blank"
      rel="noopener noreferrer"
      className="font-mono text-xs text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
    >
      {item.doi}
    </a>
  );
}

function CatalogDesktopTable({ items }: { items: InvestigationSummary[] }) {
  return (
    <div className="relative hidden overflow-x-auto rounded-md border border-zinc-200 md:block dark:border-zinc-800">
      <table className="w-full border-collapse text-left text-sm">
        <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-600 dark:bg-zinc-900 dark:text-zinc-400">
          <tr>
            <th scope="col" className="px-3 py-2">Investigation</th>
            <th scope="col" className="px-3 py-2">Type</th>
            <th scope="col" className="px-3 py-2">Hardware</th>
            <th scope="col" className="px-3 py-2">Fuel</th>
            <th scope="col" className="px-3 py-2">Pillar</th>
            <th scope="col" className="px-3 py-2">Test data</th>
            <th scope="col" className="px-3 py-2">DOI</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
          {items.map((item) => (
            <tr key={item.psiId} className="align-top hover:bg-zinc-50 dark:hover:bg-zinc-900/60">
              <td className="px-3 py-3">
                <div className="font-medium text-zinc-900 dark:text-zinc-100">
                  <Link href={`/investigations/${item.psiId}`} className="underline-offset-2 hover:underline">{item.acronym ?? item.psiId}</Link>
                  {item.acronym && (
                    <span className="ml-2 font-mono text-xs font-normal text-zinc-500">{item.psiId}</span>
                  )}
                </div>
                <div className="mt-0.5 max-w-md text-zinc-600 dark:text-zinc-400">{item.title}</div>
              </td>
              <td className="px-3 py-3 whitespace-nowrap">
                {item.type === "FLIGHT" ? "Flight" : "Ground"}
              </td>
              <td className="px-3 py-3">{HARDWARE_LABELS[item.classification.hardwareFamily]}</td>
              <td className="px-3 py-3 whitespace-nowrap">{FUEL_LABELS[item.classification.fuelState]}</td>
              <td className="px-3 py-3 whitespace-nowrap">
                <PillarBadge pillar={item.classification.safetyPillar} />
              </td>
              <td className="px-3 py-3 whitespace-nowrap">
                <TestData data={item.experimentalData} />
              </td>
              <td className="px-3 py-3 whitespace-nowrap">
                <DoiLink item={item} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

const PILLAR_STYLES: Record<InvestigationSummary["classification"]["safetyPillar"], string> = {
  PREVENTION: "bg-emerald-100 text-emerald-900 dark:bg-emerald-900/40 dark:text-emerald-200",
  DETECTION: "bg-sky-100 text-sky-900 dark:bg-sky-900/40 dark:text-sky-200",
  SUPPRESSION: "bg-rose-100 text-rose-900 dark:bg-rose-900/40 dark:text-rose-200",
  FUNDAMENTAL: "bg-zinc-100 text-zinc-800 dark:bg-zinc-800 dark:text-zinc-200",
};

function PillarBadge({ pillar }: { pillar: InvestigationSummary["classification"]["safetyPillar"] }) {
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${PILLAR_STYLES[pillar]}`}>
      {PILLAR_LABELS[pillar]}
    </span>
  );
}

function TestData({ data }: { data: InvestigationSummary["experimentalData"] }) {
  if (!data.available) {
    return <span className="text-zinc-500">No CSV</span>;
  }
  return (
    <span title={`${data.normalizedTestRows} rows with normalized O₂, pressure or flow`}>
      <span className="font-medium text-zinc-900 dark:text-zinc-100">{data.testRows}</span>
      <span className="text-zinc-500"> rows · {data.normalizedTestRows} with conditions</span>
    </span>
  );
}
