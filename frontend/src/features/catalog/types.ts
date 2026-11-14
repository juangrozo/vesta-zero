export type InvestigationType = "FLIGHT" | "GROUND";
export type HardwareFamily = "CIR_ACME" | "CIR_MDCA" | "MSG" | "CYGNUS" | "OTHER_FLIGHT" | "GROUND";
export type FuelState = "GAS" | "LIQUID_DROPLET" | "SOLID" | "AEROSOL";
export type SafetyPillar = "PREVENTION" | "DETECTION" | "SUPPRESSION" | "FUNDAMENTAL";

export interface InvestigationSummary {
  psiId: string;
  acronym: string | null;
  title: string;
  doi: string;
  doiUrl: string;
  type: InvestigationType;
  projectType: string | null;
  subArea: string | null;
  hardware: string | null;
  classification: {
    hardwareFamily: HardwareFamily;
    fuelState: FuelState;
    safetyPillar: SafetyPillar;
    provenance: "CURATED";
  };
  experimentalData: {
    available: boolean;
    tables: number;
    testRows: number;
    normalizedTestRows: number;
  };
}

export interface CatalogFilter {
  type?: InvestigationType;
  hardware?: HardwareFamily;
  fuelState?: FuelState;
  pillar?: SafetyPillar;
}
