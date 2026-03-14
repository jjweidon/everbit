package com.everbit.everbit.auth.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드 파라미터에 JWT 인증된 현재 사용자(owner_id)를 주입.
 * JwtAuthenticationFilter가 설정한 SecurityContext principal(Long)을 사용.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentOwnerId {
}
