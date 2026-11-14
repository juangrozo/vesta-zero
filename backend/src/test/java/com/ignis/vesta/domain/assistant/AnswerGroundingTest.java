package com.ignis.vesta.domain.assistant;

import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerGroundingTest {

    private static final EvidenceCorpus CORPUS = new EvidenceCorpus("""
            <investigation id="PSI-159" doi="10.60555/cyt1-kh64">
            Acronym: ACME CFI-G
            Documented O2 range: 12 to 85 % O2
            Documented pressure range: 0.489 to 3.267 atm (7.2 to 48 psi)
            </investigation>
            <investigation id="PSI-69" doi="10.60555/mbq8-0451">
            Acronym: FLEX
            Documented O2 range: 12 to 34 % O2
            </investigation>
            """, Set.of(PsiId.of("PSI-159"), PsiId.of("PSI-69")), "fp");

    @Test
    void keepsCitationsInCorpusAndDropsUnknownOnes() {
        GeneratedAnswer generated = answer("CFI-G reached 85 % O2 [PSI-159]; FLEX reached 34 % O2 [PSI-69]. "
                + "See also [PSI-999].", List.of("PSI-159", "PSI-42"));

        AssistantAnswer grounded = AnswerGrounding.ground(generated, CORPUS, "Highest oxygen tested?");

        assertThat(grounded.citations()).extracting(PsiId::value).containsExactly("PSI-69", "PSI-159");
        assertThat(grounded.droppedCitations()).containsExactlyInAnyOrder("PSI-42", "PSI-999");
    }

    @Test
    void numbersFromTheRecordsAreVerifiedIncludingFractionForms() {
        GeneratedAnswer generated = answer("Up to 85 % O2 (a mole fraction of 0.85) at 0.489 atm, about 7.2 psi, "
                + "in FLEX-2 and CO2 diluted tests [PSI-159].", List.of("PSI-159"));

        assertThat(AnswerGrounding.ground(generated, CORPUS, "q").unverifiedNumbers()).isEmpty();
    }

    @Test
    void inventedNumbersAreFlagged() {
        GeneratedAnswer generated = answer("Flames spread at 42.5 mm/s at 0.3 atm in 3 tests [PSI-69].",
                List.of("PSI-69"));

        assertThat(AnswerGrounding.ground(generated, CORPUS, "q").unverifiedNumbers())
                .containsExactly("42.5", "0.3");
    }

    @Test
    void lunarQuestionsCarryThePartialGravityNotice() {
        assertThat(AnswerGrounding.ground(answer("x", List.of()), CORPUS,
                "Will cotton burn the same way on the Moon?").partialGravityNotice()).isTrue();
        assertThat(AnswerGrounding.ground(answer("x", List.of()), CORPUS,
                "Which experiments studied droplets?").partialGravityNotice()).isFalse();
    }

    @Test
    void partialGravityDetectionCoversSpanishAndVariants() {
        assertThat(PartialGravityTopic.mentionedIn("¿Qué pasa en una base lunar?")).isTrue();
        assertThat(PartialGravityTopic.mentionedIn("fire safety on Mars rovers")).isTrue();
        assertThat(PartialGravityTopic.mentionedIn("partial-gravity flammability")).isTrue();
        assertThat(PartialGravityTopic.mentionedIn("smoke detectors on the ISS")).isFalse();
    }

    private static GeneratedAnswer answer(String text, List<String> cited) {
        return new GeneratedAnswer(text, cited, false, false, "claude-opus-5-5", false);
    }
}
