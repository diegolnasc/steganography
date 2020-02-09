package com.d2relz.steganography.util;

import com.d2relz.steganography.cryptography.AES;

import java.awt.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SteganographyUtil {

	private final static Logger log = Logger.getLogger(SteganographyUtil.class.getName());

	public static int getAvailableSize(int height, int width) {
		return height * width / 2 - 10;
	}

	public static File zipFles(List<File> files) throws Exception {
		int bRead;
		ZipOutputStream zipOutputStream = null;
		BufferedInputStream bInputFile = null;
		File compactedFile = File.createTempFile("packed", ".zip");
		try {
			zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(compactedFile)));
			zipOutputStream.setLevel(9);
			log.log(Level.INFO, "Starting File Compression");
			for (File arquivo : files) {
				log.log(Level.INFO, "Compacting-> {0}", arquivo.getName());
				zipOutputStream.putNextEntry(new ZipEntry(arquivo.getName()));
				bInputFile = new BufferedInputStream(new FileInputStream(arquivo));
				while ((bRead = bInputFile.read()) != -1) {
					zipOutputStream.write(bRead);
				}
				zipOutputStream.closeEntry();
				bInputFile.close();
				log.log(Level.INFO, "Compressed file-> {0}", arquivo.getName());
			}
			return compactedFile;
		} finally {
			if (zipOutputStream != null) {
				zipOutputStream.close();
			}
			if (bInputFile != null) {
				bInputFile.close();
			}
		}
	}

	public static List<File> unzip(File file, File path, String aesKey) throws Exception {
		if (aesKey != null) {
			new AES().decrypt(aesKey, file, file);
		}
		List<File> files = new LinkedList<>();
		int bRead;
		ZipInputStream zipInputStream = null;
		BufferedOutputStream bOut = null;
		File outFile;
		try {
			zipInputStream = new ZipInputStream(new FileInputStream(file));
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				outFile = new File(path, zipEntry.getName());
				bOut = new BufferedOutputStream(new FileOutputStream(outFile));
				while ((bRead = zipInputStream.read()) != -1) {
					bOut.write(bRead);
				}
				zipInputStream.closeEntry();
				bOut.close();
				zipEntry = zipInputStream.getNextEntry();
				files.add(outFile);
			}
			return files;
		} finally {
			if (bOut != null) {
				bOut.close();
			}
			if (zipInputStream != null) {
				zipInputStream.close();
			}
		}
	}
}