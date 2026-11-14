/** Footer on every page: data scope, data source and how to cite it. */
export function SiteFooter() {
  return (
    <footer className="mt-auto border-t border-zinc-200 dark:border-zinc-800">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-2 px-4 py-6 text-xs text-zinc-600 dark:text-zinc-400">
        <p>
          <strong className="font-semibold text-zinc-800 dark:text-zinc-200">Scope.</strong> Fire safety for habitable
          crew environments (spacecraft, stations, lunar and Martian bases, pressurized rovers). Rockets and propellants
          are outside the PSI data: combustion inside rocket engines is effectively independent of gravity.
        </p>
        <p>
          Data: NASA Physical Sciences Informatics (PSI), cited per investigation by DOI. Vesta Zero · NASA Space Apps
          Challenge 2026.
        </p>
      </div>
    </footer>
  );
}
