"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense } from "react";

const LINKS = [
  { href: "/", label: "Catalog", match: (p: string) => p === "/" || p.startsWith("/investigations") },
  { href: "/ranking", label: "Ranking", match: (p: string) => p.startsWith("/ranking") },
  { href: "/compare", label: "Compare", match: (p: string) => p.startsWith("/compare") },
  { href: "/data", label: "Data", match: (p: string) => p.startsWith("/data") },
  { href: "/assistant", label: "Assistant", match: (p: string) => p.startsWith("/assistant") },
];

/**
 * Under Cache Components, usePathname() on a dynamic route is runtime data and must sit inside <Suspense>.
 * The fallback renders the same links without the active state, so the static shell keeps the navigation.
 */
export function SiteNav() {
  return (
    <Suspense fallback={<NavShell pathname={null} />}>
      <ActiveNav />
    </Suspense>
  );
}

function ActiveNav() {
  return <NavShell pathname={usePathname() ?? "/"} />;
}

function NavShell({ pathname }: { pathname: string | null }) {
  return (
    <nav aria-label="Main" className="border-b border-zinc-200 dark:border-zinc-800">
      <div className="mx-auto flex w-full max-w-6xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3">
        <Link href="/" className="whitespace-nowrap font-semibold tracking-tight">
          Vesta Zero
        </Link>
        <ul className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm">
          {LINKS.map((l) => {
            const active = pathname !== null && l.match(pathname);
            return (
              <li key={l.href}>
                <Link
                  href={l.href}
                  aria-current={active ? "page" : undefined}
                  className={
                    active
                      ? "font-medium text-zinc-900 underline underline-offset-4 dark:text-zinc-100"
                      : "text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-100"
                  }
                >
                  {l.label}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}
