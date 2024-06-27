package com.raifTest.core.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raifTest.core.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    @Column(name = "serial_number", length = 20)
    @Id
    @Pattern(regexp = "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d")
    private String serialNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    @JsonBackReference
    private Customer customer;

    // сколько осталось на счету
    @Min(0)
    private double balance;

    @Column(name = "creation_date")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    public Account(){}
}
