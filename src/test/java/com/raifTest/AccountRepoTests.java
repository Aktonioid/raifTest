package com.raifTest;

import com.raifTest.core.enums.AccountType;
import com.raifTest.core.models.Account;
import com.raifTest.core.models.Customer;
import com.raifTest.core.respositories.IAccountRepo;
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


import java.util.Date;
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
public class AccountRepoTests {

    @Autowired
    IAccountRepo repo;

    String serialForCreateAndDelete = "40817840353309778079";
    String serialForSend = "40817810176854866777";
    String serialTransferTo = "40817810176854866777";
    UUID customerId = UUID.fromString("bf4ef7fc-0e3c-4166-92a4-ed5ada85e61e");

    @Test
    @Order(1)
    public void createAccount_true(){

        Account account = new Account(
                serialForCreateAndDelete,
                new Customer(customerId),
                0,
                new Date(),
                AccountType.RUB
        );

        assertEquals(true, repo.createAccount(account));
    }

    @Test
    @Order(2)
    public void refilBalance_true(){
        assertEquals(true,repo.refilAccountBalance(serialForSend, 1000));
    }

    @Test
    @Order(3)
    public void withdrawBalance_true(){

        assertEquals(true, repo.withdrawAccountBalance(serialForSend,1000));
    }

    @Test
    @Order(4)
    public void transferFromAccountToAccount_true(){

        assertEquals(true, repo.transferFromAccountToAccount(serialForSend, serialTransferTo, 1,1));
    }

    @Test
    @Order(5)
    public void deleteAccount_true(){

        assertEquals(true, repo.deleteAccount(serialForCreateAndDelete));
    }
}
