package servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Servlet implementation class IndexServlet
 */
@WebServlet("/IndexServlet")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
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
	 *      response)
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
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// String description = request.getParameter("description"); // Retrieves <input
		// type="text" name="description">
		// Part filePart = request.getPart("file"); // Retrieves <input type="file"
		// name="file">
		// fileName =
		// Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE
		// fix.
		// fileContent = filePart.getInputStream();
		// System.out.println(filePart);
		// doGet(request, response);
		processInputFile();

		TextSimiliarityChecker checker = new TextSimiliarityChecker();
		Pair<Double, String> result = checker.checkText(inputFileString);

		String toPrint = "Most similiar file was: " + result.getRight() + " with score: " + result.getLeft();

		request.setAttribute("inputData", toPrint);

		RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
		rd.forward(request, response);
	}

	void processInputFile() throws IOException {
		// tento Path bude treba casom vymazat/ alebo koli testom nastavit spravne
		String jspPath = "D:\\Martin\\Documents\\UNI\\WS 18\\CC\\PaaS\\petovProjekt\\CC-TextChecker\\CC\\WebContent\\WEB-INF\\README.txt";
		BufferedReader reader = new BufferedReader(new FileReader(jspPath));
		// BufferedReader br = new InputStreamReader(new FileInputStream(txtFilePath));
		// StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		String wholeDocument = "";

		while (line != null) {
			wholeDocument = wholeDocument + line;
			line = reader.readLine();
		}
		inputFileString = wholeDocument;
		System.out.println("INPUT: " + inputFileString);
		reader.close();
	}

}
