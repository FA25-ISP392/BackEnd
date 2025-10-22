package com.isp392.entity;

import com.isp392.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    int id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Orders order;

    @Enumerated(EnumType.STRING)
    @Column(name = "method",  nullable = false)
    PaymentMethod method;

    @Column(name = "total", nullable = false)
    double total;

    @Column(name = "status")
    String status;

    @Column(name = "payos_order_code")
    Long payosOrderCode;

    @Column(name = "payment_link_id")
    String paymentLinkId;

    @Column(name = "checkout_url", columnDefinition = "TEXT")
    String checkoutUrl;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    String qrCode;
}
