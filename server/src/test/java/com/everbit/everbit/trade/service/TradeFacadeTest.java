package com.everbit.everbit.trade.service;

import com.everbit.everbit.user.service.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradeFacadeTest {

    @InjectMocks
    TradeFacade tradeFacade;

    @Mock
    TradeService tradeService;

    @Mock
    UserService userService;



}