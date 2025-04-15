package sat301.s_martproject.model;

import jakarta.persistence.*;

@Entity
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_details_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String recipient_name;

    private String detail_type; // "Home", "Work", "Other"

    @Column(nullable = false)
    private String phone_number;

    @Column(nullable = false)
    private String address;

    private String additional_info;

    @Column(nullable = false)
    private boolean isDefault;

    // Constructors
    public UserDetails() {}

    public UserDetails(User user, String recipient_name, String detail_type, String phone_number, String address, String additional_info, boolean isDefault) {
        this.user = user;
        this.recipient_name = recipient_name;
        this.detail_type = detail_type;
        this.phone_number = phone_number;
        this.address = address;
        this.additional_info = additional_info;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public Long getUser_details_id() {
        return user_details_id;
    }

    public void setUser_details_id(Long user_details_id) {
        this.user_details_id = user_details_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRecipient_name() {
        return recipient_name;
    }

    public void setRecipient_name(String recipient_name) {
        this.recipient_name = recipient_name;
    }

    public String getDetail_type() {
        return detail_type;
    }

    public void setDetail_type(String detail_type) {
        this.detail_type = detail_type;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdditional_info() {
        return additional_info;
    }

    public void setAdditional_info(String additional_info) {
        this.additional_info = additional_info;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
