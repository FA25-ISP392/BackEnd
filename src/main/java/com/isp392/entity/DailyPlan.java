package com.isp392.entity;

import com.isp392.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "DailyPlan", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"itemId", "itemType", "planDate"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int planId;

    @Column(nullable = false)
    int itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ItemType itemType;

    @Column(nullable = false)
    LocalDate planDate;

    @Column(nullable = false)
    int plannedQuantity;

    @Column(nullable = false)
    int remainingQuantity;

    @Column(nullable = false, columnDefinition = "BIT default 0")
    Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffId", nullable = false)
    @ToString.Exclude
    Staff plannerStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedByStaffId")
    @ToString.Exclude
    Staff approverStaff;
}