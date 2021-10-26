package Interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public interface UserInterface {

    JSONObject getUser(int id) throws SQLException, ClassNotFoundException;

    JSONObject getVehicleInfo(String id);

    String postVehicleChange(String v_id, int submitted_by, JSONObject body) throws SQLException, ClassNotFoundException;

    String postSubmitTicketPaymentProof(String t_id, int submitted_by, JSONObject body);

}
