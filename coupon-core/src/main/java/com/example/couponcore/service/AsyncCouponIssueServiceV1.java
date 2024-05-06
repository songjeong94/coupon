package com.example.couponcore.service;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.couponcore.exception.ErrorCode.*;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // sorted set
//    public void issue(long userId, long couponId) {
//        // 1. 유저의 요청을 sorted set 적재
//        String key = "issue.request.sorted_set.couponId=%s".formatted(couponId);
//        redisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());
//        // 2. 유저의 요청의 순서를 조회
//        // 3. 조회 결과를 선착순 조건과 비교
//        // 4. 쿠폰 발급 queue에 적재
//    }

    public void issue(long couponId, long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        if (!coupon.availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. couponId: %s, issueStart:%s, issueEnd:%s".formatted(couponId, coupon.getDateIssueStart(), coupon.getDateIssueEnd()));
        }
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리됐습니다.. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }
}
