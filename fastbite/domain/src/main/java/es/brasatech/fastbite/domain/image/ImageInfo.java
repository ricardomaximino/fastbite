package es.brasatech.fastbite.domain.image;

/**
 * Represents information about an image.
 * Used for both system images and user-uploaded images.
 */
public record ImageInfo(
        String name, // File name (e.g., "burger.jpg")
        String url, // URL path to access the image (e.g., "/images/food/burger.jpg")
        String folder, // Folder/group name (e.g., "food", "drinks")
        String type, // "system" or "user"
        long size // File size in bytes
) {
}
