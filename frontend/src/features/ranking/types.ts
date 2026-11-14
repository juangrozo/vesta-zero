import type { Provenance } from "@/shared/ui/ProvenanceBadge";

export interface Criterion {
  id: string;
  name: string;
  description: string;
  defaultWeight: number;
  provenance: Provenance;
}

export interface Contribution {
  criterionId: string;
  criterionName: string;
  provenance: Provenance;
  weight: number;
  score: number;
  contribution: number;
  reason: string;
}

export interface RankedInvestigation {
  position: number;
  psiId: string;
  acronym: string | null;
  title: string;
  doiUrl: string;
  type: "FLIGHT" | "GROUND";
  score: number;
  breakdown: Contribution[];
}

export interface RankingResponse {
  weights: Record<string, number>;
  lunarTarget: { o2Fraction: number; pressureAtm: number; pressurePsi: number };
  partialGravityNotice: string;
  results: RankedInvestigation[];
}
