package com.raifTest.infrastructure.repositories;

import com.raifTest.core.enums.AccountType;
import com.raifTest.core.models.Account;
import com.raifTest.core.models.Customer;
import com.raifTest.core.respositories.IAccountRepo;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import org.hibernate.*;
import org.hibernate.query.MutationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class AccountRepo implements IAccountRepo {

    Logger logger = LoggerFactory.getLogger(AccountRepo.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Boolean createAccount(Account account) {

        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        try {
            logger.info("Creating new account");
            transaction.begin();
            session.persist(account);
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            return false;
        }
        catch (Exception e){
            transaction.rollback();
            return false;
        }
        finally {
            session.close();
        }

        return true;
    }

    @Override
    public Boolean refilAccountBalance(String accountSerial, double refilAmount) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        String hql = "update Account set balance=balance+:refil where serialNumber=:serial";

        MutationQuery update = session.createMutationQuery(hql);

        update.setParameter("refil", refilAmount);
        update.setParameter("serial", accountSerial);

        try {
            logger.info("Adding to balance");
            transaction.begin();
            update.executeUpdate();
            transaction.commit();
        }
        catch (HibernateException e){
            logger.error(e.getMessage());
            transaction.rollback();
            return false;
        }
        catch (Exception e){
            logger.error(e.getMessage());
            transaction.rollback();
            return false;
        }
        finally {
            session.close();
        }

        session.close();
        return true;
    }

    @Override
    public Boolean withdrawAccountBalance(String accountSerial, double withdrawAmount) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        String updateHql = "update Account set balance=balance-:withdraw where serialNumber=:serial";

        MutationQuery mq = session.createMutationQuery(updateHql);

        mq.setParameter("withdraw", withdrawAmount);
        mq.setParameter("serial", accountSerial);

        try{
            logger.info("withdraw from account + " + accountSerial);
            transaction.begin();
            mq.executeUpdate();
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            logger.error(e.getMessage());
            return  false;
        }
        catch (Exception e){
            transaction.rollback();
            logger.error(e.getMessage());
            return false;
        }
        finally {
            session.close();
        }

        return true;
    }

    @Override
    public Boolean deleteAccount(String accountSerial) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        Transaction transaction = session.getTransaction();

        CriteriaDelete<Account> cd = cb.createCriteriaDelete(Account.class);
        Root<Account> root = cd.from(Account.class);

        cd.where(cb.equal(root.get("serialNumber"), accountSerial));

        try{
            logger.info("Delete account with serial " +accountSerial);
            transaction.begin();
            session.createMutationQuery(cd).executeUpdate();
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            logger.error(e.getMessage());
            return  false;
        }
        catch (Exception e){
            transaction.rollback();
            logger.error(e.getMessage());
            return false;
        }
        finally {
            session.close();
        }

        return true;
    }

    @Override
    public Account getAccountBySerial(String accountSerial) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);

        cq.select(root).where(cb.equal(root.get("serialNumber"), accountSerial));

        Account result = session.createQuery(cq).uniqueResult();

        session.close();
        return result;
    }

    @Override
    public List<Account> getAllAccountsByCustomerId(UUID customerId) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);

        // находим пользователя, а по id
        Subquery<Customer> customerSub = cq.subquery(Customer.class);
        Root<Customer> subRoot = customerSub.from(Customer.class);

        customerSub.select(subRoot)
                .where(cb.equal(subRoot.<UUID>get("id"), customerId));

        cq.select(root)
                .where(cb.equal(root.get("customer"), customerSub));

        List<Account> accounts = session.createQuery(cq).getResultList();

        session.close();
        return accounts;
    }

    @Override
    public List<Account> getAccountsByCustomerAndAccountType(UUID customerId, AccountType accountType) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);

        Subquery<Customer> customerSubquery = cq.subquery(Customer.class);
        Root<Customer> subRoot = customerSubquery.from(Customer.class);

        customerSubquery.select(subRoot).where(cb.equal(subRoot.get("id"),customerId));

        cq.select(root)
                .where(cb.and(
                        cb.equal(root.get("customer"), customerSubquery),
                        cb.equal(root.get("type"), accountType)
                        )
                );

        List<Account> result = session.createQuery(cq).getResultList();

        session.close();
        return result;
    }

    @Override
    public List<Account> getAccountsByBalanceAndCustomer(UUID customerId, double balance) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);

        Subquery<Customer> customerSubquery = cq.subquery(Customer.class);
        Root<Customer> subRoot = customerSubquery.from(Customer.class);

        customerSubquery.select(subRoot)
                .where(cb.equal(subRoot.get("id"),customerId));

        cq.select(root)
                .where(cb.and(
                        cb.equal(root.get("customer"), customerSubquery),
                        cb.ge(root.get("balance"), balance)
                        )
                );

        List<Account> result = session.createQuery(cq).getResultList();

        session.close();
        return result;
    }

    @Override
    public List<Account> getAccountsByCreationDateAndCustomer(UUID customerId, Date date) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);

        Subquery<Customer> customerSubquery = cq.subquery(Customer.class);
        Root<Customer> subRoot = customerSubquery.from(Customer.class);

        customerSubquery.select(subRoot).where(cb.equal(subRoot.get("id"),customerId));

        cq.select(root)
                .where(cb.and(
                        cb.equal(root.get("customer"), customerSubquery),
                        cb.greaterThanOrEqualTo(root.<Date>get("creationDate"), date)
                        )
                );

        List<Account> result = session.createQuery(cq).getResultList();

        session.close();
        return result;
    }

    @Override
    public Boolean transferFromAccountToAccount(String serialFrom,
                                                String serialTo,
                                                double amountInSerialFromCur,
                                                double amountInSerialToCur) {

        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        Transaction transaction = session.getTransaction();

        // hql для обновления записи отправителя
        String fromUpdateHql = "UPDATE Account set balance=balance-:amount WHERE serialNumber=:serial";
        //hql для обновления записи получателя
        String toUdpateHql = "UPDATE Account set balance=balance+:amount WHERE serialNumber=:serial";

        // обновление отправителя
        MutationQuery updateFrom = session.createMutationQuery(fromUpdateHql);
        updateFrom.setParameter("amount", amountInSerialFromCur);
        updateFrom.setParameter("serial",serialFrom);

        // обновление получателя
        MutationQuery updateTo = session.createQuery(toUdpateHql);
        updateTo.setParameter("amount", amountInSerialToCur);
        updateTo.setParameter("serial",serialTo);

        try{
            logger.info("Transfer from account " + serialFrom +" to account "+ serialTo);
            transaction.begin();
            updateFrom.executeUpdate();
            updateTo.executeUpdate();
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            logger.error(e.getMessage());
            return  false;
        }
        catch (Exception e){
            transaction.rollback();
            logger.error(e.getMessage());
            return false;
        }
        finally {
            session.close();
        }

        return true;
    }
}
