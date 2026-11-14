import { RankingView } from "@/features/ranking/components/RankingView";
import { PartialGravityBanner } from "@/shared/ui/PartialGravityBanner";

export const metadata = {
  title: "Ranking · Vesta Zero",
};

export default function RankingPage() {
  return (
    <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-6 px-4 py-10">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl font-semibold tracking-tight">Relevance to crew environments</h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          Pick a spacecraft, station, lunar or Martian base, or the spacesuit as a worst case. The ranking is
          deterministic and explainable: every score is a weighted sum of five criteria, and every criterion states
          why it scored what it did. Adjust the weights to explore.
        </p>
      </header>
      <PartialGravityBanner />
      <RankingView />
    </main>
  );
}
