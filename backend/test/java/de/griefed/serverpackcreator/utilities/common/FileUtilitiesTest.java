package de.griefed.serverpackcreator.utilities.common;

import de.griefed.serverpackcreator.Dependencies;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileUtilitiesTest {

  FileUtilitiesTest() {}

  @Test
  void unzipArchiveTest() {
    String modpackDir = "backend/test/resources/curseforge_tests";
    String zipFile = "backend/test/resources/curseforge_tests/modpack.zip";
    Dependencies.getInstance().UTILITIES().FileUtils().unzipArchive(zipFile, modpackDir);
    Assertions.assertTrue(
        new File("./backend/test/resources/curseforge_tests/manifest.json").exists());
    Assertions.assertTrue(
        new File("./backend/test/resources/curseforge_tests/modlist.html").exists());
    Assertions.assertTrue(
        new File("./backend/test/resources/curseforge_tests/overrides").isDirectory());
    FileUtils.deleteQuietly(new File("./backend/test/resources/curseforge_tests/manifest.json"));
    FileUtils.deleteQuietly(new File("./backend/test/resources/curseforge_tests/modlist.html"));
    FileUtils.deleteQuietly(new File("./backend/test/resources/curseforge_tests/overrides"));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void replaceFileTest() throws IOException {
    File source = new File("source.file");
    File destination = new File("destination.file");
    source.createNewFile();
    destination.createNewFile();
    Dependencies.getInstance().UTILITIES().FileUtils().replaceFile(source, destination);
    Assertions.assertFalse(source.exists());
    Assertions.assertTrue(destination.exists());
    FileUtils.deleteQuietly(destination);
  }
}
