package com.example.couponcore.repository.redis.dto;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.model.CouponType;
import com.example.couponcore.model.QCoupon;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalDateTime;

public record CouponRedisEntity (
        Long id,
        CouponType couponType,
        Integer totalQuantity,

        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDateTime dateIssueEnd
){

    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public void checkIssuableCoupon() {
        if(!availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. couponId: %s, issueStart:%s, issueEnd:%s".formatted(id, dateIssueStart, dateIssueEnd));
        }
    }
}
