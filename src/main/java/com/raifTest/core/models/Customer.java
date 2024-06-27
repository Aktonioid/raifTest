package com.raifTest.core.models;

import com.raifTest.core.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@ToString
@AllArgsConstructor
@Entity
@Table(name = "customers")
//Просто модель-затычка, чтоб если что было бы проще прикрутить авторизацию
public class Customer implements UserDetails {

    @Id
    private UUID id;

    @Column
    private String name;

    private String password;

    @Column(unique = true)
    private String username;

    // инн
//    @Column(length = 12)
//    private String itn;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "customer")
    private Set<Account> accounts;

    public Customer(UUID id){
        this.id = id;
    }

    public Customer(UUID id, String name, String username, String password){
        this.id =id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public Customer(){}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
    }

}
