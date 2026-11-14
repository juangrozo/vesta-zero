/** Formatting for normalized quantities. Backend units: O2 mole fraction, pressure in atm, flow in cm/s. */
export interface Range {
  min: number;
  max: number;
}

const PSI_PER_ATM = 14.6959;

function span(range: Range, format: (value: number) => string): string {
  return range.min === range.max ? format(range.min) : `${format(range.min)}–${format(range.max)}`;
}

function trim(value: number, digits: number): string {
  return Number(value.toFixed(digits)).toString();
}

export function formatO2(range: Range | null): string {
  return range ? `${span(range, (v) => trim(v * 100, 1))} % O₂` : "Not documented";
}

export function formatPressure(range: Range | null): string {
  if (!range) return "Not documented";
  return `${span(range, (v) => trim(v, 3))} atm (${span(range, (v) => trim(v * PSI_PER_ATM, 1))} psi)`;
}

export function formatFlow(range: Range | null): string {
  return range ? `${span(range, (v) => trim(v, 2))} cm/s` : "Not documented";
}

export function formatDate(iso: string | null): string | null {
  if (!iso) return null;
  return new Date(`${iso}T00:00:00Z`).toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    timeZone: "UTC",
  });
}
