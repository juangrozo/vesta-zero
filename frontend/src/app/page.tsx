import { CatalogView } from "@/features/catalog/components/CatalogView";
import { PartialGravityBanner } from "@/shared/ui/PartialGravityBanner";

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-6 px-4 py-10">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl font-semibold tracking-tight">Vesta Zero</h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          Fire safety insights for crewed habitats, built from NASA PSI microgravity
          combustion investigations.
        </p>
      </header>
      <PartialGravityBanner />
      <CatalogView />
    </main>
  );
}
