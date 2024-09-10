package com.jkromberg.erscm.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for zip operations. Currently supports the following operations:
 * <ul>
 * <li>Unzip</li>
 * </ul>
 * 
 * Code modified from:
 * https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-io/src/main/java/com/baeldung/unzip/UnzipFile.java
 */
public class ZipUtility {

	/**
	 * Unzips the zip file to the specified directory.
	 * 
	 * @param zipPath Zip file path
	 * @param destPath Destination directory path
	 * @return True if file was successfully unzipped, false if not
	 */
	public static boolean unzip(String zipPath, String destPath) {
		// Check for valid zip and destination
		if (zipPath == null || destPath == null) {
			return false;
		}

		File zip = new File(zipPath);
		File destDir = new File(destPath);

		if (!zip.exists() || !destDir.exists() || !destDir.isDirectory()) {
			return false;
		}

		// unzip
		final byte[] buffer = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				final File newFile = newFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					if (!newFile.isDirectory() && !newFile.mkdirs()) {
						throw new IOException("Failed to create directory " + newFile);
					}
				} else {
					File parent = newFile.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}

					final FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
			}

			zis.closeEntry();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @see https://snyk.io/research/zip-slip-vulnerability
	 */
	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

}