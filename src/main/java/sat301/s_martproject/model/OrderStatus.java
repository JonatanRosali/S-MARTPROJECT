package sat301.s_martproject.model;

import jakarta.persistence.*;

@Entity
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int status_id;

    @Column(nullable = false, unique = true)
    private String status_name; // e.g. Pending, Confirmed, Delivered, Completed

    public OrderStatus() {}

    public OrderStatus(String status_name) {
        this.status_name = status_name;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }
}
