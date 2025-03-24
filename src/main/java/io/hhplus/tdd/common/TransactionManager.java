package io.hhplus.tdd.common;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

@UtilityClass
@RequiredArgsConstructor
public class TransactionManager {
    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    public <T> void runTransaction(T backup, Consumer<T> action, Runnable rollback) {
        try{
            action.accept(backup);
        }catch(Exception e){
            rollback.run();
            logger.error("Exception in runTransaction",e);
        }
    }
}
