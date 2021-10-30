package Actions;

import Database.DB;
import Interfaces.UserInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.Set;

public class UserAction implements UserInterface {

    @Override
    public JSONObject getUser(int id) {

        JSONObject jUser = new JSONObject();
        try {

            DB conn = new DB();
            Connection sqlConn = conn.getCon();

            String reqSql = "select * from users where id=" + id;
            PreparedStatement ps = sqlConn.prepareStatement(reqSql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int dId = rs.getInt("id");
                String name = rs.getString("name");
                String role = rs.getString("role");

                jUser.put("ID", dId);
                jUser.put("Name", name);
                jUser.put("Role", role);
            }

            sqlConn.close();
            return jUser;

        } catch (Exception e){
            e.printStackTrace();
            return jUser.put("Error","Error while fetching user information");
        }
    }

    @Override
    public JSONObject getVehicleInfo(String id) {
        JSONObject jData = new JSONObject();
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();

            JSONArray arrVehicles = new JSONArray();
            String reqSql = "select v.*, u.name from vehicles v, users u where v.rc_owner=u.id and v.rc_owner=" + id;
            PreparedStatement ps = sqlConn.prepareStatement(reqSql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String v_id = rs.getString("v_id");
                String state = rs.getString("state");
                String model = rs.getString("model");
                String name = rs.getString("name");

                JSONObject jVehicle = new JSONObject();
                jVehicle.put("Owner", name);
                jVehicle.put("Details", model);
                jVehicle.put("State", state);
                jVehicle.put("Reg.Number", v_id);
                arrVehicles.put(jVehicle);
            }

            jData.put("Vehicles",arrVehicles);

            JSONArray arrTickts = new JSONArray();
            String ticketsSql = "select t.*,v.* from tickets t, vehicles v where t.v_num=v.v_id and v.rc_owner=" + id+";";
            PreparedStatement ts = sqlConn.prepareStatement(ticketsSql);
            ResultSet tss = ts.executeQuery();

            while (tss.next()) {
                Integer t_id = tss.getInt("t_id");
                String v_id = tss.getString("v_num");
                Date created_date = tss.getDate("created_date");
                String case_desc = tss.getString("case_desc");
                Integer fine_amount = tss.getInt("fine_amount");
                String proof_of_payment = tss.getString("proof_of_payment");
                String status = tss.getString("status");
                Date closed_date = tss.getDate("closed_date");

                JSONObject jTicket = new JSONObject();
                jTicket.put("t_id", t_id);
                jTicket.put("v_num", v_id);
                jTicket.put("created_date", created_date);
                jTicket.put("case_desc", case_desc);
                jTicket.put("fine_amount", fine_amount);
                jTicket.put("proof_of_payment", proof_of_payment);
                jTicket.put("status", status);
                jTicket.put("closed_date", closed_date);

                arrTickts.put(jTicket);
            }

            jData.put("Tickets",arrTickts);

            sqlConn.close();
            return jData;
        }
        catch (Exception e){
            e.printStackTrace();
            return new JSONObject("{\"Error\":\"Error fetching vehicle details\"}");
        }
    }

    @Override
    public String postVehicleChange(String v_id, int submitted_by, JSONObject body) {
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();
            Set<String> keys = body.keySet();

            if (!keys.isEmpty()) {
                String validateQuery = "select * from vehicles where v_id=\"" + v_id + "\" and rc_owner=" + submitted_by;
                PreparedStatement vQ = sqlConn.prepareStatement(validateQuery);
                ResultSet valSet = vQ.executeQuery();

                String dbChanges = body.toString().replace("\"","\\\"");

                while (valSet.next()) {
                    try {
                        String insertQuery = "INSERT INTO CHANGE_REQUESTS(v_id,changes,assignee,status) values (\"" + v_id + "\",\"" + dbChanges + "\"," + 1 + ",\"Open\");";
                        Statement stmt = sqlConn.createStatement();
                        stmt.executeUpdate(insertQuery);

                        sqlConn.close();
                        return "Success";
                    } catch (Exception e) {
                        return "Error";
                    }
                }
            }

            return "Empty Body";

        } catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    public String postSubmitTicketPaymentProof(String t_id, int submitted_by, JSONObject body) {
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();
            Set<String> keys = body.keySet();
            if(!keys.isEmpty()){
                String validateQuery = "select 1 from tickets t, vehicles v where t.v_num=v.v_id and t.t_id="+t_id+" and v.rc_owner="+submitted_by+";";

                PreparedStatement vQ = sqlConn.prepareStatement(validateQuery);
                ResultSet valSet = vQ.executeQuery();

                if(valSet.next()) {
                    String proof_of_payment="";
                    if(keys.contains("proof_of_payment")) proof_of_payment = body.getString("proof_of_payment");

                    String updateQuery = "update tickets set proof_of_payment='"+proof_of_payment+"' where t_id="+t_id+";";

                    Statement stmt = sqlConn.createStatement();
                    stmt.executeUpdate(updateQuery);

                    return "Success";
                }

                else return "Wrong information";
            }
            else return "Empty Body";

        }
        catch (Exception e){
            e.printStackTrace();
            return "Error";
        }

    }

    @Override
    public String patchUpdateVehicleChange(String cr_id, int submitted_by, JSONObject body){
        try {

            DB conn = new DB();
            Connection sqlConn = conn.getCon();
            Set<String> keys = body.keySet();

            if (!keys.isEmpty()) {
                String validateQuery = "select * from vehicles v,change_requests c where cr_id=" + cr_id + " " +
                        "and c.v_id = v.v_id and rc_owner=" + submitted_by;

                Statement vQ = sqlConn.createStatement();
                ResultSet valSet = vQ.executeQuery(validateQuery);

                String old_changes="";

                while(valSet.next()){
                    old_changes = valSet.getString("changes");
                }

                if(!old_changes.isEmpty()){
                    JSONObject temp = new JSONObject(old_changes);

                    for(String key: keys){
                        temp.put(key,body.getString(key));
                    }
                    String updateQuery = "UPDATE CHANGE_REQUESTS set changes=\""+temp.toString().replace("\"","\\\"")+
                            "\" where cr_id="+cr_id ;


                    Statement stmt = sqlConn.createStatement();
                    stmt.executeUpdate(updateQuery);

                    sqlConn.close();
                    return "Success";

                }
                else{
                    System.out.println("Authentication error");
                }

            }

            return "Empty Body";

        } catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }

}
