package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Author;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

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
    public Author getById(Long id) {
        //Author.class는 하이버네이트가 엔터티로 매핑되어 있기 때문에 하이버네이트가 알고 있는 클래스를 찾도록 요청하는 클래스입니다.
        return getEntityManager().find(Author.class, id);
    }

    @Override
    public Author findAuthorByName(String firstName, String lastName) {
        //namedParameter
        TypedQuery<Author> query = getEntityManager().createQuery("SELECT a from Author a " +
                "WHERE a.firstName = :first_name and a.lastName = : last_name",Author.class);

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
