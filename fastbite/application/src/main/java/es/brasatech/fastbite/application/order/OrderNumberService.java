package es.brasatech.fastbite.application.order;

/** Hands out the number guests and staff call an order by. */
public interface OrderNumberService {

    /** The next number for the current restaurant; numbering restarts at 1 every business day. */
    int next();
}
