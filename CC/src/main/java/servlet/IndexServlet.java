package main.java.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Servlet implementation class IndexServlet
 */
@WebServlet("/IndexServlet")
@MultipartConfig
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	String fileName = "";
	InputStream fileContent;
	String inputFileString;

	public IndexServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("inputFileString", inputFileString);
		RequestDispatcher dispatch = request.getRequestDispatcher("index.jsp");
		dispatch.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String file_name = null;
		boolean isMultipartContent = ServletFileUpload.isMultipartContent(request);
		if (isMultipartContent) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				List<FileItem> fields = upload.parseRequest(request);
				Iterator<FileItem> it = fields.iterator();
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
							fileItem.write(new File(fileName));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			processInputFile();
		} else if (!isMultipartContent
				&& (request.getParameter("textFieldText") != null && request.getParameter("textFieldText") != "")) {
			inputFileString = request.getParameter("textFieldText");
		}
		TextComparer checker = new TextComparer();
		Pair<Integer, Collection<String>> result = checker.checkText(inputFileString);

		String[] collectionAsArray = result.getRight().toArray(new String[result.getRight().size()]);
		List<String> formattedFileNames = new ArrayList<String>();

		// Format the strings, remove the folder
		for (String fileName : collectionAsArray) {
			formattedFileNames.add(fileName.replace("textFiles/", ""));
		}

		String toPrint = "";

		if (formattedFileNames.size() == 1) {
			toPrint = "Most similar file was: " + formattedFileNames.get(0) + " with score of " + result.getLeft()
					+ ".";
		} else {
			toPrint = "Multiple files scored the same: ";
			for (String fileName : formattedFileNames) {
				toPrint = toPrint + fileName + ", ";
			}
			toPrint = toPrint.substring(0, toPrint.length() - 2);
			toPrint = toPrint + ". All had the score of: " + result.getLeft() + ".";
		}

		request.setAttribute("inputData", toPrint);

		RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
		rd.forward(request, response);
	}

	void processInputFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		String line = reader.readLine();
		String wholeDocument = "";

		while (line != null) {
			wholeDocument = wholeDocument + line;
			line = reader.readLine();
		}
		inputFileString = wholeDocument;
		reader.close();
	}
}
