import { ProvenanceBadge } from "@/shared/ui/ProvenanceBadge";
import type { AssistantAnswer } from "../types";
import { AnswerText } from "./AnswerText";

/** Grounded AI answer with its warnings and sources; shared by the assistant (F5) and the comparator (F4). */
export function AnswerCard({ question, answer }: { question: string; answer: AssistantAnswer }) {
  const validIds = new Set(answer.citations.map((c) => c.psiId));
  return (
    <article className="flex flex-col gap-4 rounded-md border border-zinc-200 p-4 dark:border-zinc-800">
      <header className="flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm font-medium text-zinc-900 dark:text-zinc-100">{question}</p>
        <span className="flex items-center gap-2 text-xs text-zinc-500">
          <ProvenanceBadge kind="AI_EXTRACTED" />
          {answer.model}
          {answer.cached && <span title="Served from the answer cache, no new API call">· cached</span>}
        </span>
      </header>

      {answer.partialGravityNotice && (
        <p className="rounded-md border border-amber-400 bg-amber-50 px-3 py-2 text-sm text-amber-900 dark:border-amber-500/60 dark:bg-amber-950/40 dark:text-amber-200">
          <strong className="font-semibold">Partial-gravity data gap.</strong> {answer.partialGravityNotice}
        </p>
      )}

      {answer.declined ? (
        <p className="text-sm text-zinc-700 dark:text-zinc-300">{answer.answer}</p>
      ) : (
        <AnswerText text={answer.answer} validIds={validIds} />
      )}

      {answer.insufficientData && (
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          The records do not fully answer this question; the answer says what is not documented.
        </p>
      )}
      {answer.unverifiedNumbers.length > 0 && (
        <p className="rounded-md border border-rose-300 px-3 py-2 text-sm text-rose-800 dark:border-rose-800 dark:text-rose-200">
          These numbers do not appear in the source records; check them before using them:{" "}
          <span className="font-mono">{answer.unverifiedNumbers.join(", ")}</span>
        </p>
      )}
      {answer.droppedCitations.length > 0 && (
        <p className="text-xs text-rose-700 dark:text-rose-300">
          Removed citations not in the catalog: {answer.droppedCitations.join(", ")}
        </p>
      )}

      {answer.citations.length > 0 && (
        <section aria-label="Sources" className="border-t border-zinc-200 pt-3 dark:border-zinc-800">
          <h3 className="mb-2 flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-zinc-500">
            Sources <ProvenanceBadge kind="DOCUMENTED" />
          </h3>
          <ul className="flex flex-col gap-1.5 text-sm">
            {answer.citations.map((c) => (
              <li key={c.psiId} className="flex flex-wrap items-baseline gap-x-2">
                <a
                  href={`/investigations/${c.psiId}`}
                  className="font-medium text-zinc-900 underline-offset-2 hover:underline dark:text-zinc-100"
                >
                  {c.acronym ?? c.psiId}
                </a>
                <span className="font-mono text-xs text-zinc-500">{c.psiId}</span>
                <span className="min-w-0 flex-1 truncate text-zinc-600 dark:text-zinc-400">{c.title}</span>
                <a
                  href={c.doiUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="font-mono text-xs text-sky-700 underline underline-offset-2 hover:text-sky-900 dark:text-sky-400 dark:hover:text-sky-300"
                >
                  {c.doi}
                </a>
              </li>
            ))}
          </ul>
        </section>
      )}
    </article>
  );
}
