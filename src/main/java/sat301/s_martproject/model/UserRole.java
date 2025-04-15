package sat301.s_martproject.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class UserRole {
    @Id
    private int role_id;
    private String role_name;
    private String role_description;
    
    @OneToMany(mappedBy = "role")
    private List<User> users;

    public UserRole(){
    }
    public UserRole(int role_id, String role_name, String role_description){
        this.role_id = role_id;
        this.role_name = role_name;
        this.role_description = role_description;
    }
    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public String getRole_description() {
        return role_description;
    }

    public void setRole_description(String role_description) {
        this.role_description = role_description;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
