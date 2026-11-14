/** Stable color per criterion, by display order. Labels always accompany colors (never color alone). */
const PALETTE = [
  { bar: "bg-sky-500", dot: "bg-sky-500" },
  { bar: "bg-emerald-500", dot: "bg-emerald-500" },
  { bar: "bg-amber-500", dot: "bg-amber-500" },
  { bar: "bg-violet-500", dot: "bg-violet-500" },
  { bar: "bg-rose-500", dot: "bg-rose-500" },
];

export function criterionColor(index: number) {
  return PALETTE[index % PALETTE.length];
}
