package ar.edu.utn.frc.tup.piii.dtos.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorApiTest {

    @Test
    void builder_ShouldCreateErrorApiCorrectly() {
        String timestamp = "123456789";
        Integer status = 404;
        String error = "Not Found";
        String message = "Resource not found";

        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(timestamp)
                .status(status)
                .error(error)
                .message(message)
                .build();

        assertThat(errorApi.getTimestamp()).isEqualTo(timestamp);
        assertThat(errorApi.getStatus()).isEqualTo(status);
        assertThat(errorApi.getError()).isEqualTo(error);
        assertThat(errorApi.getMessage()).isEqualTo(message);
    }

    @Test
    void noArgumentsConstructor_ShouldCreateErrorApiEmpty() {
        ErrorApi errorApi = new ErrorApi();

        assertThat(errorApi.getTimestamp()).isNull();
        assertThat(errorApi.getStatus()).isNull();
        assertThat(errorApi.getError()).isNull();
        assertThat(errorApi.getMessage()).isNull();
    }
}
