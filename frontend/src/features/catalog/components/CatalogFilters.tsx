"use client";

import { FUEL_LABELS, HARDWARE_LABELS, PILLAR_LABELS, TYPE_LABELS } from "../labels";
import type { CatalogFilter } from "../types";

interface Props {
  filter: CatalogFilter;
  onChange: (filter: CatalogFilter) => void;
}

export function CatalogFilters({ filter, onChange }: Props) {
  const active = Object.values(filter).some(Boolean);
  return (
    <form
      className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4"
      onSubmit={(e) => e.preventDefault()}
      aria-label="Filter investigations"
    >
      <Select
        label="Type"
        value={filter.type}
        options={TYPE_LABELS}
        onChange={(type) => onChange({ ...filter, type })}
      />
      <Select
        label="Hardware"
        value={filter.hardware}
        options={HARDWARE_LABELS}
        onChange={(hardware) => onChange({ ...filter, hardware })}
      />
      <Select
        label="Fuel state"
        value={filter.fuelState}
        options={FUEL_LABELS}
        onChange={(fuelState) => onChange({ ...filter, fuelState })}
      />
      <Select
        label="Safety pillar"
        value={filter.pillar}
        options={PILLAR_LABELS}
        onChange={(pillar) => onChange({ ...filter, pillar })}
      />
      {active && (
        <button
          type="button"
          onClick={() => onChange({})}
          className="justify-self-start text-sm text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
        >
          Clear filters
        </button>
      )}
    </form>
  );
}

function Select<K extends string>({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: K | undefined;
  options: Record<K, string>;
  onChange: (value: K | undefined) => void;
}) {
  return (
    <label className="flex flex-col gap-1 text-sm">
      <span className="font-medium text-zinc-700 dark:text-zinc-300">{label}</span>
      <select
        value={value ?? ""}
        onChange={(e) => onChange((e.target.value || undefined) as K | undefined)}
        className="rounded-md border border-zinc-300 bg-white px-3 py-2 text-zinc-900 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/30 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
      >
        <option value="">All</option>
        {(Object.entries(options) as [K, string][]).map(([key, text]) => (
          <option key={key} value={key}>
            {text}
          </option>
        ))}
      </select>
    </label>
  );
}
