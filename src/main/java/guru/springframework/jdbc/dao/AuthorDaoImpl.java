package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Author;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Queue;

/**
 * Created by jt on 8/28/21.
 */
@Component
public class AuthorDaoImpl implements AuthorDao {

    private final EntityManagerFactory emf;

    public AuthorDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Author> findAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Author> typedQuery = em.createNamedQuery("author_find_all",Author.class);

            return typedQuery.getResultList();
        }finally {
            em.close();
        }

    }

    @Override
    public List<Author> listAuthorByLastNameLike(String lastName) {
        EntityManager em = getEntityManager();

        try {
            Query query = em.createQuery("SELECT a from Author a WHERE a.lastName like :last_name");
            query.setParameter("last_name",lastName + "%");
            List<Author> authors = query.getResultList();

            return authors;
        }finally {
            em.close(); //connection pool을 위해
        }

    }

    @Override
    public Author getById(Long id) {
        //Author.class는 하이버네이트가 엔터티로 매핑되어 있기 때문에 하이버네이트가 알고 있는 클래스를 찾도록 요청하는 클래스입니다.
        return getEntityManager().find(Author.class, id);
    }

    @Override
    public Author findAuthorByNameCriteria(String firstName, String lastName) {
        EntityManager em = getEntityManager();

        try {
            //building the criteriaBuilder
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            //criteriaBuilder로부터 criteriaQuery객체를 얻는다.
            CriteriaQuery<Author> criteriaQuery = criteriaBuilder.createQuery(Author.class);

            //criteriaQuery로부터 Root Element를 얻고 Root는
            // predicate에 대한 element로부터 lastName과 firstName을 reference한다.
            Root<Author> root = criteriaQuery.from(Author.class);

            ParameterExpression<String> firstNameParam = criteriaBuilder.parameter(String.class);
            ParameterExpression<String> lastNameParam = criteriaBuilder.parameter(String.class);

            Predicate firstNamePred = criteriaBuilder.equal(root.get("firstName"),firstNameParam);
            Predicate lastNamePred = criteriaBuilder.equal(root.get("lastName"),lastNameParam);

            criteriaQuery.select(root).where(criteriaBuilder.and(firstNamePred,lastNamePred));

            TypedQuery<Author> typedQuery = em.createQuery(criteriaQuery);
            typedQuery.setParameter(firstNameParam,firstName);
            typedQuery.setParameter(lastNameParam,lastName);


            return typedQuery.getSingleResult();
        }finally {
            em.close();
        }
    }

    @Override
    public Author findAuthorByNameNative(String firstName, String lastName) {
        EntityManager em = getEntityManager();

        try {
            Query query = em.createNativeQuery("SELECT * FROM Author a WHERE a.first_name = ? and a.last_name = ?", Author.class);

            query.setParameter(1,firstName);
            query.setParameter(2,lastName);


            return (Author) query.getSingleResult();
        }finally {
            em.close();
        }
    }

    @Override
    public Author findAuthorByName(String firstName, String lastName) {
        EntityManager em = getEntityManager();
        //namedParameter
        //TypedQuery<Author> query = getEntityManager().createQuery("SELECT a from Author a " +
        //        "WHERE a.firstName = :first_name and a.lastName = :last_name",Author.class);
        TypedQuery<Author> query = em.createNamedQuery("find_by_name", Author.class);
        query.setParameter("first_name",firstName);
        query.setParameter("last_name",lastName);


        return query.getSingleResult();
    }

    @Override
    public Author saveNewAuthor(Author author) {
        EntityManager em = getEntityManager();
        //em.joinTransaction();
        em.getTransaction().begin();
        em.persist(author);
        em.flush();
        em.getTransaction().commit();
        em.close();
        return author;
    }

    @Override
    public Author updateAuthor(Author author) {
        EntityManager em = getEntityManager();
        try {
            em.joinTransaction();
            em.merge(author); //merge 존재하는 entity를 업데이트 해라.
            em.flush();//flush는 하이버네이트에게 데이터베이스에 기대해서 SQL트랜잭션을 실행시켜라.
            em.clear();//first level cache를 없앤다.
            return em.find(Author.class, author.getId());
        } finally {
            em.close();
        }


    }

    @Override
    public void deleteAuthorById(Long id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        Author author = em.find(Author.class,id);
        em.remove(author);//transactional context는 실행되지만, 이것은 lazy to lead다.바로 실행하지않는다.
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    //이것은 전형적인 팩토리 프로 애플리케이션 디자인 패턴이므로 여기서는 팩토리 패턴을 사용하고 있습니다.
    private EntityManager getEntityManager(){
        return emf.createEntityManager();
    }
}
