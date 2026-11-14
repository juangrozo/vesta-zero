"use client";

import { useRef, useState } from "react";
import { askAssistant } from "../api";
import type { AssistantAnswer } from "../types";
import { AnswerCard } from "./AnswerCard";

const SUGGESTIONS = [
  "Which investigations tested oxygen levels close to a lunar habitat (at least 30 % O2 at low pressure)?",
  "What materials were burned in SAFFIRE and how large were the samples?",
  "Compare FLEX and FLEX-2: what changed between them?",
  "Which experiments help design smoke detectors for spacecraft?",
  "Will a cotton fabric burn the same way on the Moon as on the ISS?",
];

type State =
  | { status: "idle" }
  | { status: "loading"; question: string }
  | { status: "error"; question: string; message: string }
  | { status: "ready"; question: string; answer: AssistantAnswer };

/** Grounded Q&A over the stored investigations (spec F5). */
export function AssistantView() {
  const [question, setQuestion] = useState("");
  const [state, setState] = useState<State>({ status: "idle" });
  const controller = useRef<AbortController | null>(null);

  async function submit(text: string) {
    const q = text.trim();
    if (q.length < 5) return;
    controller.current?.abort();
    const current = new AbortController();
    controller.current = current;
    setQuestion(q);
    setState({ status: "loading", question: q });
    try {
      const answer = await askAssistant(q, current.signal);
      setState({ status: "ready", question: q, answer });
    } catch (error) {
      if (current.signal.aborted) return;
      setState({
        status: "error",
        question: q,
        message: error instanceof Error ? error.message : "The assistant could not answer",
      });
    }
  }

  const busy = state.status === "loading";

  return (
    <div className="flex flex-col gap-6">
      <form
        className="flex flex-col gap-3"
        onSubmit={(e) => {
          e.preventDefault();
          void submit(question);
        }}
      >
        <label htmlFor="question" className="text-sm font-medium">
          Ask about the 24 PSI fire investigations
        </label>
        <textarea
          id="question"
          rows={3}
          maxLength={1000}
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && (e.metaKey || e.ctrlKey)) void submit(question);
          }}
          placeholder="e.g. Which experiments burned solid materials in concurrent flow?"
          className="w-full resize-y rounded-md border border-zinc-300 bg-white px-3 py-2 text-zinc-900 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/30 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
        />
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-xs text-zinc-500">
            Answers use only the stored PSI records and cite each investigation. Ctrl + Enter to send.
          </p>
          <button
            type="submit"
            disabled={busy || question.trim().length < 5}
            className="rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white hover:bg-sky-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busy ? "Reading the records…" : "Ask"}
          </button>
        </div>
      </form>

      <section aria-label="Suggested questions" className="flex flex-col gap-2">
        <h2 className="text-xs font-medium uppercase tracking-wide text-zinc-500">Try a demo question</h2>
        <ul className="flex flex-wrap gap-2">
          {SUGGESTIONS.map((s) => (
            <li key={s}>
              <button
                type="button"
                disabled={busy}
                onClick={() => void submit(s)}
                className="rounded-full border border-zinc-300 px-3 py-1 text-left text-xs text-zinc-700 hover:bg-zinc-50 disabled:opacity-50 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-900"
              >
                {s}
              </button>
            </li>
          ))}
        </ul>
      </section>

      <div aria-live="polite">
        {state.status === "loading" && (
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
            Claude is reading the 24 investigation records… (usually 10–20 s; repeated questions are instant)
          </p>
        )}
        {state.status === "error" && (
          <p
            role="alert"
            className="rounded-md border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-900 dark:border-rose-800 dark:bg-rose-950/40 dark:text-rose-200"
          >
            {state.message}
          </p>
        )}
        {state.status === "ready" && <AnswerCard question={state.question} answer={state.answer} />}
      </div>
    </div>
  );
}
