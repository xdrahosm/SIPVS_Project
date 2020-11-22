package SIPVS.helper;

import SIPVS.model.Book;
import SIPVS.model.Borrow;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XmlManipulator {
	
	public static String path = null;

	static String ns(String s) {
		return "md:" + s;
	}

	public void generateHtmlFile(String saveFilePath) {

		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Source xsl = new StreamSource(getClass().getClassLoader().getResource("xml/stylesheet.xsl").getPath());
			String xmlPath = path==null? getClass().getClassLoader().getResource("xml/export.xml").getPath(): path;
			Source xml = new StreamSource(xmlPath);

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

	private Element newXmlElements(Document document, String timestampToken) {

		Element encapsulatedTimeStampElement = document.createElement("xades:EncapsulatedTimeStamp");
		Element signatureTimestampElement = document.createElement("xades:SignatureTimeStamp");
		Element unsignedSignaturePropertiesElement = document.createElement("xades:UnsignedSignatureProperties");
		Element unsignedPropertiesElement = document.createElement("xades:UnsignedProperties");

		unsignedPropertiesElement.appendChild(unsignedSignaturePropertiesElement);
		unsignedSignaturePropertiesElement.appendChild(signatureTimestampElement);
		signatureTimestampElement.appendChild(encapsulatedTimeStampElement);
		Text timestampNode = document.createTextNode(timestampToken);
		encapsulatedTimeStampElement.appendChild(timestampNode);
		return unsignedPropertiesElement;
	}

	public String addTimeStamp(byte[] data) {

		byte request[] = null;
		String result = null;
		TimeStampResponse response = null;

		// request
		TimeStampRequestGenerator requestGenerator = new TimeStampRequestGenerator();
		TimeStampRequest timeStampRequest = requestGenerator.generate(TSPAlgorithms.SHA256, data);
		try {
			request = timeStampRequest.getEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// connection
		OutputStream out;
		HttpURLConnection con;
		try {
			URL url = new URL("http://test.ditec.sk/timestampws/TS.aspx");
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-type", "application/timestamp-query");
			con.setRequestProperty("Content-length", String.valueOf(request.length));
			out = con.getOutputStream();
			out.write(request);
			out.flush();
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP Error: " + con.getResponseCode() + " - " + con.getResponseMessage());
			}
			InputStream in = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			result = builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// response
		String timestampToken = null;
		try {
			response = new TimeStampResponse(Base64.getDecoder().decode(result));
			timestampToken = new String(Base64.getEncoder().encode(response.getTimeStampToken().getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timestampToken;
	}

    private StreamResult addToDocument(String signedXml) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(signedXml)));

            Node signatureValue = document.getElementsByTagName("ds:SignatureValue").item(0);
            if (signatureValue == null) {
                System.err.println("SignatureValue is missing");
            }

            String timeStamp = addTimeStamp(Base64.getEncoder().encodeToString(signatureValue.getTextContent().getBytes()).getBytes());

            Node qualifyingProperties = document.getElementsByTagName("xades:QualifyingProperties").item(0);
            if (qualifyingProperties == null) {
                System.err.println("QualifyingProperties is missing");
            }

            qualifyingProperties.appendChild(newXmlElements(document, timeStamp));


            DOMSource source = new DOMSource(document);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter outWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(outWriter);
            transformer.transform(source, streamResult);
            return streamResult;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public List<String> getTSFiles(String toTSName){
		List<String> toTSXMLs = Stream.of(Objects.requireNonNull(new File(toTSName).listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().contains("xml"))
                .map(File::getAbsolutePath)
				.collect(Collectors.toList());

		return toTSXMLs;
	}

	public String getTSFileName(String fileName){
		String[] nazovSplit = fileName.split("\\\\");
		String nazov = nazovSplit[nazovSplit.length - 1];
		String[] tmp_file_name = nazov.split("/");
		String file_name = tmp_file_name[tmp_file_name.length-1];
		return file_name;
	}

	public void saveXmlWithTS() {
		String TSDirName = "resources/timeStamped/";
		String toTSName = "resources/toTimeStamp/";
		List<String> toTSXMLs = getTSFiles(toTSName);
		if(toTSXMLs.size() < 1) {
			System.out.println("No xmls found!");
		} else {
			toTSXMLs.forEach((fileName) -> {
				System.out.println("Reading file: " + fileName);

				String signedXml = null;
				try {
					signedXml = new String(Files.readAllBytes(Paths.get(fileName)));
				} catch (IOException e) {
					e.printStackTrace();
				}

				try (PrintStream out = new PrintStream(new FileOutputStream(TSDirName+"T_"+getTSFileName(fileName)))) {
					out.print(addToDocument(signedXml).getWriter());
					System.out.println("Add time stamp to file: " + fileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			});
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

