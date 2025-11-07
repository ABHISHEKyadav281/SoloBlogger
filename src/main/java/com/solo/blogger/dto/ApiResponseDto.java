package com.solo.blogger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDto<T> {
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponseDto <T> success ( T data) {
       return new ApiResponseDto<>("200", "success", data);
    }

    public static ApiResponseDto<Void> error(String code, String message) {
        return new ApiResponseDto<>(code, message, null);
    }
}
