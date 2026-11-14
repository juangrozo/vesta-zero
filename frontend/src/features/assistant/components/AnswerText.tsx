import Link from "next/link";
import { Fragment } from "react";

const CITATION = /\[(PSI-\d{1,5})\]/g;

/** Renders answer paragraphs, turning inline [PSI-n] markers into links to the investigation page. */
export function AnswerText({ text, validIds }: { text: string; validIds: Set<string> }) {
  const paragraphs = text.split(/\n{2,}/).filter((p) => p.trim().length > 0);
  return (
    <div className="flex flex-col gap-3 text-[15px] leading-relaxed text-zinc-800 dark:text-zinc-200">
      {paragraphs.map((paragraph, i) => (
        <p key={i}>{withCitations(paragraph, validIds)}</p>
      ))}
    </div>
  );
}

function withCitations(paragraph: string, validIds: Set<string>) {
  const parts: React.ReactNode[] = [];
  let last = 0;
  for (const match of paragraph.matchAll(CITATION)) {
    const id = match[1];
    const index = match.index ?? 0;
    parts.push(paragraph.slice(last, index));
    parts.push(
      validIds.has(id) ? (
        <Link
          key={`${id}-${index}`}
          href={`/investigations/${id}`}
          className="mx-0.5 whitespace-nowrap rounded bg-sky-100 px-1 py-px font-mono text-xs text-sky-800 no-underline hover:bg-sky-200 dark:bg-sky-900/50 dark:text-sky-200 dark:hover:bg-sky-900"
        >
          {id}
        </Link>
      ) : (
        <span
          key={`${id}-${index}`}
          title="This citation is not in the Vesta Zero catalog and was removed from the sources"
          className="mx-0.5 whitespace-nowrap rounded bg-rose-100 px-1 py-px font-mono text-xs text-rose-800 line-through dark:bg-rose-900/40 dark:text-rose-200"
        >
          {id}
        </span>
      ),
    );
    last = index + match[0].length;
  }
  parts.push(paragraph.slice(last));
  return parts.map((part, i) => <Fragment key={i}>{part}</Fragment>);
}
