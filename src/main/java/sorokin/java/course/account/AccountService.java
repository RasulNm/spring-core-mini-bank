package sorokin.java.course.account;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.transaction.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.List;
import java.util.Optional;

@Component
public class AccountService {

    private final AccountProperties accountProperties;
    private final TransactionHelper transactionHelper;
    private final SessionFactory sessionFactory;

    public AccountService(AccountProperties accountProperties, TransactionHelper transactionHelper, SessionFactory sessionFactory) {
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
        this.sessionFactory = sessionFactory;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        return transactionHelper.executeInTransaction(session -> {
            Account newAccount = new Account(accountProperties.getDefaultAmount());
            newAccount.setUser(user);
            session.persist(newAccount);
            return newAccount;
        });
    }

    public Optional<Account> findAccountById(Integer id) {
        validatePositiveId(id, "account id");
        return Optional.ofNullable(transactionHelper.executeInTransaction(session -> {
            return session.get(Account.class, id);
        }));
    }

    public List<Account> getUserAccounts(User user) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                            SELECT a FROM Account a
                            WHERE a.user.id = :userId
                            """, Account.class)
                    .setParameter("userId", user.getId())
                    .list();
        }
    }

    public void withdraw(Integer fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);
        Account account = findAccountById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId)));

        if (amount > account.getMoneyAmount()) {
            throw new IllegalArgumentException(
                    "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                            .formatted(account.getId(), account.getMoneyAmount(), amount)
            );
        }
        transactionHelper.executeInTransaction(session -> {
            Account persistentAccount = session.merge(account);
            persistentAccount.setMoneyAmount(persistentAccount.getMoneyAmount() - amount);
        });
    }

    public void deposit(Integer toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);
        transactionHelper.executeInTransaction(session -> {
            Account account = session.get(Account.class, toAccountId);
            account.setMoneyAmount(account.getMoneyAmount() + amount);
        });
    }

    public Account closeAccount(Integer accountId) {
        validatePositiveId(accountId, "account id");
        Account accountToClose = findAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(accountId)));
        var userId = accountToClose.getUser();
        var userAccounts = getUserAccounts(userId);
        if (userAccounts.size() == 1) {
            throw new IllegalStateException("Can't close the only one account");
        }

        var accountToTransferMoney = userAccounts.stream()
                .filter(it -> !it.getId().equals(Long.valueOf(accountId)))
                .findFirst()
                .orElseThrow();

        var newAmount = accountToTransferMoney.getMoneyAmount() + accountToClose.getMoneyAmount();

        transactionHelper.executeInTransaction(session -> {
            Account account = session.get(Account.class, accountToTransferMoney.getId());
            account.setMoneyAmount(newAmount);
            Account persistentAccount = session.merge(accountToClose);
            session.remove(persistentAccount);
        });
        return accountToClose;
    }

    public void transfer(int fromAccountId, int toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("source and target account id must be different");
        }
        Account accountFrom = findAccountById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId)));
        Account accountTo = findAccountById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId)));

        if (amount > accountFrom.getMoneyAmount()) {
            throw new IllegalArgumentException(
                    "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
                            .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
            );
        }

        int amountToTransfer = accountTo.getUser().getId().longValue() == accountFrom.getUser().getId().longValue()
                ? amount
                : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));

        System.out.println("accountTo.getUser() == accountFrom.getUser() ->>" + (accountTo.getUser() == accountFrom.getUser())
                + "accountTo.getUser().getId().intValue() == accountFrom.getUser().getId().intValue() ->>" + (accountTo.getUser().getId().intValue() == accountFrom.getUser().getId().intValue()));

        transactionHelper.executeInTransaction(session -> {
            Account account1 = session.get(Account.class, accountFrom.getId());
            account1.setMoneyAmount(account1.getMoneyAmount() - amount);
            Account account2 = session.get(Account.class, accountTo.getId());
            account2.setMoneyAmount(account2.getMoneyAmount() + amountToTransfer);
        });
    }

    private void validatePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
