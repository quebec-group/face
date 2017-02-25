package uk.ac.cam.cl.quebec.face.storage;

import uk.ac.cam.cl.quebec.face.config.Config;
import uk.ac.cam.cl.quebec.face.exceptions.StorageException;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles all directory structures on disk
 */
class DirectoryStructure {
    static File getOrMakeTempDirectory(Config config) throws StorageException {
        return getOrMakeDirectory(config.TempFileDir);
    }
    static File getOrMakeUserDirectory(Config config, String userId, RecogniserConfigSlug slug)
            throws StorageException {
        String path = assembleUserDirectoryPath(config, userId, slug);
        return getOrMakeDirectory(path);
    }

    private static File getOrMakeDirectory(String path) throws StorageException {
        File tempDir = new File(path);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        if (!tempDir.isDirectory()) {
            throw new StorageException("Directory path (" + path + ") already exists, but is not a directory!");
        }

        return tempDir;
    }

    static String assembleUserDirectoryPath(Config config, String userId, RecogniserConfigSlug slug) {
        return assembleUserDirectoryPath(config, userId, slug.toString());
    }
    static String assembleUserDirectoryPath(Config config, String userId, String settingsSlug) {
        // File path structure: dataDir/training/{settingsSlug}/{userId}/*randomString*.hist

        String[] segments = {
                config.DataDir,
                "training",
                settingsSlug,
                userId
        };
        return Arrays.stream(segments).collect(Collectors.joining("/"));
    }

    static Set<String> findAllConfigSlugsForUser(Config config, String userId) throws StorageException {
        File trainingDir = getOrMakeDirectory(config.DataDir + "/training");
        String[] allSlugs = trainingDir.list();

        Set<String> slugs = new HashSet<>();
        for (String slug : allSlugs) {
            File userDir = new File(assembleUserDirectoryPath(config, userId, slug));
            if (!userDir.exists() || userDir.list().length == 0) {
                continue;
            }

            slugs.add(slug);
        }

        return slugs;
    }
}
