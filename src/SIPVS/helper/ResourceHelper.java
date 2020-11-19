package SIPVS.helper;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import SIPVS.sign.XadesSigner;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;

public class ResourceHelper {

    static public InputStream getResourceAsStream(String name) throws FileNotFoundException {
        String path = new File("/resources", name).getPath();
        return new FileInputStream(name);
    }

    static public String readResource(String name) throws IOException {
        InputStream is = getResourceAsStream(name);
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        return new String(data, "UTF-8");
    }

    static public String readResourceAsBase64(String name) throws IOException {
        InputStream is = getResourceAsStream(name);
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        String msg = Base64.encode(data);
        return msg;

    }

    static public void writeFileFromBase64(String filename, String base64) throws IOException {
        String path = new File(System.getProperty("user.home"), filename).getAbsolutePath();
        FileOutputStream is = new FileOutputStream(path);
        try {
            is.write(Base64.decode(base64));
        } catch (Base64DecodingException e) {
            throw new RuntimeException(e);
        } finally {
            is.close();
        }
    }

    static public XadesSigner getXadesSigner() {

        String toSigndirName = "resources/toSign/";
        String toXsdDirName = "resources/xml/";
        String signedDirName = "resources/signed/";

        List<String> toSignXMLs = Stream.of(Objects.requireNonNull(new File(toSigndirName).listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().contains("xml"))
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());


        if(toSignXMLs.size() < 1) {
            System.out.println("No xmls found!");
            return null;
        }

        String xsdFile = Stream.of(Objects.requireNonNull(new File(toXsdDirName).listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().contains("xsd"))
                .map(File::getAbsolutePath)
                .findFirst()
                .orElse(null);

        String xslFile = Stream.of(Objects.requireNonNull(new File(toXsdDirName).listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().contains("xsl"))
                .map(File::getAbsolutePath)
                .findFirst()
                .orElse(null);

        String signedFiles = Stream.of(Objects.requireNonNull(new File(toSigndirName).listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().contains("xml"))
                .map((x) -> {
                    var absPath = x.getAbsolutePath().split("resources");
                    var path = absPath[0];
                    return new File(path + signedDirName, x.getName()).getPath();
                }).findFirst().orElse(null);

        return new XadesSigner(toSignXMLs, xsdFile, xslFile, signedFiles);
    }
}
