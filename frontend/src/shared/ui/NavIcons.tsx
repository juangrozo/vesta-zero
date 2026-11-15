/** Small 16 px line icons for the main navigation. Decorative: every icon sits next to its text label. */
const common = {
  viewBox: "0 0 16 16",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.5,
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
  className: "h-4 w-4 shrink-0",
  "aria-hidden": true,
  focusable: false,
};

export function CatalogIcon() {
  return (
    <svg {...common}>
      <path d="M2.5 4h1M6 4h7.5M2.5 8h1M6 8h7.5M2.5 12h1M6 12h7.5" />
    </svg>
  );
}

export function RankingIcon() {
  return (
    <svg {...common}>
      <path d="M3 13.5v-4M8 13.5v-8M13 13.5V2.5" strokeWidth={2} />
    </svg>
  );
}

export function CompareIcon() {
  return (
    <svg {...common}>
      <rect x="1.75" y="2.5" width="5" height="11" rx="1" />
      <rect x="9.25" y="2.5" width="5" height="11" rx="1" />
    </svg>
  );
}

export function DataIcon() {
  return (
    <svg {...common}>
      <ellipse cx="8" cy="3.75" rx="5" ry="1.75" />
      <path d="M3 3.75v8.5c0 .97 2.24 1.75 5 1.75s5-.78 5-1.75v-8.5M3 8c0 .97 2.24 1.75 5 1.75S13 8.97 13 8" />
    </svg>
  );
}

export function AssistantIcon() {
  return (
    <svg {...common}>
      <path d="M7 1.75l1.3 3.45 3.45 1.3-3.45 1.3L7 11.25 5.7 7.8 2.25 6.5 5.7 5.2z" />
      <path d="M12.25 10.25l.55 1.2 1.2.55-1.2.55-.55 1.2-.55-1.2-1.2-.55 1.2-.55z" />
    </svg>
  );
}
