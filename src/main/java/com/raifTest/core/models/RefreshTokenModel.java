package com.raifTest.core.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenModel
{
    @Id
    private UUID id; // id записи(токена) гененрируется при создании пары
    // access - refresh и нужна для того чтобы получать новую пару только по одному refresh токену

    @Column(name = "expired_date")
    @Temporal(TemporalType.DATE)
    @Basic
    private Date expiredDate; // дата когда токен истекает

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String token;
}