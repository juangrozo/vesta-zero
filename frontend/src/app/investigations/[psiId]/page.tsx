import { Suspense } from "react";
import { InvestigationDetailView } from "@/features/investigation/components/InvestigationDetailView";
import { PartialGravityBanner } from "@/shared/ui/PartialGravityBanner";

/**
 * Not async: params are runtime data under Cache Components, so they are awaited inside <Suspense>
 * and the rest of the page stays in the static shell.
 */
export default function InvestigationPage({ params }: PageProps<"/investigations/[psiId]">) {
  return (
    <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-6 px-4 py-10">
      <PartialGravityBanner />
      <Suspense fallback={<p className="text-sm text-zinc-600 dark:text-zinc-400">Loading investigation…</p>}>
        {params.then(({ psiId }) => (
          <InvestigationDetailView psiId={decodeURIComponent(psiId).toUpperCase()} />
        ))}
      </Suspense>
    </main>
  );
}
