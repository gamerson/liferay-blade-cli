package com.liferay.blade.cli.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class PomUtil {
	public static boolean addPluginToPom(Path pom) {

		boolean addedPlugin = false;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(pom.toAbsolutePath().toFile());
			Element root = document.getDocumentElement();
			Node build = root.getElementsByTagName("build").item(0);
	
			if (build instanceof Element) {
				Element buildElement = (Element) build;
	
				Node plugins = buildElement.getElementsByTagName("plugins").item(0);
	
				if (plugins instanceof Element) {
	
					Element pluginsElement = (Element) plugins;
	
					NodeList pluginList = pluginsElement.getChildNodes();
	
					for (int x = 0; x < pluginList.getLength(); x++) {
						Node plugin = pluginList.item(x);
	
						if (plugin instanceof Element) {
							Element pluginNode = (Element) plugin;
	
							Node artifactIdNode = pluginNode.getElementsByTagName("artifactId").item(0);
	
							String artifactId = artifactIdNode.getTextContent();
	
							if (Objects.equals("fizzed-watcher-maven-plugin", artifactId)) {
								addedPlugin = true;
								break;
							}
	
						}
					}
	
					if (!addedPlugin) {
						Element plugin = document.createElement("plugin");
						pluginsElement.insertBefore(plugin, pluginList.item(0));
	
						Element groupId = document.createElement("groupId");
						plugin.appendChild(groupId);
						Text groupIdValue = document.createTextNode("com.fizzed");
						groupId.appendChild(groupIdValue);
	
						Element artifactId = document.createElement("artifactId");
						plugin.appendChild(artifactId);
						Text artifactIdValue = document.createTextNode("fizzed-watcher-maven-plugin");
						artifactId.appendChild(artifactIdValue);
	
						Element version = document.createElement("version");
						plugin.appendChild(version);
						Text versionValue = document.createTextNode("1.0.6");
						version.appendChild(versionValue);
	
						Element config = document.createElement("configuration");
						plugin.appendChild(config);
						Element watches = document.createElement("watches");
						config.appendChild(watches);
						Element watch = document.createElement("watch");
						watches.appendChild(watch);
						Element directory = document.createElement("directory");
						watch.appendChild(directory);
						Text directoryValue = document.createTextNode("src/main/java");
						directory.appendChild(directoryValue);
	
						Element goals = document.createElement("goals");
						config.appendChild(goals);
						Element goal = document.createElement("goal");
						goals.appendChild(goal);
						Text goalValue = document.createTextNode("package");
						goal.appendChild(goalValue);
	
					}
	
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
	
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
	
					transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (PrintStream ps = new PrintStream(baos, true, "utf-8")) {
						
						Result output = new StreamResult(ps);
						Source input = new DOMSource(document);
						
						transformer.transform(input, output);
						ps.close();
						String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
						StringBuilder sb = null;
						try (Scanner scanner = new Scanner(content)) {
							
							while (scanner.hasNextLine()) {
								String line = scanner.nextLine();
								int bracketIndex = line.indexOf('<');
								if (bracketIndex > -1) {
									String linePartial = line.substring(0, bracketIndex);
									while (linePartial.contains("    ")) {
										linePartial = linePartial.replaceFirst("    ", "\t");
									}
									line = linePartial + line.substring(bracketIndex);
									boolean isFirst = sb == null;;
									if (isFirst) {
										sb = new StringBuilder();
									}
									else {
										sb.append(System.lineSeparator());
									}
								}
								
								sb.append(line);
							}
						}
						Files.write( pom, sb.toString().getBytes());
						addedPlugin = true;
					}
					
	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return addedPlugin;

	}
}
