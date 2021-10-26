package Actions;

import Database.DB;
import Interfaces.SuperAdminInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SuperAdmin implements SuperAdminInterface {

    @Override
    public String postRouteChangeRequestTo(String cr_id, String assignee_id, int submitted_by) {
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();
            String validateQuery = "select 1 from users where id="+submitted_by+" and role='admin' and " +
                    "exists(select 1 from users where id="+ assignee_id+" and role in ('police','admin'))" +
                    "and exists(select 1 from change_requests where cr_id="+cr_id+");";

            PreparedStatement vQ = sqlConn.prepareStatement(validateQuery);
            ResultSet valSet = vQ.executeQuery();

            if(valSet.next()) {

                String updateQuery = "update change_requests set assignee="+assignee_id+" where cr_id="+cr_id+";";

                Statement stmt = sqlConn.createStatement();
                stmt.executeUpdate(updateQuery);

                return "Success";
            }

            else return "Wrong information";

        }

        catch (Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

    @Override
    public String deleteRecord(String table, String case_id, int submitted_by) {
        try{

            DB conn = new DB();
            Connection sqlConn = conn.getCon();
            String delQuery = "delete from "+table+" where t_id="+case_id+" and created_by="+submitted_by+" and " +
                    "exists(select 1 from users where role='admin' and id="+submitted_by+");";

            Statement stmt = sqlConn.createStatement();
            stmt.executeUpdate(delQuery);

            return "Success";

        } catch(Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }
}
