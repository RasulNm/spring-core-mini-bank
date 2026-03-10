package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.transaction.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.*;

@Component
public class UserService {

    private final Set<String> takenLogins;
    private final AccountService accountService;
    private final TransactionHelper transactionHelper;
    private final SessionFactory sessionFactory;

    public UserService(
            AccountService accountService,
            TransactionHelper transactionHelper,
            SessionFactory sessionFactory
    ) {
        this.transactionHelper = transactionHelper;
        this.sessionFactory = sessionFactory;
        this.takenLogins = new HashSet<>();
        this.accountService = accountService;
    }

    public User createUser(String login) {
        String normalizedLogin = validateLogin(login);
        if (takenLogins.contains(normalizedLogin)) {
            throw new IllegalArgumentException("User already exists with login=%s".formatted(normalizedLogin));
        }
        var user = new User(normalizedLogin, new ArrayList<>());
        transactionHelper.executeInTransaction(session -> {
            session.persist(user);
        });
        var defaultAccount = accountService.createAccount(user);
        takenLogins.add(normalizedLogin);
        return user;
    }

    public User findUserById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }
        return transactionHelper.executeInTransaction(session -> {
            User user = session.get(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("No such user with id=%s".formatted(id));
            }
            return user;
        });
    }

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("""
                                    SELECT u FROM User u
                                    LEFT JOIN FETCH u.accountList a
                                    """,
                            User.class
                    )
                    .list();
        }
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }
}