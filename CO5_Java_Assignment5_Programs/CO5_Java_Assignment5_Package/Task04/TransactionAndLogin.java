import java.sql.*;
public class TransactionAndLogin {
    static void transfer(Connection c, int fromId, int toId, double amount) throws SQLException {
        String debit = "UPDATE wallet SET balance=balance-? WHERE wallet_id=? AND balance>=?";
        String credit = "UPDATE wallet SET balance=balance+? WHERE wallet_id=?";
        boolean old = c.getAutoCommit(); c.setAutoCommit(false);
        try (PreparedStatement d=c.prepareStatement(debit);
             PreparedStatement cr=c.prepareStatement(credit)) {
            d.setDouble(1,amount); d.setInt(2,fromId); d.setDouble(3,amount);
            if (d.executeUpdate()!=1) throw new SQLException("Insufficient funds or wallet missing");
            cr.setDouble(1,amount); cr.setInt(2,toId);
            if (cr.executeUpdate()!=1) throw new SQLException("Destination wallet missing");
            c.commit();
        } catch (SQLException e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(old); }
    }
    static boolean unsafeLogin(Connection c, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        try (Statement st=c.createStatement(); ResultSet rs=st.executeQuery(sql)) { return rs.next(); }
    }
    static boolean safeLogin(Connection c, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=? AND pwd=?";
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,id); ps.setString(2,pwd);
            try (ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }
}
