package com.everbit.everbit.trade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.user.entity.User;

public interface TradeRepository extends JpaRepository<Trade, String> {
    List<Trade> findByUser(User user);

    List<Trade> findByUserAndStatus(User user, TradeStatus status);

    List<Trade> findByStatus(TradeStatus status);
}
