package com.raifTest;

import com.raifTest.core.models.Customer;
import com.raifTest.core.respositories.ICustomerRepo;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = RaifTestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        // locations = "resources",
        locations = {"classpath:application-test.properties"}
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerRepoTests {

    @Autowired
    ICustomerRepo repo;

    UUID createAndDeleteUUID =UUID.fromString("d1299a51-b755-4e7d-83bf-a413a8041c19");
    String customerUsername = "testingUsername";

    @Test
    @Order(1)
    void createCustomer_true(){
        Customer customer = new Customer(
                createAndDeleteUUID,
                "name",
                customerUsername,
                "password"
        );
        assertEquals(true, repo.createCustomer(customer));
    }

    @Test
    @Order(2)
    void updateCustomer_true(){
        Customer customer = new Customer(
                createAndDeleteUUID,
                "nameeeee",
                customerUsername,
                "passwordddd"
        );

        assertEquals(true,repo.updateCustomer(customer));
    }

    @Test
    @Order(3)
    void isCustomerExistsByUsername_true(){

        assertEquals(true, repo.isCustomerExistsByUsername(customerUsername));
    }

    @Test
    @Order(4)
    void deleCustomerById_true(){
        assertEquals(true, repo.deleteCustomerById(createAndDeleteUUID));
    }
}
