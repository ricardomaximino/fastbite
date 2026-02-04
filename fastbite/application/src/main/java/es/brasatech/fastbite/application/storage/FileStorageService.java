package es.brasatech.fastbite.application.storage;

import es.brasatech.fastbite.domain.image.ImageInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Port for file storage operations.
 */
public interface FileStorageService {

    /**
     * Upload an image file.
     *
     * @param data             the file input stream
     * @param originalFilename the original name of the file
     * @param folder           optional subfolder to organize the file
     * @return information about the stored image
     * @throws IOException if storage fails
     */
    ImageInfo uploadImage(InputStream data, String originalFilename, String folder) throws IOException;

    /**
     * List system/predefined images.
     *
     * @return map of folder name to list of images
     */
    Map<String, List<ImageInfo>> listSystemImages();

    /**
     * List user-uploaded images.
     *
     * @return map of folder name to list of images
     */
    Map<String, List<ImageInfo>> listUserImages();

    /**
     * Get the root directory where files are stored.
     *
     * @return storage path or identifier
     */
    String getUploadDirectory();
}
