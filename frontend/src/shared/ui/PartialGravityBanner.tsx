/** Persistent notice required by the challenge: PSI has no partial-gravity data. */
export function PartialGravityBanner() {
  return (
    <aside
      role="note"
      className="rounded-md border border-amber-400 bg-amber-50 px-4 py-3 text-sm text-amber-900 dark:border-amber-500/60 dark:bg-amber-950/40 dark:text-amber-200"
    >
      <strong className="font-semibold">Data gap: partial gravity.</strong> PSI has no
      partial-gravity data yet (expected from LUCI and FM2). E-FIELD only mimics partial
      gravity with electric fields. Microgravity and 1-g results bound the intermediate
      range.
    </aside>
  );
}
