package SIPVS.helper;

import SIPVS.model.Book;
import SIPVS.model.Borrow;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

public class XmlManipulator {
	
	public static String path = null;

	static String ns(String s) {
		return "md:" + s;
	}

	public void generateHtmlFile(String saveFilePath) {

		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Source xsl = new StreamSource(getClass().getClassLoader().getResource("xml/stylesheet.xsl").getPath());
			Source xml = new StreamSource(getClass().getClassLoader().getResource("xml/export.xml").getPath());

			OutputStream outputStream = new FileOutputStream(saveFilePath);
			Transformer transformer = transformerFactory.newTransformer(xsl);
			transformer.transform(xml, new StreamResult(outputStream));
			outputStream.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void validateAgainstXSD()
	{
		if (path == null) {
			path = getClass().getClassLoader().getResource("xml/export.xml").getPath();
		}
		File file1 = new File(path);
		File file2 = new File(getClass().getClassLoader().getResource("xml/schema.xsd").getPath());

		InputStream xml = null;
		InputStream xsd = null;
		try {
			xml = new FileInputStream(file1);
			xsd = new FileInputStream(file2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	    try
	    {
	        SchemaFactory factory = 
	            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(new StreamSource(xsd));
	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(xml));
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("INFORMATION");
			alert.setHeaderText("XSD SCHEMA VALIDATION");
			alert.setContentText("XML IS VALID");
			alert.showAndWait();
	        
	    }
	    catch(Exception e)
	    {
			e.printStackTrace();
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("XSD SCHEMA VALIDATION");
			alert.setContentText("ERROR IN XML");
			alert.showAndWait();
	    }
	}
	
	public void generateXml(Book book, ObservableList<Borrow> borrows, String path) {


		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null;

			docBuilder = docFactory.newDocumentBuilder();


			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(ns("Library"));
			rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			rootElement.setAttribute("xsi:schemaLocation", "http://www.example.org/sipvs/schema.xsd");
			rootElement.setAttribute("xmlns:md", "http://www.example.org/sipvs");
			doc.appendChild(rootElement);

			Element bookElement = doc.createElement(ns("Book"));
			rootElement.appendChild(bookElement);

			// shorten way
			// staff.setAttribute("id", "1");

			Element bookName = doc.createElement(ns("BookName"));
			bookName.setTextContent(book.getBookName());
			bookElement.appendChild(bookName);

			Element authorName = doc.createElement(ns("AuthorName"));
			authorName.setTextContent(book.getAuthorName());
			bookElement.appendChild(authorName);

			Element pages = doc.createElement(ns("Pages"));
			pages.setTextContent(book.getPages().toString());
			bookElement.appendChild(pages);

			Element printed = doc.createElement(ns("PrintingYear"));
			printed.setTextContent(book.getPrintingYear().toString());
			bookElement.appendChild(printed);

			Element firstPublished = doc.createElement(ns("FirstPublished"));
			firstPublished.setTextContent(book.getFirstPublished().toString());
			bookElement.appendChild(firstPublished);

			Element ISBN = doc.createElement(ns("ISBN"));
			ISBN.setTextContent(book.getIsbn());
			bookElement.appendChild(ISBN);

			Element bookDescription = doc.createElement(ns("BookDescription"));
			bookDescription.setTextContent(book.getBookDescription());
			bookElement.appendChild(bookDescription);


			
			Element borrowsElement = doc.createElement(ns("BorrowHistory"));
			bookElement.appendChild(borrowsElement);
			
			int i = 1 ;
			for (Borrow borrow : borrows) {
				Element borrowElement = doc.createElement(ns("Borrow"));

				borrowElement.setAttribute("id", String.valueOf(i));
				i++;
				
				Element studentName = doc.createElement(ns("StudentName"));
				studentName.setTextContent(borrow.getName());
				borrowElement.appendChild(studentName);
				
				Element borrowDate = doc.createElement(ns("BorrowDate"));
				borrowDate.setTextContent(borrow.getBorrowDate());
				borrowElement.appendChild(borrowDate);
				
				Element returnDate = doc.createElement(ns("ReturnDate"));
				returnDate.setTextContent(borrow.getReturnedDate());
				borrowElement.appendChild(returnDate);
				
				borrowsElement.appendChild(borrowElement);
				
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(path);
			XmlManipulator.path= path;
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}

	}
}

