package com.everbit.everbit.global.util;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * UUID v7 생성기. 시간순 정렬 가능, thread-safe.
 * SoT: docs/architecture/data-model.md, docs/architecture/jpa-mapping.md.
 */
@UtilityClass
public class Uuids {

	/**
	 * UUID v7 생성. 외부 노출용 public_id에 사용.
	 */
	public static UUID next() {
		return UuidCreator.getTimeOrderedEpoch();
	}

	/**
	 * UUID v7 문자열 표현 (36자, 하이픈 포함).
	 */
	public static String nextString() {
		return next().toString();
	}
}
