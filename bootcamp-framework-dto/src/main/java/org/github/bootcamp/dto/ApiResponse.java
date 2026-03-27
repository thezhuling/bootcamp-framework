package org.github.bootcamp.dto;

import org.github.bootcamp.dto.enums.RespStatus;

/**
 * Unified API response type using Java sealed interface + record implementations.
 * Callers use pattern matching switch to handle Success and Failure cases.
 *
 * <pre>{@code
 * return switch (response) {
 *     case ApiResponse.Success<T> s -> ResponseEntity.ok(s.data());
 *     case ApiResponse.Failure<T> f -> ResponseEntity.status(f.code()).body(f);
 * };
 * }</pre>
 *
 * @author zhuling
 */
public sealed interface ApiResponse<T>
    permits ApiResponse.Success, ApiResponse.Failure {

    record Success<T>(T data) implements ApiResponse<T> {}

    record Failure<T>(int code, String message) implements ApiResponse<T> {}

    static <T> ApiResponse<T> ok(T data) {
        return new Success<>(data);
    }

    static <T> ApiResponse<T> fail(RespStatus status) {
        return new Failure<>(status.getValue(), status.getDesc());
    }

    static <T> ApiResponse<T> fail(int code, String message) {
        return new Failure<>(code, message);
    }
}
