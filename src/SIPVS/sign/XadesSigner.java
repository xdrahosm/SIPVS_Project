package SIPVS.sign;

import SIPVS.helper.ResourceHelper;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import sk.ditec.zep.dsigner.xades.XadesSig;
import sk.ditec.zep.dsigner.xades.plugin.DataObject;
import sk.ditec.zep.dsigner.xades.plugins.xmlplugin.XmlPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class XadesSigner {

    private List<String> xmlFiles;
    private String xsdFile;
    private String xsltFile;
    private List<String> signed;

    public XadesSigner(List<String> xmlFiles, String xsdFile, String xsltFile, List<String> signed) {
        this.xmlFiles = xmlFiles;
        this.xsdFile = xsdFile;
        this.xsltFile = xsltFile;
        this.signed = signed;
    }

    public void sign() {
        XadesSig dSigner = new XadesSig();
        dSigner.installLookAndFeel();
        dSigner.installSwingLocalization();
        dSigner.reset();
        dSigner.setLanguage("sk");

        String DEFAULT_XSD_REF = "http://www.w3.org/2001/XMLSchema";
        String DEFAULT_XSLT_REF = "http://www.example.org/sipvs";
        XmlPlugin xmlPlugin = new XmlPlugin();

        xmlFiles.forEach((fileName) -> {
            System.out.println("Reading file: " + fileName);
            try {
                DataObject xmlObj = xmlPlugin.createObject2(
                        "XML" + 1,
                        "XML" + 1,
                        ResourceHelper.readResource(fileName),
                        ResourceHelper.readResource(xsdFile),
                        "http://www.example.org/sipvs",
                        DEFAULT_XSD_REF,
                        ResourceHelper.readResource(xsltFile),
                        DEFAULT_XSLT_REF,
                        "HTML"
                        );

                if (xmlObj == null) {
                    System.out.println("XMLPlugin.createObject() errorMessage=" + xmlPlugin.getErrorMessage());
                    Alert dialog = new Alert(Alert.AlertType.ERROR, "Problem with XMLPlugin.createObject", ButtonType.OK);
                    dialog.show();
                    return;
                }

                int rc = dSigner.addObject(xmlObj);
                if (rc != 0) {
                    System.out.println("XadesSig.addObject() errorCode=" + rc + ", errorMessage=" + dSigner.getErrorMessage());
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Alert dialog = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                dialog.show();
            }
        });

        int rc = dSigner.sign20("signatureId20", "http://www.w3.org/2001/04/xmlenc#sha256", "urn:oid:1.3.158.36061701.1.2.2", "dataEnvelopeId",
                "dataEnvelopeURI", "dataEnvelopeDescr");
        if (rc != 0) {
            System.out.println("XadesSig.sign20() errorCode=" + rc + ", errorMessage=" + dSigner.getErrorMessage());
            return;
        }

        System.out.println(dSigner.getSignedXmlWithEnvelope());

        this.signed.forEach((x) -> {
            try{
                FileWriter fileWriter = new FileWriter(new File(x));
                fileWriter.write(dSigner.getSignedXmlWithEnvelope());
                fileWriter.flush();
                fileWriter.close();
            } catch(IOException e) {
                e.printStackTrace();
                Alert dialog = new Alert(Alert.AlertType.ERROR, "Could not write signed file", ButtonType.OK);
                dialog.show();
            }
        });

    }

}
