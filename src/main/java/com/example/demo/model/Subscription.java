package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.FREE;

    private String paystackReference;
    private String paystackCustomerCode;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private LocalDateTime lastRenewalDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public String getPaystackReference() { return paystackReference; }
    public void setPaystackReference(String paystackReference) { this.paystackReference = paystackReference; }
    public String getPaystackCustomerCode() { return paystackCustomerCode; }
    public void setPaystackCustomerCode(String paystackCustomerCode) { this.paystackCustomerCode = paystackCustomerCode; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public LocalDateTime getLastRenewalDate() { return lastRenewalDate; }
    public void setLastRenewalDate(LocalDateTime lastRenewalDate) { this.lastRenewalDate = lastRenewalDate; }
}