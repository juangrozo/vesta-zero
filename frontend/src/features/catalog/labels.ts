import type { FuelState, HardwareFamily, InvestigationType, SafetyPillar } from "./types";

export const TYPE_LABELS: Record<InvestigationType, string> = {
  FLIGHT: "Flight (microgravity)",
  GROUND: "Ground study",
};

export const HARDWARE_LABELS: Record<HardwareFamily, string> = {
  CIR_ACME: "CIR · ACME",
  CIR_MDCA: "CIR · MDCA (droplets)",
  MSG: "Microgravity Science Glovebox",
  CYGNUS: "Cygnus (Saffire)",
  OTHER_FLIGHT: "Other flight hardware",
  GROUND: "Ground lab / modeling",
};

export const FUEL_LABELS: Record<FuelState, string> = {
  GAS: "Gas",
  LIQUID_DROPLET: "Liquid droplet",
  SOLID: "Solid",
  AEROSOL: "Aerosol / dust",
};

export const PILLAR_LABELS: Record<SafetyPillar, string> = {
  PREVENTION: "Prevention",
  DETECTION: "Detection",
  SUPPRESSION: "Suppression",
  FUNDAMENTAL: "Fundamental physics",
};
