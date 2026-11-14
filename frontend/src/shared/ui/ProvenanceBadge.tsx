export type Provenance = "DOCUMENTED" | "CURATED" | "AI_EXTRACTED";

const STYLES: Record<Provenance, { label: string; title: string; className: string }> = {
  DOCUMENTED: {
    label: "Documented",
    title: "Taken from a NASA PSI record or experimental table",
    className: "border-emerald-300 text-emerald-800 dark:border-emerald-700 dark:text-emerald-300",
  },
  CURATED: {
    label: "Curated",
    title: "Classified by Vesta Zero from PSI metadata and the challenge resources",
    className: "border-zinc-300 text-zinc-700 dark:border-zinc-600 dark:text-zinc-300",
  },
  AI_EXTRACTED: {
    label: "AI-extracted",
    title: "Interpreted from PSI text by an AI model; check the source span",
    className: "border-violet-300 text-violet-800 dark:border-violet-700 dark:text-violet-300",
  },
};

/** Every value shown in the dashboard carries one of these badges (spec F2). */
export function ProvenanceBadge({ kind }: { kind: Provenance }) {
  const style = STYLES[kind];
  return (
    <span
      title={style.title}
      className={`inline-flex items-center rounded border px-1.5 py-px text-[11px] font-medium uppercase tracking-wide ${style.className}`}
    >
      {style.label}
    </span>
  );
}
