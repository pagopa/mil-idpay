package it.pagopa.swclient.mil.idpay.bean;

import it.pagopa.swclient.mil.idpay.ErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateTransaction {

    @NotNull(message = "[" + ErrorCode.ERROR_INITIATIVEID_MUST_NOT_BE_NULL + "] initiativeId must not be null")
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String initiativeId;

    @NotNull(message = "[" + ErrorCode.ERROR_TIMESTAMP_MUST_NOT_BE_NULL + "] timestamp must not be null")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d",
            message = "[" + ErrorCode.ERROR_TIMESTAMP_MUST_MATCH_REGEXP + "] timestamp must match \"{regexp}\"")
    private String timestamp;

    @NotNull(message = "[" + ErrorCode.ERROR_GOODSCOST_MUST_NOT_BE_NULL + "] goodsCost must not be null")
    @Min(value = 0L, message = "[" + ErrorCode.ERROR_GOODSCOST_MUST_BE_GREATER_THAN + "] goodsCost must be greater than {value}")
    @Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_GOODSCOST_MUST_BE_LESS_THAN + "] goodsCost must less than {value}")
    private Long goodsCost;

}
