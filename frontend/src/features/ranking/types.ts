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

export type TargetBound = "AT_LEAST" | "AT_MOST" | "AROUND";

export interface HabitatEnvironment {
  id: string;
  name: string;
  o2Fraction: number;
  o2Bound: TargetBound;
  pressurePsi: number;
  pressureAtm: number;
  pressureBound: TargetBound;
  basis: string;
  assumption: boolean;
}

export interface RankingResponse {
  weights: Record<string, number>;
  environment: HabitatEnvironment;
  partialGravityNotice: string;
  results: RankedInvestigation[];
}
