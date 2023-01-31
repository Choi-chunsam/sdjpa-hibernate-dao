package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Book;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

@Component
public class BookDaoImpl implements BookDao{

    EntityManagerFactory emf;

    public BookDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Book getById(Long id) {
        EntityManager em = getEntityManager();
        em.close();
        return em.find(Book.class,id);
    }

    @Override
    public Book findByTitle(String title) {
        EntityManager em = getEntityManager();

        TypedQuery<Book> query = em.createQuery("SELECT b from Book b WHERE b.title = : title",Book.class);

        query.setParameter("title",title);
        em.close();
        return query.getSingleResult();
    }

    @Override
    public Book saveNewBook(Book book) {
        EntityManager em = getEntityManager();

        em.getTransaction();
        em.persist(book);
        em.flush();
        //엔티티매니저는 트랜잭션을 커밋하기 직전까지 데이터베이스에 엔티티를 저장하지 않고 내부 쿼리 저장소에 INSERT SQL을 모아둔다.
        em.getTransaction().commit();
        em.close();
        return book;
    }

    @Override
    public Book updateBook(Book book) {
        EntityManager em = getEntityManager();
        try {
            em.joinTransaction();
            em.merge(book);
            em.flush();
            em.clear();
            return em.find(Book.class,book.getId());
        }finally {
            em.close();
        }



    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        Book book = em.find(Book.class,id);
        em.remove(book);
        em.flush();
        em.getTransaction().commit();
        em.close();

    }

    private EntityManager getEntityManager(){
        return emf.createEntityManager();
    }
}
