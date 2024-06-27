package com.raifTest.core.respositories;

import com.raifTest.core.enums.AccountType;
import com.raifTest.core.models.Account;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface IAccountRepo {

    public Boolean createAccount(Account account);

    /**
     * Пополнение баланса на величину равную refilAmount
     * @param accountSerial - номер счета
     * @param refilAmount - сумма которую кладут на баланс
     * @return true если удачно положили. False при ошибке зачисления
     */
    public Boolean refilAccountBalance(String accountSerial, double refilAmount);

    /**
     * Снятие с баланса на величину равную withdrawAmount
     * @param accountSerial - номер счета
     * @param withdrawAmount - сколько нужно вывести
     * @return - false -ошибка при выведении средств. True - средства сняты успешно
     */
    public Boolean withdrawAccountBalance(String accountSerial, double withdrawAmount);

    /**
     * Удаление счета
     * @param accountSerial - номер счета
     * @return true - удачно, false - неудачное удаление
     */
    public Boolean deleteAccount(String accountSerial);

    /**
     * Получение информации по счету по номеру счета
     * @param accountSerial - номер счета
     * @return Возвращает Account
     */
    public Account getAccountBySerial(String accountSerial);

    /**
     * Получение всех счетов пользователя
     * @param customerId - id  клиента
     * @return List счетов
     */
    public List<Account> getAllAccountsByCustomerId(UUID customerId);

    /**
     * Получение аккаунтов по пользователю с сортировкой по типу средств на аккаунте
     * @param customerId - id клиента
     * @param accountType - тип валюты счета
     * @return список моделей счета
     */
    public List<Account> getAccountsByCustomerAndAccountType(UUID customerId, AccountType accountType);

    /**
     * Получение счетов пользователя по размеру баланса, так что balance аккаунта >= balance в запросе
     * @param customerId
     * @param balance минимальный размер баланса для сортировки
     * @return
     */
    public List<Account> getAccountsByBalanceAndCustomer(UUID customerId, double balance);

    /**
     * Получение аккаунтов пользователя по дате создания начиная с указанной даты (creationDate >= date)
     * @param customerId - id клиента
     * @param date - дата для сортировки
     * @return List<Account></>
     */
    public List<Account> getAccountsByCreationDateAndCustomer(UUID customerId, Date date);

    /**
     * Перевод денег со счета на счет
     * @param serialFrom - номер счета с которого переводят
     * @param serialTo - номер счета на который переводят
     * @param amountInSerialFromCur - Количество переводимых средств в валюте отправителя
     * @param amountInSerialToCur - количество переводисых средств в валюте получателя
     * @return True - успешный перевод. False - ошибка при переводе
     */
    public Boolean transferFromAccountToAccount(String serialFrom,
                                                String serialTo,
                                                double amountInSerialFromCur,
                                                double amountInSerialToCur);
}
