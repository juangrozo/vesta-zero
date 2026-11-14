"use client";

import { useState } from "react";

/** "Cite this investigation": PSI asks users to cite each investigation by its DOI. */
export function CitationBox({ citation, doiUrl }: { citation: string; doiUrl: string }) {
  const [copied, setCopied] = useState(false);

  async function copy() {
    try {
      await navigator.clipboard.writeText(citation);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  }

  return (
    <section
      aria-labelledby="cite-heading"
      className="rounded-md border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-800 dark:bg-zinc-900"
    >
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 id="cite-heading" className="text-sm font-semibold">
          Cite this investigation
        </h2>
        <div className="flex items-center gap-3 text-sm">
          <a
            href={doiUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
          >
            Open DOI
          </a>
          <button
            type="button"
            onClick={copy}
            className="rounded-md border border-zinc-300 px-2.5 py-1 font-medium hover:bg-white dark:border-zinc-700 dark:hover:bg-zinc-800"
          >
            {copied ? "Copied" : "Copy"}
          </button>
        </div>
      </div>
      <p className="mt-2 break-words font-mono text-xs text-zinc-700 dark:text-zinc-300">{citation}</p>
    </section>
  );
}
