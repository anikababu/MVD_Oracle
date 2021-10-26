package Actions;

import Database.DB;
import Interfaces.PoliceAdminInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.Set;

public class PoliceAdminAction implements PoliceAdminInterface {

    @Override
    public String postVehicleTicket(String v_id, int submitted_by, JSONObject body) {
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();
            Set<String> keys = body.keySet();

            if (!keys.isEmpty()) {
                String validateQuery = "select 1 from vehicles where v_id=\"" + v_id + "\" and exists (select 1 from users where id=" + submitted_by + " and role in  (\"police\",\"admin\"));";
                PreparedStatement vQ = sqlConn.prepareStatement(validateQuery);
                ResultSet valSet = vQ.executeQuery();
                String output = "";

                if (valSet.next()) {
                    output = valSet.getString("1");
                }

                if (!output.isEmpty()) {

                    String created_date = java.time.LocalDate.now().toString();
                    String case_desc = "";
                    int fine_amount = 0;
                    String proof_of_payment = "";
                    String status = "";
                    String closed_date = null;
                    Integer closed_by = null;
                    if (keys.contains("case_desc")) case_desc = body.getString("case_desc");
                    if (keys.contains("fine_amount")) fine_amount = body.getInt("fine_amount");
                    if (keys.contains("proof_of_payment")) proof_of_payment = body.getString("proof_of_payment");
                    if (keys.contains("status")) status = body.getString("status");
                    if (keys.contains("closed_date")) closed_date = body.getString("closed_date");

                    if (proof_of_payment.equalsIgnoreCase("TRUE")) {
                        status = "Closed";
                        closed_date = "\"" + java.time.LocalDate.now() + "\"";
                        closed_by = submitted_by;
                    } else {
                        proof_of_payment = "false";
                        status = "Open";
                    }

                    String insertQuery = "INSERT INTO TICKETS(v_num,created_date,created_by,case_desc,fine_amount,proof_of_payment,status,closed_date, closed_by) values " +
                            "(\"" + v_id + "\",\"" + created_date + "\"," + submitted_by + ",\"" + case_desc + "\"," + fine_amount + ",\"" + proof_of_payment + "\",\"" + status + "\"," + closed_date + "," + closed_by + ");";
                    System.out.println(insertQuery);
                    Statement stmt = sqlConn.createStatement();
                    stmt.executeUpdate(insertQuery);

                    sqlConn.close();

                    return "Success";
                } else {
                    return "Wrong information";
                }
            } else {
                return "Empty body";
            }
        } catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    public String getTicketsByVehicle(String v_id, int submitted_by) {
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();

            String QueryWithAccess = "select * from tickets where v_num=\""+v_id+"\" and exists (select 1 from users where id=" + submitted_by + " and role in ('admin','police'));";

            PreparedStatement vQ = sqlConn.prepareStatement(QueryWithAccess);
            ResultSet rs = vQ.executeQuery();
            JSONArray arrTickets = new JSONArray();

            while (rs.next()) {
                String t_id = rs.getString("t_id");
                String created_date = rs.getString("created_date");
                String case_desc = rs.getString("case_desc");
                String fine_amount = rs.getString("fine_amount");
                String proof_of_payment = rs.getString("proof_of_payment");
                String status = rs.getString("status");
                String closed_date = rs.getString("closed_date");

                JSONObject jData = new JSONObject();
                jData.put("t_id",t_id);
                jData.put("v_id",v_id);
                jData.put("created_date",created_date);
                jData.put("case_desc",case_desc);
                jData.put("fine_amount",fine_amount);
                jData.put("proof_of_payment",proof_of_payment);
                jData.put("status",status);
                jData.put("closed_date",closed_date);

                arrTickets.put(jData);
            }

            return arrTickets.toString();

        } catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    public JSONObject getInbox(int submitted_by) {
        JSONObject jData = new JSONObject();
        try{
            DB conn = new DB();
            Connection sqlConn = conn.getCon();

            JSONArray arrTickets = new JSONArray();
            String ticketsQuery = "select * from tickets where created_by="+submitted_by+" and status!='Closed' and exists " +
                    "(select 1 from users where id="+submitted_by+" and role in ('admin','police'));";

            PreparedStatement tQ = sqlConn.prepareStatement(ticketsQuery);
            ResultSet tSet = tQ.executeQuery();

            while(tSet.next()) {
                JSONObject jTickets = new JSONObject();
                jTickets.put("t_id", tSet.getInt("t_id"));
                jTickets.put("v_num", tSet.getString("v_num"));
                jTickets.put("created_date", tSet.getDate("created_date"));
                jTickets.put("case_desc", tSet.getString("case_desc"));
                jTickets.put("fine_amount", tSet.getString("fine_amount"));
                jTickets.put("proof_of_payment", tSet.getString("proof_of_payment"));
                jTickets.put("status", tSet.getString("status"));
                jTickets.put("t_id", tSet.getString("t_id"));

                arrTickets.put(jTickets);
            }

            jData.put("Tickets",arrTickets);

            JSONArray arrChangeReqs = new JSONArray();

            String requestsQuery ="select * from change_requests where assignee="+submitted_by+" and status!='Closed' and exists " +
                    "(select 1 from users where id="+submitted_by+" and role in ('admin','police'));";

            PreparedStatement rQ = sqlConn.prepareStatement(requestsQuery);
            ResultSet rSet = rQ.executeQuery();

            while(rSet.next()) {
                JSONObject jChangeReq = new JSONObject();
                jChangeReq.put("cr_id", rSet.getInt("cr_id"));
                jChangeReq.put("v_id", rSet.getString("v_id"));
                jChangeReq.put("changes", rSet.getString("changes"));
                jChangeReq.put("status", rSet.getString("status"));

                arrChangeReqs.put(jChangeReq);
            }

            jData.put("Change Requests",arrChangeReqs);

            return jData;

        } catch(Exception e){
            e.printStackTrace();
            return jData.put("Error","Error fetching tickets+change requests information");
        }
    }

    @Override
    public String postUpdateStatus(String table, String case_id, String operation, int submitted_by) {
        try {
            DB conn = new DB();
            Connection sqlConn = conn.getCon();

            String updateSql="";

            if(operation.contains("Approve")){
                if(table.contains("ticket")) {
                    updateSql += "update tickets t set status='Closed',closed_date=\"" + java.time.LocalDate.now() + "\", closed_by="+submitted_by
                            +" where t_id="+case_id+" and (exists(select 1 from users u where u.role='police' and u.id="+submitted_by+" and u.id=t.created_by) " +
                            "or exists(select 1 from users u where u.role='admin' and u.id="+ submitted_by + "));";
                }
                else if(table.contains("change")) {
                    updateSql += "update change_requests t set status='Closed' where cr_id="+case_id+" and (exists(select 1 from users u where " +
                            "u.role='police' and u.id="+submitted_by+" and u.id=t.assignee) or exists(select 1 from users u where " +
                            "u.role='admin' and u.id="+ submitted_by + "));";
                }
            }

            if(!updateSql.isEmpty()){
                Statement stmt = sqlConn.createStatement();
                stmt.executeUpdate(updateSql);
                return "Success";
            }

            return "Error";

        }
        catch(Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }
}
