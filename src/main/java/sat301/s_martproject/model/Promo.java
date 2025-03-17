package sat301.s_martproject.model;

import jakarta.persistence.*;

@Entity
public class Promo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int promo_id;

    private String promo_code;
    private double discount;
    private boolean display; // For displaying on home page
    private boolean active; // Determines if promo can be used
    private String promo_img_url;

    public Promo() {}

    public Promo(String promo_code, double discount, boolean display, boolean active, String promo_img_url) {
        this.promo_code = promo_code;
        this.discount = discount;
        this.display = display;
        this.active = active;
        this.promo_img_url = promo_img_url;
    }

    public int getPromo_id() {
        return promo_id;
    }

    public void setPromo_id(int promo_id) {
        this.promo_id = promo_id;
    }

    public String getPromo_code() {
        return promo_code;
    }

    public void setPromo_code(String promo_code) {
        this.promo_code = promo_code;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPromo_img_url() {
        return promo_img_url;
    }

    public void setPromo_img_url(String promo_img_url) {
        this.promo_img_url = promo_img_url;
    }
}
