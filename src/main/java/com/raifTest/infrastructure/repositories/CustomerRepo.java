package com.raifTest.infrastructure.repositories;

import com.raifTest.core.models.Customer;
import com.raifTest.core.respositories.ICustomerRepo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class CustomerRepo implements ICustomerRepo {


    @Autowired
    SessionFactory sessionFactory;


    public CustomerRepo(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Boolean createCustomer(Customer customer) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        try {
            transaction.begin();
            session.persist(customer);
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            return false;
        }
        catch (Exception e){
            transaction.rollback();
            return  false;
        }
        finally {
            session.close();
        }

        return true;
    }

    @Override
    public Boolean updateCustomer(Customer customer) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        try {
            transaction.begin();
            session.merge(customer);
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            return false;
        }
        catch (Exception e){
            transaction.rollback();
            return  false;
        }
        finally {
            session.close();
        }

        return true;
    }

    @Override
    public Customer getCustomerById(UUID id) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> root = cq.from(Customer.class);
        cq.select(root).where(cb.equal(root.get("id"), id));

        Customer reslut = session.createQuery(cq).uniqueResult();

        session.close();
        return reslut;
    }

    @Override
    public Customer getCustomerByUsername(String username) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> root = cq.from(Customer.class);
        cq.select(root).where(cb.equal(root.get("username"), username));

        Customer reslut = session.createQuery(cq).uniqueResult();

        session.close();
        return reslut;
    }

    @Override
    public boolean isCustomerExistsByUsername(String username) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> root = cq.from(Customer.class);
        cq.select(root).where(cb.equal(root.get("username"), username));

        Customer reslut = session.createQuery(cq).uniqueResult();

        session.close();

        return reslut != null;
    }

    @Override
    public Boolean deleteCustomerById(UUID userId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Customer> cd = cb.createCriteriaDelete(Customer.class);
        Root<Customer> root = cd.from(Customer.class);

        cd.where(cb.equal(root.get("id"), userId));

        try {
            transaction.begin();
            session.createMutationQuery(cd).executeUpdate();
            transaction.commit();
        }
        catch (HibernateException e){
            transaction.rollback();
            return false;
        }
        catch (Exception e){
            transaction.rollback();
            return  false;
        }
        finally {
            session.close();
        }

        return true;
    }
}
