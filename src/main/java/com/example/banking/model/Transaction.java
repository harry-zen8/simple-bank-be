package com.example.banking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"value\"")
    private BigDecimal value;

    private Date date;

    @Column(name = "description")
    private String description;

    private String type;

    @Column(name = "from_account")
    private Long fromAccount;

    @Column(name = "to_account")
    private Long toAccount;
} 