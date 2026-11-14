import { AssistantView } from "@/features/assistant/components/AssistantView";
import { ScopeNotice } from "@/shared/ui/ScopeNotice";

export const metadata = {
  title: "Assistant · Vesta Zero",
};

export default function AssistantPage() {
  return (
    <main className="mx-auto flex w-full max-w-4xl flex-1 flex-col gap-6 px-4 py-10">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl font-semibold tracking-tight">Research assistant</h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          Claude answers from the stored NASA PSI records only, cites every investigation it uses, and says
          when something is not documented. Numbers are checked against the records.
        </p>
      </header>
      <ScopeNotice />
      <AssistantView />
    </main>
  );
}
