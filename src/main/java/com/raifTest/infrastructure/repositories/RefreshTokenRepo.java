package com.raifTest.infrastructure.repositories;

import com.raifTest.core.models.RefreshTokenModel;
import com.raifTest.core.respositories.IRefreshTokenRepo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.UUID;

@Repository
public class RefreshTokenRepo implements IRefreshTokenRepo {

    private SessionFactory sessionFactory;

    @Autowired
    public RefreshTokenRepo(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public RefreshTokenModel getTokenById(UUID tokenId)
    {
        Session session = sessionFactory.openSession();

        RefreshTokenModel token = session.get(RefreshTokenModel.class, tokenId);

        session.close();
        return token;
    }

    @Override
    public boolean createToken(RefreshTokenModel token)
    {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        try
        {
            transaction.begin();

            session.persist(token);

            transaction.commit();
        }
        catch(HibernateException e)
        {
            transaction.rollback();
            session.close();
            return false;
        }
        catch(Exception e)
        {
            transaction.rollback();
            session.close();
            throw e;
        }

        session.close();

        return true;
    }

    @Override
    public boolean deleteTokenById(UUID tokenId)
    {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<RefreshTokenModel> cd = cb.createCriteriaDelete(RefreshTokenModel.class);
        Root<RefreshTokenModel> root = cd.from(RefreshTokenModel.class);

        cd.where(root.get("id").in(tokenId));

        try
        {
            transaction.begin();
            session.createMutationQuery(cd).executeUpdate();
            transaction.commit();
        }
        catch(HibernateException e)
        {
            transaction.rollback();
            session.close();
            return false;
        }
        catch(Exception e)
        {
            transaction.rollback();
            session.close();
            throw e;
        }

        session.close();

        return true;
    }

    //TODO прописать
    @Override
    public boolean delteTokensByExpirationDate(Date today) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.getTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<RefreshTokenModel> delete = cb.createCriteriaDelete(RefreshTokenModel.class);
        Root<RefreshTokenModel> root =delete.from(RefreshTokenModel.class);

        delete.where(cb.lessThanOrEqualTo(root.get("expiredDate"), today));

        try{

        }catch (HibernateException e){
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
}
