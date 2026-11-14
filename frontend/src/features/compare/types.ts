import type { InvestigationSummary } from "@/features/catalog/types";
import type { InvestigationDetail } from "@/features/investigation/types";

export interface CompareItem {
  psiId: string;
  acronym: string | null;
  title: string;
  doi: string;
  doiUrl: string;
  type: InvestigationSummary["type"];
  subArea: string | null;
  hardware: string | null;
  flightProgram: string | null;
  objective: string | null;
  startDate: string | null;
  endDate: string | null;
  classification: InvestigationSummary["classification"];
  conditions: InvestigationDetail["conditions"];
}

export interface ComparisonFact {
  dimension: string;
  same: boolean;
  detail: string;
}

export interface Comparison {
  investigations: CompareItem[];
  facts: ComparisonFact[];
}
