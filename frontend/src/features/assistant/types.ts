export interface Citation {
  psiId: string;
  acronym: string | null;
  title: string;
  doi: string;
  doiUrl: string;
}

export interface AssistantAnswer {
  answer: string;
  provenance: "AI_EXTRACTED";
  citations: Citation[];
  droppedCitations: string[];
  unverifiedNumbers: string[];
  insufficientData: boolean;
  declined: boolean;
  partialGravityNotice: string | null;
  model: string;
  cached: boolean;
}
