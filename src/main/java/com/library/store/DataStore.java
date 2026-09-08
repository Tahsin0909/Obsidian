package com.library.store;

import com.library.model.*;
import com.library.util.PasswordUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory "database" for the whole application. Everything lives in plain
 * Java collections for the lifetime of the running program — no external
 * database, no network, no files. Restarting the app resets all data back
 * to the seeded demo state below.
 *
 * This is a classic Singleton (Encapsulation + single source of truth for all
 * DAOs).
 */
public final class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final List<Admin> admins = Collections.synchronizedList(new ArrayList<>());
    private final List<Member> members = Collections.synchronizedList(new ArrayList<>());
    private final List<Category> categories = Collections.synchronizedList(new ArrayList<>());
    private final List<Book> books = Collections.synchronizedList(new ArrayList<>());
    private final List<Borrowing> borrowings = Collections.synchronizedList(new ArrayList<>());
    private final List<Fine> fines = Collections.synchronizedList(new ArrayList<>());
    private final List<Reservation> reservations = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger userIdSeq = new AtomicInteger(0);
    private final AtomicInteger memberIdSeq = new AtomicInteger(0);
    private final AtomicInteger categoryIdSeq = new AtomicInteger(0);
    private final AtomicInteger bookIdSeq = new AtomicInteger(0);
    private final AtomicInteger borrowingIdSeq = new AtomicInteger(0);
    private final AtomicInteger fineIdSeq = new AtomicInteger(0);
    private final AtomicInteger reservationIdSeq = new AtomicInteger(0);

    private DataStore() {
        seedDemoData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ---------- ID generators ----------
    public int nextUserId() {
        return userIdSeq.incrementAndGet();
    }

    public int nextMemberId() {
        return memberIdSeq.incrementAndGet();
    }

    public int nextCategoryId() {
        return categoryIdSeq.incrementAndGet();
    }

    public int nextBookId() {
        return bookIdSeq.incrementAndGet();
    }

    public int nextBorrowingId() {
        return borrowingIdSeq.incrementAndGet();
    }

    public int nextFineId() {
        return fineIdSeq.incrementAndGet();
    }

    public int nextReservationId() {
        return reservationIdSeq.incrementAndGet();
    }

    // ---------- Raw list accessors (used by the DAO layer) ----------
    public List<Admin> admins() {
        return admins;
    }

    public List<Member> members() {
        return members;
    }

    public List<Category> categories() {
        return categories;
    }

    public List<Book> books() {
        return books;
    }

    public List<Borrowing> borrowings() {
        return borrowings;
    }

    public List<Fine> fines() {
        return fines;
    }

    public List<Reservation> reservations() {
        return reservations;
    }

    // ---------- Query / Search Helpers ----------

    public Admin findAdminByUsernameOrEmail(String identifier) {
        if (identifier == null)
            return null;
        String trimmed = identifier.trim();
        synchronized (admins) {
            for (Admin admin : admins) {
                if (trimmed.equalsIgnoreCase(admin.getUsername()) ||
                        (admin.getEmail() != null && trimmed.equalsIgnoreCase(admin.getEmail()))) {
                    return admin;
                }
            }
        }
        return null;
    }

    public Member findMemberByUsernameOrEmail(String identifier) {
        if (identifier == null)
            return null;
        String trimmed = identifier.trim();
        synchronized (members) {
            for (Member member : members) {
                if (trimmed.equalsIgnoreCase(member.getUsername()) ||
                        (member.getEmail() != null && trimmed.equalsIgnoreCase(member.getEmail()))) {
                    return member;
                }
            }
        }
        return null;
    }

    public User findUserByUsernameOrEmail(String identifier) {
        Admin admin = findAdminByUsernameOrEmail(identifier);
        if (admin != null) {
            return admin;
        }
        return findMemberByUsernameOrEmail(identifier);
    }

    public Book findBookById(int bookId) {
        synchronized (books) {
            for (Book b : books) {
                if (b.getBookId() == bookId) {
                    return b;
                }
            }
        }
        return null;
    }

    public Book findBookByIsbn(String isbn) {
        if (isbn == null)
            return null;
        synchronized (books) {
            for (Book b : books) {
                if (isbn.equalsIgnoreCase(b.getIsbn())) {
                    return b;
                }
            }
        }
        return null;
    }

    public Member findMemberById(int memberId) {
        synchronized (members) {
            for (Member m : members) {
                if (m.getMemberId() == memberId) {
                    return m;
                }
            }
        }
        return null;
    }

    public Category findCategoryById(int categoryId) {
        synchronized (categories) {
            for (Category c : categories) {
                if (c.getCategoryId() == categoryId) {
                    return c;
                }
            }
        }
        return null;
    }

    // ---------- Mutation Helpers ----------

    public void addBook(Book book) {
        if (book.getBookId() <= 0) {
            book.setBookId(nextBookId());
        }
        books.add(book);
    }

    public boolean updateBook(Book updated) {
        if (updated == null) {
            return false;
        }
        synchronized (books) {
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getBookId() == updated.getBookId()) {
                    books.set(i, updated);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteBook(int bookId) {
        synchronized (books) {
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getBookId() == bookId) {
                    books.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasActiveBorrowingsForBook(int bookId) {
        synchronized (borrowings) {
            for (Borrowing b : borrowings) {
                if (b.getBookId() == bookId && b.getStatus() == Borrowing.Status.ACTIVE) {
                    return true;
                }
            }
        }
        return false;
    }

    public Category addCategory(String name, String desc) {
        Category c = new Category(nextCategoryId(), name, desc);
        categories.add(c);
        return c;
    }

    public void addMember(Member member) {
        if (member.getMemberId() <= 0) {
            member.setMemberId(nextMemberId());
        }
        if (member.getUserId() <= 0) {
            member.setUserId(nextUserId());
        }
        members.add(member);
    }

    public void addMember(String name, String email, String phone, String address, String passwordHash) {
        addMember(name, email, email, phone, address, passwordHash);
    }

    public void addMember(String name, String username, String email, String phone, String address,
            String passwordHash) {
        Member m = new Member(nextMemberId(), nextUserId(), username, passwordHash, true,
                name, email, phone, address, LocalDate.now().minusMonths(2), Member.Status.ACTIVE);
        members.add(m);
    }

    public boolean updateMember(Member updated) {
        if (updated == null) {
            return false;
        }
        synchronized (members) {
            for (int i = 0; i < members.size(); i++) {
                if (members.get(i).getMemberId() == updated.getMemberId()) {
                    members.set(i, updated);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUsernameOrEmailTaken(String identifier, int excludeMemberId) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        String trimmed = identifier.trim();
        synchronized (admins) {
            for (Admin admin : admins) {
                if (trimmed.equalsIgnoreCase(admin.getUsername()) ||
                        (admin.getEmail() != null && trimmed.equalsIgnoreCase(admin.getEmail()))) {
                    return true;
                }
            }
        }
        synchronized (members) {
            for (Member member : members) {
                if (member.getMemberId() != excludeMemberId) {
                    if (trimmed.equalsIgnoreCase(member.getUsername()) ||
                            (member.getEmail() != null && trimmed.equalsIgnoreCase(member.getEmail()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Borrowing borrowBook(int memberId, int bookId, int days) {
        Book book = findBookById(bookId);
        if (book == null || book.getAvailableQuantity() <= 0) {
            return null;
        }
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        Borrowing borrowing = new Borrowing(memberId, bookId, LocalDate.now(), LocalDate.now().plusDays(days));
        borrowing.setBorrowingId(nextBorrowingId());
        borrowings.add(borrowing);
        return borrowing;
    }

    public boolean returnBook(int borrowingId) {
        synchronized (borrowings) {
            for (Borrowing b : borrowings) {
                if (b.getBorrowingId() == borrowingId && b.getStatus() == Borrowing.Status.ACTIVE) {
                    b.setStatus(Borrowing.Status.RETURNED);
                    b.setReturnDate(LocalDate.now());
                    Book book = findBookById(b.getBookId());
                    if (book != null) {
                        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // ---------- Fine Operations ----------

    public void addFine(Fine fine) {
        if (fine.getFineId() <= 0) {
            fine.setFineId(nextFineId());
        }
        if (fine.getFineDate() == null) {
            fine.setFineDate(LocalDate.now());
        }
        fines.add(fine);
    }

    public boolean updateFine(Fine updated) {
        if (updated == null) {
            return false;
        }
        synchronized (fines) {
            for (int i = 0; i < fines.size(); i++) {
                if (fines.get(i).getFineId() == updated.getFineId()) {
                    fines.set(i, updated);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteFine(int fineId) {
        synchronized (fines) {
            for (int i = 0; i < fines.size(); i++) {
                if (fines.get(i).getFineId() == fineId) {
                    fines.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public Fine findFineById(int fineId) {
        synchronized (fines) {
            for (Fine f : fines) {
                if (f.getFineId() == fineId) {
                    return f;
                }
            }
        }
        return null;
    }

    public Fine findFineByBorrowingId(int borrowingId) {
        synchronized (fines) {
            for (Fine f : fines) {
                if (f.getBorrowingId() == borrowingId) {
                    return f;
                }
            }
        }
        return null;
    }

    public List<Fine> findFinesByMemberId(int memberId) {
        List<Fine> result = new ArrayList<>();
        synchronized (fines) {
            for (Fine f : fines) {
                if (f.getMemberId() == memberId) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    public boolean payFine(int fineId, String paymentMethod, LocalDate paidDate) {
        synchronized (fines) {
            for (Fine f : fines) {
                if (f.getFineId() == fineId) {
                    f.setPaid(true);
                    f.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
                    f.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod : "Cash");
                    return true;
                }
            }
        }
        return false;
    }

    public int payAllFinesForMember(int memberId, String paymentMethod) {
        int count = 0;
        synchronized (fines) {
            for (Fine f : fines) {
                if (f.getMemberId() == memberId && !f.isPaid()) {
                    f.setPaid(true);
                    f.setPaidDate(LocalDate.now());
                    f.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod : "Cash");
                    count++;
                }
            }
        }
        return count;
    }

    public double getTotalOutstandingFines() {
        double total = 0.0;
        synchronized (fines) {
            for (Fine f : fines) {
                if (!f.isPaid()) {
                    total += f.getAmount();
                }
            }
        }
        return total;
    }

    public double getTotalCollectedFines() {
        double total = 0.0;
        synchronized (fines) {
            for (Fine f : fines) {
                if (f.isPaid()) {
                    total += f.getAmount();
                }
            }
        }
        return total;
    }

    public long calculateOverdueDays(Borrowing b) {
        if (b == null) {
            return 0;
        }
        LocalDate endDate = b.getReturnDate() != null ? b.getReturnDate() : LocalDate.now();
        if (b.getDueDate() != null && endDate.isAfter(b.getDueDate())) {
            return java.time.temporal.ChronoUnit.DAYS.between(b.getDueDate(), endDate);
        }
        return 0;
    }

    // ============================================================================
    // DEMO / SEED DATA
    // Credentials pattern: <role>@mail.com / 123456
    // ============================================================================
    private void seedDemoData() {
        String demoHash = PasswordUtil.hash("123456");

        // --- Admin account (allows login via "admin" or "admin@mail.com") ---
        Admin admin = new Admin(nextUserId(), "admin", demoHash, true,
                "System Administrator", "admin@mail.com");
        admins.add(admin);

        // --- Categories ---
        Category fiction = addCategory("Fiction", "Novels and fictional works");
        Category science = addCategory("Science", "Science and technology books");
        Category history = addCategory("History", "Historical books and biographies");
        Category cs = addCategory("Computer Science", "Programming and computing");
        Category math = addCategory("Mathematics", "Mathematics and statistics");

        // --- Demo Members (allows login via username "member" or email
        // "member@mail.com") ---
        addMember("Alice Rahman", "member", "member@mail.com", "01710000001", "12 University Road, Dhaka", demoHash);
        addMember("Bob Karim", "member2", "member2@mail.com", "01710000002", "45 Green Road, Dhaka", demoHash);
        addMember("Chloe Islam", "member3", "member3@mail.com", "01710000003", "8 Lake Circus, Dhaka", demoHash);

        // --- Demo Books (with valid ISBNs) ---
        addBook("9780132350884", "Clean Code", "Robert C. Martin", cs, "Prentice Hall", 2008, 3);
        addBook("9780262033848", "Introduction to Algorithms", "Thomas H. Cormen", cs, "MIT Press", 2009, 2);
        addBook("9780451524935", "1984", "George Orwell", fiction, "Signet Classic", 1961, 4);
        addBook("9780062316097", "Sapiens", "Yuval Noah Harari", history, "Harper", 2015, 2);
        addBook("9780553380163", "A Brief History of Time", "Stephen Hawking", science, "Bantam", 1988, 2);
        addBook("9780261102217", "The Hobbit", "J.R.R. Tolkien", fiction, "Houghton Mifflin", 1937, 3);
        addBook("9780201633610", "Design Patterns", "Erich Gamma", cs, "Addison-Wesley", 1994, 1);
        addBook("9780486243016", "A Brief History of Mathematics", "Isaac Asimov", math, "Fawcett", 1966, 2);

        // --- Demo borrowings ---
        // 1. Active normal loan (Alice)
        Member alice = members.get(0);
        Book cleanCode = books.get(0);
        cleanCode.setAvailableQuantity(cleanCode.getAvailableQuantity() - 1);
        Borrowing borrowing1 = new Borrowing(alice.getMemberId(), cleanCode.getBookId(),
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(4));
        borrowing1.setBorrowingId(nextBorrowingId());
        borrowings.add(borrowing1);

        // 2. Overdue loan (Bob Karim, overdue by 7 days)
        Member bob = members.get(1);
        Book introAlgorithms = books.get(1);
        introAlgorithms.setAvailableQuantity(introAlgorithms.getAvailableQuantity() - 1);
        Borrowing borrowing2 = new Borrowing(bob.getMemberId(), introAlgorithms.getBookId(),
                LocalDate.now().minusDays(21), LocalDate.now().minusDays(7));
        borrowing2.setBorrowingId(nextBorrowingId());
        borrowings.add(borrowing2);

        // 3. Past returned overdue loan (Chloe Islam)
        Member chloe = members.get(2);
        Borrowing borrowing3 = new Borrowing(nextBorrowingId(), chloe.getMemberId(), books.get(2).getBookId(),
                LocalDate.now().minusDays(25), LocalDate.now().minusDays(15),
                LocalDate.now().minusDays(12), Borrowing.Status.RETURNED);
        borrowings.add(borrowing3);

        // --- Demo Fines ---
        // Fine for Bob's active overdue loan (Unpaid, $7.00 @ $1/day)
        Fine fine1 = new Fine(nextFineId(), borrowing2.getBorrowingId(), bob.getMemberId(),
                7.00, false, LocalDate.now().minusDays(2), null, null, "Overdue by 7 days ($1.00/day)");
        fines.add(fine1);

        // Fine for Chloe's returned overdue loan (Paid, $3.00)
        Fine fine2 = new Fine(nextFineId(), borrowing3.getBorrowingId(), chloe.getMemberId(),
                3.00, true, LocalDate.now().minusDays(12), LocalDate.now().minusDays(11), "Cash", "Paid at front desk");
        fines.add(fine2);
    }

    private void addBook(String isbn, String title, String author, Category category,
            String publisher, int year, int qty) {
        Book b = new Book(nextBookId(), isbn, title, author, category.getCategoryId(), category.getCategoryName(),
                publisher, year, qty, qty);
        books.add(b);
    }
}
