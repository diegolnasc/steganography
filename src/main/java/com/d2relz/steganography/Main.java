package com.d2relz.steganography;

import com.d2relz.steganography.cryptography.AES;
import com.d2relz.steganography.service.SteganographyService;
import com.d2relz.steganography.util.SteganographyUtil;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {
		//arguments -i (Image) -f (Files to be hidden) -s (Stego to start or anything to undo)
		start(args);
	}

	private static void start(String[] args) throws Exception {
		List<File> files = new ArrayList<>();
		File zipFile;
		Options options = new Options();
		options.addOption(Option.builder("i").numberOfArgs(1).hasArgs().build());
		options.addOption(Option.builder("f").hasArgs().build());
		options.addOption(Option.builder("c").hasArgs().build());
		options.addOption(Option.builder("s").hasArgs().build());
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		if (!cmd.hasOption("i")) {
			throw new Exception("Image not found!");
		}
		if (cmd.hasOption("s") && cmd.getOptionValue("s").equalsIgnoreCase("stego")) {
			if (!cmd.hasOption("f")) {
				throw new Exception("File not found!");
			} else {
				Arrays.stream(cmd.getOptionValues("f")).map(File::new).forEach(files::add);
				zipFile = SteganographyUtil.zipFles(files);
			}
			if (zipFile != null) {
				new SteganographyService()
						.startSteganography(
								new File(cmd.getOptionValue("i")),
								zipFile,
								cmd.hasOption("c") ? cmd.getOptionValue("c") : null);
			}
		} else {
			new SteganographyService()
					.dSteganography(
							new File(cmd.getOptionValue("i")),
							cmd.hasOption("c") ? cmd.getOptionValue("c") : null);
		}

	}
}
