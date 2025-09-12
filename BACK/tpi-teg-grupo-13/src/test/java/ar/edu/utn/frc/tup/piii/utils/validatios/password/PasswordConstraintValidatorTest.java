package ar.edu.utn.frc.tup.piii.utils.validatios.password;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordConstraintValidatorTest {

    private PasswordConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new PasswordConstraintValidator();
        validator.initialize(null); // Prueba el método initialize

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation())
                .thenReturn(context);
    }


    @Test
    void isValid_WithTooShortPassword_ShouldReturnFalse() {
        String shortPassword = "Abc1!";

        boolean result = validator.isValid(shortPassword, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithTooLongPassword_ShouldReturnFalse() {
        String longPassword = "ThisPasswordIsTooLong123!";

        boolean result = validator.isValid(longPassword, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithNoUpperCase_ShouldReturnFalse() {
        String noUpperCase = "lowercase1!";

        boolean result = validator.isValid(noUpperCase, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithNoLowerCase_ShouldReturnFalse() {
        String noLowerCase = "UPPERCASE1!";

        boolean result = validator.isValid(noLowerCase, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithNoDigit_ShouldReturnFalse() {
        String noDigit = "Password!";

        boolean result = validator.isValid(noDigit, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithNoSpecialCharacter_ShouldReturnFalse() {
        String noSpecial = "Password1";

        boolean result = validator.isValid(noSpecial, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithAlphabeticalSequence_ShouldReturnFalse() {
        String alphabeticalSeq = "Abcdef1!";

        boolean result = validator.isValid(alphabeticalSeq, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithNumericalSequence_ShouldReturnFalse() {
        String numericalSeq = "Pass12345!";

        boolean result = validator.isValid(numericalSeq, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithQwertySequence_ShouldReturnFalse() {
        String qwertySeq = "Qwerty1!";

        boolean result = validator.isValid(qwertySeq, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_WithWhitespace_ShouldReturnFalse() {
        String withWhitespace = "Pass word1!";

        boolean result = validator.isValid(withWhitespace, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

}