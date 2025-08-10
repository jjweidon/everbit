package com.everbit.everbit.trade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.entity.enums.TradeType;
import com.everbit.everbit.user.entity.User;

public interface TradeRepository extends JpaRepository<Trade, String> {
    List<Trade> findByUser(User user);

    List<Trade> findByUserAndStatus(User user, TradeStatus status);

    List<Trade> findByStatus(TradeStatus status);

    Optional<Trade> findByMarketAndStatus(Market market, TradeStatus status);

    Optional<Trade> findFirstByUserAndMarketAndTypeOrderByCreatedAtDesc(User user, Market market, TradeType type);
}
