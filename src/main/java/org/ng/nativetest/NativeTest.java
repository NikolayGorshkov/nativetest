package org.ng.nativetest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class NativeTest {
	
	@FunctionalInterface
	private static interface Action {
		void run() throws Exception;
	}

	private static final Path fileJSON = Paths.get("./data/file.json");
	
	private static final Path fileXML = Paths.get("./data/file.xml");
	
	public static void main(String[] args) throws Exception {
		
		String firstArg = args.length == 0 ? "" : args[0];
		
		switch (firstArg) {
		case "genxml":
			writeXmlTestData();
			break;
		case "genjson":
			writeJsonTestData();
			break;
		case "readxml":
			runWithPromptAndTiming(() -> readXml(false));
			break;
		case "readxmlparallel":
			runWithPromptAndTiming(() -> readXmlParallel(false));
			break;
		case "readjson":
			runWithPromptAndTiming(() -> readJson(false));
			break;
		case "readjsonparallel":
			runWithPromptAndTiming(() -> readJsonParallel(false));
			break;
		case "graaltrainingrun":
			readXml(true);
			readXmlParallel(true);
			readJson(true);
			readJsonParallel(true);
			break;
		default:
			IO.println("Arguments supported: (genxml|genjson|readxml|readxmlparallel|readjson|readjsonparallel|graaltrainingrun)");
		}
		
	}
	
	private static void runWithPromptAndTiming(Action action) throws Exception {
		for (;;) {
			IO.readln("Press enter to run action");
			IO.println("Started");
			long start = System.nanoTime();
			action.run();
			IO.println("Finished, took %,d ns".formatted(System.nanoTime() - start));
		}
	}
	
	private static void readXml(boolean breakAfter1K) throws Exception {
		IO.println("readXml");
		int i = 0;
		int lines = 1;
		XmlMapper mapper = new XmlMapper();
		ObjectReader reader = mapper.readerFor(DataClass.class);
		try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(Files.newInputStream(fileXML), UTF_8), 10240)) {
			for (String line = null; (line = lnr.readLine()) != null; lines++) {
				DataClass obj = reader.readValue(line);
				i += obj.getField1().length();
				if (breakAfter1K && lines == 1000) {
					return;
				}
				if (lines % 100_000 == 0) {
					System.out.println(lines);
				}
			}
		}
		IO.println("Temp result: " + i);
	}
	
	private static void readXmlParallel(boolean breakAfter1K) throws Exception {
		IO.println("readXmlParallel");
		int[] i = {0};
		AtomicInteger lines = new AtomicInteger(1);
		int linesRead = 1;
		XmlMapper mapper = new XmlMapper();
		ObjectReader reader = mapper.readerFor(DataClass.class);
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); LineNumberReader lnr = new LineNumberReader(new InputStreamReader(Files.newInputStream(fileXML), UTF_8), 10240)) {
			for (String line = null; (line = lnr.readLine()) != null; ) {
				if (breakAfter1K && linesRead == 1000) {
					return;
				}
				linesRead++;
				String lineF = line;
				executor.execute(() -> {
					DataClass obj;
					try {
						obj = reader.readValue(lineF);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					i[0] += obj.getField1().length();
					
					int linesValue = lines.getAndIncrement();
					if (linesValue % 100_000 == 0) {
						System.out.println(linesValue);
					}
				});
			}
		}
		IO.println("Temp result: " + i[0]);
	}
	
	private static void readJson(boolean breakAfter1K) throws Exception {
		IO.println("readJson");
		int i = 0;
		int lines = 1;
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.readerFor(DataClass.class);
		try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(Files.newInputStream(fileJSON), UTF_8), 10240)) {
			for (String line = null; (line = lnr.readLine()) != null; lines++) {
				DataClass obj = reader.readValue(line);
				i += obj.getField1().length();
				if (breakAfter1K && lines == 1000) {
					return;
				}
				if (lines % 100_000 == 0) {
					System.out.println(lines);
				}
			}
		}
		IO.println("Temp result: " + i);
	}
	
	private static void readJsonParallel(boolean breakAfter1K) throws Exception {
		IO.println("readJsonParallel");
		int[] i = {0};
		AtomicInteger lines = new AtomicInteger(1);
		int linesRead = 1;
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.readerFor(DataClass.class);
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); LineNumberReader lnr = new LineNumberReader(new InputStreamReader(Files.newInputStream(fileJSON), UTF_8), 10240)) {
			for (String line = null; (line = lnr.readLine()) != null; ) {
				if (breakAfter1K && linesRead == 1000) {
					return;
				}
				linesRead++;
				String lineF = line;
				executor.execute(() -> {
					DataClass obj;
					try {
						obj = reader.readValue(lineF);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					i[0] += obj.getField1().length();
					
					int linesValue = lines.getAndIncrement();
					if (linesValue % 100_000 == 0) {
						System.out.println(linesValue);
					}
				});
			}
		}
		IO.println("Temp result: " + i[0]);
	}
	
	
	private static void writeXmlTestData() throws Exception {
		Files.createDirectories(fileJSON.getParent());
		
		int lines = 1_000_000;
		IO.println("Writing %,d lines to %s".formatted(lines, fileXML.toAbsolutePath()));

		XmlMapper mapper = new XmlMapper();
		SerializationConfig config = mapper.getSerializationConfig()
				.without(SerializationFeature.CLOSE_CLOSEABLE)
				.without(Feature.AUTO_CLOSE_TARGET)
				;
		mapper.setConfig(config);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		try (Writer out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(fileXML, StandardOpenOption.CREATE, StandardOpenOption.APPEND), UTF_8))) {
			for (int i = 0; i < lines; i++) {
				DataClass dc = initDataClassWithInner();
				mapper.writeValue(out, dc);
				out.write('\n');
			}
			
		}
		
		IO.println("DONE");
		
	}
	
	private static void writeJsonTestData() throws Exception {
		Files.createDirectories(fileJSON.getParent());
		
		int lines = 1_000_000;
		IO.println("Writing %,d lines to %s".formatted(lines, fileJSON.toAbsolutePath()));
		
		ObjectMapper mapper = new ObjectMapper();
		SerializationConfig config = mapper.getSerializationConfig()
				.without(SerializationFeature.CLOSE_CLOSEABLE)
				.without(Feature.AUTO_CLOSE_TARGET)
				;
		mapper.setConfig(config);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		try (Writer out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(fileJSON, StandardOpenOption.CREATE, StandardOpenOption.APPEND), UTF_8))) {
			for (int i = 0; i < lines; i++) {
				DataClass dc = initDataClassWithInner();
				mapper.writeValue(out, dc);
				out.write('\n');
			}
			
		}
		
		IO.println("DONE");
		
	}
	
	private static final DataClass initDataClass() {
		DataClass result = new DataClass();
		result.setField1(UUID.randomUUID().toString());
		result.setField2(UUID.randomUUID().toString());
		result.setField3(UUID.randomUUID().toString());
		result.setField4(UUID.randomUUID().toString());
		result.setField5(UUID.randomUUID().toString());
		result.setField6(UUID.randomUUID().toString());
		result.setField7(UUID.randomUUID().toString());
		result.setField8(UUID.randomUUID().toString());
		result.setField9(UUID.randomUUID().toString());
		result.setField10(UUID.randomUUID().toString());
		return result;
	}
	
	private static final DataClass initDataClassWithInner() {
		DataClass result = initDataClass();
		result.setInner1(initDataClass());
		result.setInner2(initDataClass());
		result.setInner3(initDataClass());
		result.setInner4(initDataClass());
		result.setInner5(initDataClass());
		result.setInner6(initDataClass());
		result.setInner7(initDataClass());
		result.setInner8(initDataClass());
		result.setInner9(initDataClass());
		result.setInner10(initDataClass());
		return result;
	}
	
}
