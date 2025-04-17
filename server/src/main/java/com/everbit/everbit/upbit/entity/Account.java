package com.everbit.everbit.upbit.entity;

import com.everbit.everbit.global.entity.BaseTime;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTime {
    @Id
    @Column(name = "account_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @Setter
    private String upbitAccessKey;

    @Setter
    private String upbitSecretKey;



}
