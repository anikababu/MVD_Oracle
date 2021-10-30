import Actions.PoliceAdminAction;
import Actions.SuperAdmin;
import Actions.UserAction;
import org.json.JSONObject;

import static spark.Spark.*;

public class mvdMain {
    public static void main(String[] args) {

        //Get user information
        get("/user/:id", (request, response) -> {
            UserAction uA = new UserAction();
            return uA.getUser(Integer.parseInt(request.params(":id")));
        });


        //Get user's vehicle + ticket information by user. A user can only view their own vehicle's details.
        get("/vehiclesByUser", (request, response) -> {
            UserAction uA = new UserAction();
            return uA.getVehicleInfo(request.queryParams("submitted_by"));
        });


        //Request change of vehicle details by user. A user can only raise change for their own vehicles.
        post("/request_change/:v_id", (request, response) -> {
            UserAction uA = new UserAction();
            JSONObject jBody = new JSONObject(request.body());
            return uA.postVehicleChange(request.params("v_id"), Integer.parseInt(request.queryParams("submitted_by")), jBody);
        });

        //Request change to the raised change request. A user can only raise change for their own vehicles.
        /*
        http://localhost:4567/update_change_request/100008?submitted_by=1001
        {
         "state":"NEWSTATE",
         "model":"NEWMODEL"
        }
         */
        patch("/update_change_request/:cr_id", (request, response) -> {

            UserAction uA = new UserAction();
            JSONObject jBody = new JSONObject(request.body());
            return uA.patchUpdateVehicleChange(request.params("cr_id"), Integer.parseInt(request.queryParams("submitted_by")), jBody);
        });


        //Raise ticket against a vehicle by a police/admin. Body -> case_desc, fine_amount, proof_of_payment.
        /*
        http://localhost:4567/raise_ticket/KA-53-CS-7145?submitted_by=101
        {
        "case_desc":"overspeed",
        "fine_amount":10000,
        }
         */
        post("/raise_ticket/:v_id", (request, response) -> {
            PoliceAdminAction pA = new PoliceAdminAction();
            JSONObject jBody = new JSONObject(request.body());
            return pA.postVehicleTicket(request.params("v_id"), Integer.parseInt(request.queryParams("submitted_by")), jBody);
        });


        //Get tickets raised against a vehicle. Only police/admin can get the details.
        //http://localhost:4567/ticketsByVehicle/KL-07-CS-7145?submitted_by=100
        get("/ticketsByVehicle/:v_id", (request, response) -> {
            PoliceAdminAction pA = new PoliceAdminAction();
            return pA.getTicketsByVehicle(request.params(":v_id"), Integer.parseInt(request.queryParams("submitted_by")));
        });


        //Submit proof of payment by User. User will be able to update only tickets issued in their name.
        /*
        http://localhost:4567/submit_proof_of_payment/50004?submitted_by=1001
        {
        "proof_of_payment":"true"
        }
         */
        post("/submit_proof_of_payment/:t_id", (request, response) -> {
            UserAction uA = new UserAction();
            JSONObject jBody = new JSONObject(request.body());
            return uA.postSubmitTicketPaymentProof(request.params("t_id"), Integer.parseInt(request.queryParams("submitted_by")), jBody);
        });


        //Route a change request to any police/admin. Only admin can do this.
        /*
        http://localhost:4567/route_change_request_to/100001/to/1?submitted_by=2
         */
        post("/route_change_request_to/:cr_id/to/:id", (request, response) -> {
            SuperAdmin s = new SuperAdmin();
            return s.postRouteChangeRequestTo(request.params("cr_id"), request.params("id"), Integer.parseInt(request.queryParams("submitted_by")));
        });


        //Get Open /Pending change requests + tickets assigned to a police. Only police/admin can see this.
        get("/view_inbox", (request, response) -> {
            PoliceAdminAction pA = new PoliceAdminAction();
            return pA.getInbox(Integer.parseInt(request.queryParams("submitted_by")));
        });


        //Approve/Reject ticket/change_request by owner police/admin.
        //http://localhost:4567/update_status/tickets/50007/_Approve?submitted_by=101
        //http://localhost:4567/update_status/change_requests/50007/_Approve?submitted_by=101
        post("/update_status/:table/:r_id/:operation", (request, response) -> {
            PoliceAdminAction pA = new PoliceAdminAction();
            return pA.postUpdateStatus(request.params("table"), request.params("r_id"), request.params("operation"), Integer.parseInt(request.queryParams("submitted_by")));
        });


        //Admin Deletes Records created by Admin itself.
        delete("/:table/:r_id", (request, response) -> {
            SuperAdmin s = new SuperAdmin();
            return s.deleteRecord(request.params("table"), request.params("r_id"), Integer.parseInt(request.queryParams("submitted_by")));
        });
    }
}
