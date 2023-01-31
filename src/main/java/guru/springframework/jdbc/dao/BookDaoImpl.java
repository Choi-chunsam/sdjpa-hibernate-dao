package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Book;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

@Component
public class BookDaoImpl implements BookDao{

    EntityManagerFactory emf;

    public BookDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Book> findAll() {
        EntityManager em = getEntityManager();
        try{
            TypedQuery<Book> query = em.createNamedQuery("book_find_all",Book.class);
            return query.getResultList();
        }finally {
            em.close();
        }

    }

    @Override
    public Book findByISBN(String isbn) {
        EntityManager em = getEntityManager();

        try{
            TypedQuery<Book> query = em.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn",Book.class);
            query.setParameter("isbn",isbn);


            Book book = query.getSingleResult();

            return book;
        }finally {
            em.close();
        }

    }

    @Override
    public Book getById(Long id) {
        EntityManager em = getEntityManager();
        em.close();
        return em.find(Book.class,id);
    }

    @Override
    public Book findByTitleCriteria(String title) {
        EntityManager em = getEntityManager();

        try{
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Book> criteriaQuery = criteriaBuilder.createQuery(Book.class);

            Root<Book> root = criteriaQuery.from(Book.class);

            ParameterExpression<String> titleParam = criteriaBuilder.parameter(String.class);

            Predicate titlePred = criteriaBuilder.equal(root.get("title"),titleParam);

            criteriaQuery.select(root).where(criteriaBuilder.and(titlePred));

            TypedQuery<Book> typedQuery = em.createQuery(criteriaQuery);
            typedQuery.setParameter(titleParam,title);


            return typedQuery.getSingleResult();
        }finally {
            em.close();
        }
    }

    @Override
    public Book findByTitleNative(String title) {
        EntityManager em = getEntityManager();

        try{

            Query query = em.createNativeQuery("SELECT * FROM Book b where b.title =:title", Book.class);
            query.setParameter("title",title);

            return (Book) query.getSingleResult();
        }finally {
            em.close();
        }
    }

    @Override
    public Book findByTitle(String title) {
        EntityManager em = getEntityManager();

        //TypedQuery<Book> query = em.createQuery("SELECT b from Book b WHERE b.title = : title",Book.class);

        try {
            TypedQuery<Book> query = em.createNamedQuery("find_by_title",Book.class);
            query.setParameter("title",title);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public Book saveNewBook(Book book) {
        //엔티티매니저는 트랜잭션을 커밋하기 직전까지 데이터베이스에 엔티티를 저장하지 않고 내부 쿼리 저장소에 INSERT SQL을 모아둔다.
        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        em.persist(book);
        em.flush();
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
