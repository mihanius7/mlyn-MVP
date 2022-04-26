package file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import gui.MainWindow;

public class SAXelementParser {

	public void loadFromFile(File file) {
		try {
			URL inputURL = new URL(file.toURI().toString());
			String fileName = inputURL.toString();
			MainWindow.println("Адкрыццё файла: " + fileName);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			ElementHandler handler = new ElementHandler();
			reader.setContentHandler(handler);
			//reader.setProperty("http://apache.org/xml/properties/locale", new Locale("by", "BY"));
			if (handler != null)
				reader.parse(fileName);
		} catch (SAXException e) {
			e.printStackTrace();
			MainWindow.println("Памылка парсера");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainWindow.println("Файл ня знойдзены");
		} catch (IOException e) {
			e.printStackTrace();
			MainWindow.println("Памылка падчас чытання файла");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			MainWindow.println("Памылка падчас атрымання лічбы з тэкставай зменнай");
		}
	}

}
