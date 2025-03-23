package sat301.s_martproject.model;

import jakarta.persistence.*;

@Entity
public class OrderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_list_id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Column(nullable = false)
    private double price_at_purchase;

    public OrderList() {}

    // Getters & Setters

    public Long getOrder_list_id() {
        return order_list_id;
    }

    public void setOrder_list_id(Long order_list_id) {
        this.order_list_id = order_list_id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice_at_purchase() {
        return price_at_purchase;
    }

    public void setPrice_at_purchase(double price_at_purchase) {
        this.price_at_purchase = price_at_purchase;
    }
}
