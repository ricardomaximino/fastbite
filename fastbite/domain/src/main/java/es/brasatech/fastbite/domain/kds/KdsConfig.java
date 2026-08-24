package es.brasatech.fastbite.domain.kds;

public record KdsConfig(
    String id,
    int yellowTimerMinutes,
    int redTimerMinutes
) {
    public static final String DEFAULT_ID = "default";

    public KdsConfig(int yellowTimerMinutes, int redTimerMinutes) {
        this(DEFAULT_ID, yellowTimerMinutes, redTimerMinutes);
    }
}
