package es.brasatech.fastbite.dto.menu;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SequenceNumberServiceImpl {
    private final AtomicInteger counter = new AtomicInteger(0);

    public int getNextSequenceNumber() {
        return counter.incrementAndGet();
    }

}
