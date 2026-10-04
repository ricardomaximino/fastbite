package es.brasatech.fastbite.domain.tenant;
public record SetupProgress(boolean started, boolean serviceReviewed, boolean previewReviewed, boolean dismissed) {
    public static final SetupProgress EMPTY = new SetupProgress(false, false, false, false);
}
