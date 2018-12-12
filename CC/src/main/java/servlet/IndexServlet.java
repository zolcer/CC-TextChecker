package main.java.servlet;

import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class IndexServlet
 */
@WebServlet("/IndexServlet")
@MultipartConfig
public class IndexServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PATH_TO_SAVED_INPUTFILE = "/Users/peterzolcer/CC-TextChecker/";

    
    String fileName = "";
    InputStream fileContent;
    String inputFileString;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public IndexServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        request.setAttribute("inputFileString", inputFileString);

        RequestDispatcher dispatch = request.getRequestDispatcher("index.jsp");
        dispatch.forward(request, response);
        // response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
	    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    		throws ServletException, IOException {
	
	    	
	    	
	    	
	    	
	    	String file_name = null;
	    	boolean isMultipartContent = ServletFileUpload.isMultipartContent(request);
	    	if (!isMultipartContent) {
	    		return;
	    	}
	    	FileItemFactory factory = new DiskFileItemFactory();
	    	ServletFileUpload upload = new ServletFileUpload(factory);
	    	try {
	    		List < FileItem > fields = upload.parseRequest(request);
	    		Iterator < FileItem > it = fields.iterator();
	    		if (!it.hasNext()) {
	    			return;
	    		}
	    		while (it.hasNext()) {
	    			FileItem fileItem = it.next();
	    			boolean isFormField = fileItem.isFormField();
	    			if (isFormField) {
	    				if (file_name == null) {
	    					if (fileItem.getFieldName().equals("file_name")) {
	    						file_name = fileItem.getString();
	    						fileName = file_name;
	    					}
	    				}
	    			} else {
	    				if (fileItem.getSize() > 0) {
	    					fileName = fileItem.getName();
	    					fileItem.write(new File("/Users/peterzolcer/CC-TextChecker/" + fileName));
	    				}
	    			}
	    		}
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	} 
	    	
	    	if (fileName.isEmpty() || fileName == null) {
	    		inputFileString = request.getParameter("textFieldText");
	    		fileName = "";
		    	System.out.println("text");

	    	} else {
		    	processInputFile();
		    	System.out.println("file");
	    	}
	
	    	System.out.println(inputFileString);
	    	
	    	TextSimiliarityChecker checker = new TextSimiliarityChecker();
	    	Pair<Double, String> result = checker.checkText(inputFileString);
	    	double similarityPercentage = convertScoreToPercentage(result.getLeft());
	
	    	String toPrint = "Most similiar file was: " + result.getRight() + " with similarity of: " + similarityPercentage +"%";
	
	    	request.setAttribute("inputData", toPrint);
	
	    	RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
	    	rd.forward(request, response);
    }

    void processInputFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PATH_TO_SAVED_INPUTFILE+fileName));

        String line = reader.readLine();
        String wholeDocument = "";

        while (line != null) {
            wholeDocument = wholeDocument + line;
            line = reader.readLine();
        }
        inputFileString = wholeDocument;
        reader.close();
    }
    
    double convertScoreToPercentage(double score)
    {    		
		return score*100;
    }

}
