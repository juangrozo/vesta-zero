"use client";

import { Fragment, useState } from "react";
import { formatFlow, formatO2, formatPressure } from "@/shared/lib/format";
import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import { useTestConditions } from "../hooks/useInvestigation";
import type { TestConditionRow } from "../types";

const PAGE_SIZE = 50;

/** Experimental-table rows with normalized values and the original row on demand (spec F2). */
export function TestConditionsTable({ psiId }: { psiId: string }) {
  const state = useTestConditions(psiId);
  const [visible, setVisible] = useState(PAGE_SIZE);
  const [expanded, setExpanded] = useState<string | null>(null);

  if (state.status === "loading") {
    return <p className="text-sm text-zinc-600 dark:text-zinc-400">Loading test conditions…</p>;
  }
  if (state.status === "error") {
    return (
      <p role="alert" className="text-sm text-rose-700 dark:text-rose-300">
        Could not load test conditions ({state.message})
      </p>
    );
  }
  const rows = state.data;
  if (rows.length === 0) {
    return (
      <p className="rounded-md border border-dashed border-zinc-300 px-4 py-6 text-center text-sm text-zinc-600 dark:border-zinc-700 dark:text-zinc-400">
        No experimental table is documented in PSI for this investigation.
      </p>
    );
  }
  const multipleFiles = new Set(rows.map((r) => r.sourceFile)).size > 1;

  return (
    <div className="flex flex-col gap-3">
      <div className="relative overflow-x-auto rounded-md border border-zinc-200 dark:border-zinc-800">
        <table className="w-full border-collapse text-left text-sm">
          <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-600 dark:bg-zinc-900 dark:text-zinc-400">
            <tr>
              <th scope="col" className="px-3 py-2">#</th>
              {multipleFiles && <th scope="col" className="px-3 py-2">Table</th>}
              <th scope="col" className="px-3 py-2">Test</th>
              <th scope="col" className="px-3 py-2">Fuel / material</th>
              <th scope="col" className="px-3 py-2">O₂</th>
              <th scope="col" className="px-3 py-2">Pressure</th>
              <th scope="col" className="px-3 py-2">Flow</th>
              <th scope="col" className="px-3 py-2"><span className="sr-only">Original row</span></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
            {rows.slice(0, visible).map((row) => {
              const id = `${row.sourceFile}#${row.rowIndex}`;
              const open = expanded === id;
              return (
                <Fragment key={id}>
                  <tr className="align-top">
                    <td className="px-3 py-2 font-mono text-xs text-zinc-500">{row.rowIndex}</td>
                    {multipleFiles && <td className="px-3 py-2 text-xs">{tableName(row.sourceFile)}</td>}
                    <td className="px-3 py-2 whitespace-nowrap">{row.testLabel ?? "—"}</td>
                    <td className="px-3 py-2">{row.fuelOrMaterial ?? "—"}</td>
                    <Cell value={row.o2Fraction && formatO2(row.o2Fraction)} />
                    <Cell value={row.pressureAtm && formatPressure(row.pressureAtm)} />
                    <Cell value={row.flowCmPerS && formatFlow(row.flowCmPerS)} />
                    <td className="px-3 py-2 text-right">
                      <button
                        type="button"
                        aria-expanded={open}
                        onClick={() => setExpanded(open ? null : id)}
                        className="text-xs text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
                      >
                        {open ? "Hide row" : "Original row"}
                      </button>
                    </td>
                  </tr>
                  {open && (
                    <tr>
                      <td colSpan={multipleFiles ? 8 : 7} className="bg-zinc-50 px-3 py-3 dark:bg-zinc-900">
                        <RawRow row={row} />
                      </td>
                    </tr>
                  )}
                </Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-zinc-500">
        <span className="flex items-center gap-2">
          <ProvenanceBadge kind="DOCUMENTED" />
          Values converted to O₂ %, atm and cm/s from the units documented in each table.
        </span>
        <span>
          Showing {Math.min(visible, rows.length)} of {rows.length}
          {visible < rows.length && (
            <button
              type="button"
              onClick={() => setVisible((v) => v + PAGE_SIZE)}
              className="ml-3 rounded-md border border-zinc-300 px-2 py-1 font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-800"
            >
              Show more
            </button>
          )}
        </span>
      </div>
    </div>
  );
}

function Cell({ value }: { value: string | null }) {
  return (
    <td className="px-3 py-2 whitespace-nowrap">
      {value ?? <span className="text-zinc-400">—</span>}
    </td>
  );
}

function RawRow({ row }: { row: TestConditionRow }) {
  return (
    <div>
      <p className="mb-2 text-xs text-zinc-500">
        {row.sourceFile}, row {row.rowIndex}
      </p>
      <dl className="grid grid-cols-1 gap-x-6 gap-y-1 text-xs sm:grid-cols-2 lg:grid-cols-3">
        {Object.entries(row.raw).map(([column, value]) => (
          <div key={column} className="flex gap-2">
            <dt className="shrink-0 text-zinc-500">{column}:</dt>
            <dd className="break-words font-mono text-zinc-800 dark:text-zinc-200">{value || "—"}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}

function tableName(file: string): string {
  const parts = file.replace(/\.csv$/i, "").split("_");
  return parts[parts.length - 1] || file;
}
