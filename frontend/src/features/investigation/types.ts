import type { InvestigationSummary } from "@/features/catalog/types";
import type { Range } from "@/shared/lib/format";

export interface Publication {
  title: string;
  authors: string;
  doi: string | null;
  doiUrl: string | null;
}

export interface DataQualityWarning {
  sourceFile: string | null;
  rowIndex: number | null;
  column: string | null;
  rawValue: string | null;
  reason: string;
}

export interface InvestigationDetail {
  psiId: string;
  acronym: string | null;
  title: string;
  doi: string;
  doiUrl: string;
  citation: string;
  type: InvestigationSummary["type"];
  projectType: string | null;
  researchArea: string | null;
  subArea: string | null;
  flightProgram: string | null;
  hardware: string | null;
  objective: string | null;
  approach: string | null;
  hypothesis: string | null;
  researchImpacts: string | null;
  startDate: string | null;
  endDate: string | null;
  license: string | null;
  publications: Publication[];
  classification: InvestigationSummary["classification"];
  conditions: {
    testRows: number;
    normalizedRows: number;
    o2Fraction: Range | null;
    pressureAtm: Range | null;
    flowCmPerS: Range | null;
    fuelsOrMaterials: string[];
    sourceFiles: string[];
  };
  dataQualityWarnings: DataQualityWarning[];
}

export interface TestConditionRow {
  sourceFile: string;
  rowIndex: number;
  testLabel: string | null;
  fuelOrMaterial: string | null;
  o2Fraction: Range | null;
  pressureAtm: Range | null;
  flowCmPerS: Range | null;
  raw: Record<string, string>;
}
