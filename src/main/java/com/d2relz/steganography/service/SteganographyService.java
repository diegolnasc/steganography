package com.d2relz.steganography.service;

import com.d2relz.steganography.cryptography.AES;
import com.d2relz.steganography.util.SteganographyUtil;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SteganographyService {

	private final static Logger log = Logger.getLogger(SteganographyService.class.getName());

	public void startSteganography(File imageFile, File zipFile, String aesKey) throws Exception {
		try {
			log.log(Level.INFO, "Starting Steganography. ZipFile size {0} bytes.", zipFile.length());
			int byteRead;
			doCrypto(aesKey, zipFile);
			long zipFileSize = zipFile.length();
			File exitFile;
			List<Integer> pixelsOrder;
			BufferedImage image = ImageIO.read(imageFile);
			checkCapacity(zipFileSize, image);
			pixelsOrder = generatePixelOrder(image.getHeight() * image.getWidth());
			writeHeader(image, pixelsOrder, zipFileSize);
			int pixelNumber = 9;
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(zipFile));
			while ((byteRead = in.read()) != -1) {
				putLSB(image, pixelsOrder.get(++pixelNumber), byteRead >> 4);
				putLSB(image, pixelsOrder.get(++pixelNumber), byteRead);
			}
			in.close();
			exitFile = new File(String.format("%snew%s", FilenameUtils
					.getFullPath(imageFile.getPath()), FilenameUtils.getName(imageFile.getPath())));
			ImageIO.write(image, FilenameUtils.getExtension(imageFile.getPath()), exitFile);
			log.log(Level.INFO, "Steganography Finished");
		} catch (Exception e) {
			log.log(Level.SEVERE, "error {0}", e);
			throw e;
		}
	}

	private void doCrypto(String aesKey, File zipFile) throws Exception {
		if (Objects.nonNull(aesKey)) {
			new AES().encrypt(aesKey, zipFile, zipFile);
		}
	}

	private void checkCapacity(long length, BufferedImage openImage) throws IllegalAccessException {
		if (length > SteganographyUtil.getAvailableSize(openImage.getHeight(), openImage.getWidth())) {
			throw new IllegalAccessException(String.format("exceeded the size allowed to write on the image. " +
					"Compressed File Size {%s} bytes.", length));
		}
	}

	private void writeHeader(BufferedImage image, List<Integer> pixelsOrder, long zipFileSize) {
		IntStream.range(0, 10).forEach(pixelNumber -> {
			int value = Integer.parseInt(Long.toString(zipFileSize >> ((9 - pixelNumber) * 4)));
			putLSB(image, pixelsOrder.get(pixelNumber), value);
			putLSB(image, pixelsOrder.get(pixelNumber), value);
		});
	}

	private int getSizeFromHeader(BufferedImage image, List<Integer> pixelsOrder) {
		int zipFileSize = 0;
		for (int pixelNumber = 0; pixelNumber < 10; pixelNumber++) {
			zipFileSize = pixelNumber == 0 ?
					getLSB(image, pixelsOrder.get(pixelNumber)) :
					zipFileSize << 4 | getLSB(image, pixelsOrder.get(pixelNumber));
		}
		return zipFileSize;
	}

	public void dSteganography(File image, String aesKey) throws Exception {
		BufferedImage openImage = ImageIO.read(image);
		List<Integer> pixelsOrder = generatePixelOrder(openImage.getHeight() * openImage.getWidth());
		int zipFileSize = getSizeFromHeader(openImage, pixelsOrder);
		File tempFile = File.createTempFile("packed", ".zip");
		List<File> listFiles;
		BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(tempFile));
		log.log(Level.INFO, "D-Esteganography Starting");
		for (int pixelNumber = 9; pixelNumber < zipFileSize * 2 + 10; ) {
			outFile
					.write(getLSB(openImage, pixelsOrder.get(++pixelNumber)) << 4 |
							getLSB(openImage, pixelsOrder.get(++pixelNumber)));
		}
		outFile.close();
		log.log(Level.INFO, "D-Esteganography Finished");
		listFiles = SteganographyUtil.unzip(tempFile, image.getParentFile(), aesKey);
	}

	private void putLSB(BufferedImage image, int pixelNumber, int value) {
		int pixel = image.getRGB((pixelNumber % image.getWidth()), (pixelNumber / image.getWidth()));
		pixel &= 0xFFFCFEFE;
		value &= 0x0F;
		pixel |= (value & 0x01);
		pixel |= (((value >> 1) & 0x01) << 8);
		pixel |= (((value >> 2) & 0x03) << 16);
		image.setRGB((pixelNumber % image.getWidth()), (pixelNumber / image.getWidth()), pixel);
	}

	private int getLSB(BufferedImage image, int pixelNumber) {
		int value;
		int pixel = image.getRGB((pixelNumber % image.getWidth()), (pixelNumber / image.getWidth()));
		pixel &= 0x00FFFFFF; // No alpha
		//Get the Blue channel ( 1 Bit ).
		value = pixel & 0x01;
		//Get the Green channel ( 1 Bit ).
		value |= ((pixel >> 8) & 0x01) << 1;
		//Get the Red channel ( 2 Bit ).
		return value |= ((pixel >> 16) & 0x03) << 2;
	}

	private List<Integer> generatePixelOrder(int n) {
		List<Integer> listOrder = IntStream.range(0, n).boxed().collect(Collectors.toList());
		Collections.shuffle(listOrder, new Random(n / 2));
		return listOrder;
	}
}