package ar.edu.utn.frc.tup.piii.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CodeGeneratorTest {

    @InjectMocks
    private CodeGenerator codeGenerator;

    @Test
    void generateUniqueCode_ShouldReturnCodeWithCorrectLength() {
        String result = codeGenerator.generateUniqueCode();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(8);
    }

    @Test
    void generateUniqueCode_ShouldReturnCodeWithValidCharacters() {
        String result = codeGenerator.generateUniqueCode();

        assertThat(result).matches("[A-Z0-9]{8}");
    }

    @Test
    void generateUniqueCode_ShouldReturnDifferentCodes() {
        String code1 = codeGenerator.generateUniqueCode();
        String code2 = codeGenerator.generateUniqueCode();

        // Es muy improbable que dos códigos generados secuencialmente sean iguales
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void generateUniqueCode_MultipleCalls_ShouldReturnValidCodes() {
        for (int i = 0; i < 100; i++) {
            String result = codeGenerator.generateUniqueCode();
            assertThat(result).isNotNull();
            assertThat(result).hasSize(8);
            assertThat(result).matches("[A-Z0-9]{8}");
        }
    }
}