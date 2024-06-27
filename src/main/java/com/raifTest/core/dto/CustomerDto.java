package com.raifTest.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CustomerDto {

    private UUID id;

    private String name;

    private String username;

    private String password;


    public CustomerDto(UUID id){
        this.id =id;
    }
}
