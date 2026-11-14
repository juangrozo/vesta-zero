import { Suspense } from "react";
import { CompareView } from "@/features/compare/components/CompareView";

export const metadata = {
  title: "Compare · Vesta Zero",
};

export default function ComparePage() {
  return (
    <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-6 px-4 py-10">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl font-semibold tracking-tight">Compare investigations</h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          Put two or three investigations side by side: documented test conditions, curated classification,
          computed similarities and differences, and an optional AI comparison with citations.
        </p>
      </header>
      {/* useSearchParams() is runtime data under Cache Components: keep it inside Suspense. */}
      <Suspense fallback={<p className="text-sm text-zinc-600 dark:text-zinc-400">Loading…</p>}>
        <CompareView />
      </Suspense>
    </main>
  );
}
