package Interfaces;

import org.json.JSONObject;
//test
public interface PoliceAdminInterface {
    String postVehicleTicket(String v_id, int submitted_by, JSONObject body);

    String getTicketsByVehicle(String v_id, int submitted_by);

    JSONObject getInbox(int submitted_by);

    String postUpdateStatus(String table, String case_id, String operation, int submitted_by);
}
