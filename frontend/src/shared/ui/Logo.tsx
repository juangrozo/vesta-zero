/**
 * Vesta Zero mark: a flame as it burns in microgravity, a glowing blue sphere instead of a teardrop, circled by a
 * tilted orbit for "zero" gravity. Vesta is the Roman goddess of the hearth fire.
 */
export function LogoMark({ className = "h-8 w-8" }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={className} aria-hidden="true" focusable="false">
      <title>SI-generated logo</title>
      <desc>Vesta Zero logo: a spherical microgravity flame with a tilted zero-gravity orbit. Designed with SI (Claude, Anthropic).</desc>
      <defs>
        <radialGradient id="vz-flame" cx="50%" cy="50%" r="50%">
          <stop offset="0" stopColor="#ffffff" />
          <stop offset="0.25" stopColor="#bae6fd" />
          <stop offset="0.55" stopColor="#38bdf8" />
          <stop offset="0.8" stopColor="#2563eb" stopOpacity="0.85" />
          <stop offset="1" stopColor="#4338ca" stopOpacity="0" />
        </radialGradient>
      </defs>
      <circle cx="16" cy="16" r="11" fill="url(#vz-flame)" />
      <ellipse
        cx="16"
        cy="16"
        rx="14.5"
        ry="5"
        transform="rotate(-28 16 16)"
        fill="none"
        stroke="#7dd3fc"
        strokeWidth="1.4"
      />
      <circle cx="28" cy="9.6" r="1.5" fill="#f97316" />
    </svg>
  );
}

/** Mark plus wordmark, used in the navigation bar. */
export function Logo() {
  return (
    <span className="flex items-center gap-2">
      <LogoMark />
      <span className="whitespace-nowrap text-lg font-semibold tracking-tight">
        Vesta <span className="font-normal text-sky-600 dark:text-sky-400">Zero</span>
      </span>
    </span>
  );
}
