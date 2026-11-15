"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Suspense, type ReactNode } from "react";
import { Logo } from "./Logo";
import { AssistantIcon, CatalogIcon, CompareIcon, DataIcon, RankingIcon } from "./NavIcons";

const LINKS: { href: string; label: string; icon: ReactNode; match: (p: string) => boolean }[] = [
  {
    href: "/",
    label: "Catalog",
    icon: <CatalogIcon />,
    match: (p) => p === "/" || p.startsWith("/investigations"),
  },
  { href: "/ranking", label: "Ranking", icon: <RankingIcon />, match: (p) => p.startsWith("/ranking") },
  { href: "/compare", label: "Compare", icon: <CompareIcon />, match: (p) => p.startsWith("/compare") },
  { href: "/data", label: "Data", icon: <DataIcon />, match: (p) => p.startsWith("/data") },
  { href: "/assistant", label: "Assistant", icon: <AssistantIcon />, match: (p) => p.startsWith("/assistant") },
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
        <Link href="/" aria-label="Vesta Zero home" className="text-zinc-900 dark:text-zinc-100">
          <Logo />
        </Link>
        <ul className="flex flex-wrap items-center gap-x-1 gap-y-1 text-sm">
          {LINKS.map((l) => {
            const active = pathname !== null && l.match(pathname);
            return (
              <li key={l.href}>
                <Link
                  href={l.href}
                  aria-current={active ? "page" : undefined}
                  className={`flex items-center gap-1.5 rounded-md px-2 py-1 transition-colors ${
                    active
                      ? "bg-sky-50 font-medium text-sky-800 dark:bg-sky-950/60 dark:text-sky-200"
                      : "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-800 dark:hover:text-zinc-100"
                  }`}
                >
                  {l.icon}
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
