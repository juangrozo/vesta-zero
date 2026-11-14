/** What the PSI data covers and what it does not (challenge resources): rockets and propellants are out of scope. */
export function ScopeNotice() {
  return (
    <aside
      role="note"
      className="rounded-md border border-zinc-300 px-4 py-3 text-sm text-zinc-700 dark:border-zinc-700 dark:text-zinc-300"
    >
      <strong className="font-semibold">Scope.</strong> PSI covers fire safety research for habitable crew environments:
      spacecraft, stations, lunar and Martian bases, pressurized rovers. Rockets and propellants are outside it: the
      high velocities inside rocket engines make their combustion effectively independent of gravity.
    </aside>
  );
}
