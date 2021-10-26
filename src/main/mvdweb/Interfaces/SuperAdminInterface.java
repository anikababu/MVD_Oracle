package Interfaces;

public interface SuperAdminInterface {
    String postRouteChangeRequestTo(String cr_id, String assignee_id, int submitted_by);

    String deleteRecord(String table, String case_id, int submitted_by);
}
