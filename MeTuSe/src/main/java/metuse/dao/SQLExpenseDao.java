package metuse.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import metuse.domain.Expense;

public class SQLExpenseDao implements ExpenseDao {

    final private Database db;
    private List<Expense> expenses;

    public SQLExpenseDao(Database db) throws SQLException {
        this.db = db;
    }

    @Override
    public boolean create(Expense expense) {
        try {
            Connection c = db.getConnection();
            PreparedStatement s = c.prepareStatement("INSERT INTO Expenses(name, amount, user_id, date) "
                    + "VALUES (?, ?, ?, date('now', 'localtime'));");
            s.setString(1, expense.getName());
            s.setDouble(2, expense.getAmount());
            s.setInt(3, expense.getUserId());
            s.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @Override
    public List<Expense> getUserExpenses(int id) throws SQLException {
        expenses = new ArrayList<>();
        Connection c = db.getConnection();
        PreparedStatement s = c.prepareStatement("SELECT * FROM Expenses WHERE user_id = ?");
        s.setInt(1, id);
        ResultSet r = s.executeQuery();
        
        while(r.next()) {
            Expense e = new Expense(r.getString("name"), r.getDouble("amount"), id);
            expenses.add(e);
        }
        
        return expenses;
    }
}
